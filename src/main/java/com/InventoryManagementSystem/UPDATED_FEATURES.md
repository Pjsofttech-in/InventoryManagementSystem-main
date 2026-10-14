****# Backend changes

## Restock / pricing
`PUT /api/products/{id}/add-stock` now treats a stock update as a new stock batch.

- Added quantity is added to current stock.
- No price averaging is performed.
- Current product `mrp`, vendor discount, store/purchase price and customer price are replaced by the new values for the new restock.
- A `ProductStockBatch` is persisted with its own purchase/selling prices and quantity.
- A `StockHistory` entry is persisted as `RESTOCK`.
- A `VendorBill` is automatically generated from purchase price × quantity.
- Optional initial vendor payment is recorded.

## Batch APIs
Existing `/api/stock-batches` endpoints are now paired with frontend batch viewing.

## Vendor bill APIs
Added `VendorBillController`:
- `GET /api/vendor-bills`
- `GET /api/vendor-bills/{id}`
- `GET /api/vendor-bills/vendor/{vendorId}`
- `GET /api/vendor-bills/print/{id}`

## Orders
Order creation consumes available stock batches FIFO and records `ORDER_DEDUCTION` stock history. Product stock remains the source of truth for legacy products that do not have batches.

## Invoice
The existing backend invoice PDF endpoint remains `/api/v1/invoices/print/{orderId}` and the frontend now uses it for the print action.

No new database table is introduced by these changes; the existing batch, stock-history and vendor-bill entities are used.

---

# New changes (Product Actual MRP, Order Due Date & GST Summary)

## 1. New "Actual Product MRP" field on Product
Product pricing now has a clear 5-step chain, in this order:

1. `mrp` — Vendor MRP (price the vendor quotes as MRP)
2. `vendorDiscountPercentage` — Vendor Discount % on that MRP
3. `storePrice` — Vendor Discounted Price (our purchase price after vendor discount)
4. **`productMrp` (NEW)** — Actual Product MRP (the real MRP printed on the product, shown to the end customer)
5. `customerPrice` — Store Price / Selling Price (what we actually sell it for)

- `productMrp` is sent the same way as the other pricing fields, both on `POST /api/products` (create) and `PUT /api/products/{id}/add-stock` (update/restock via `StockUpdateRequest`), and also via `StockPurchaseRequestDTO` (`/api/stock-batches` purchase flow).
- It is stored on the product, and also snapshotted into `StockHistory` and `ProductStockBatch` for every restock, so historical batches keep their own MRP.

## 2. Order creation shows Product MRP
- `InventoryOrderItem` now has a `productMrp` field, filled automatically from the product's `productMrp` at the moment the order is created (`OrderServiceImpl.createOrder`).
- Together with the already-existing `unitPrice` (store price) and `customerDiscountPercentage` (order discount), the order item now carries: Actual Product MRP + Store Price + Discount — exactly what's needed to render "MRP / your price / discount" on the order screen.

## 3. Order page: GST per order + grand total + payment due date/overdue handling
- Each order already exposed `totalGstAmount` (that order's total GST) plus `totalCgstAmount` / `totalSgstAmount` / `totalUgstAmount` / `totalIgstAmount` — unchanged, still returned by `GET /getAllOrders` and `GET /getOrderById/{id}`.
- New `InventoryOrder.paymentDueDate` — the date by which the customer must pay for that order. Send it when creating the order (optional).
- New `GET /getOrdersGstSummary` endpoint returns a grand total across **all** orders:
  - `totalGstAmount`, `totalCgstAmount`, `totalSgstAmount`, `totalUgstAmount`, `totalIgstAmount`
  - `totalOrders`
  - `overdueOrdersCount` and `totalOverdueAmount`
- Overdue handling: every time an order is fetched (`getAllOrders`, `getOrderById`, or the GST summary), the backend checks `paymentDueDate` against today's date. If the due date has passed and the order still has a pending amount:
  - `order.paymentOverdue` becomes `true`
  - `order.overdueDays` shows how many days late it is
  - `order.paymentStatus` becomes the new `OVERDUE` value (added to the `PaymentStatus` enum)
  - This is computed live, so it always reflects "today" without needing a background job.


## Order Update / Modify Order

- Added `PUT /updateOrder/{id}` for modifying an existing order.
- Update supports customer, order date, payment due date, product lines, quantities, and per-line customer discount.
- Backend recalculates price/GST/totals from current product data; client totals are not trusted.
- Stock is reconciled transactionally by restoring the old order quantity and consuming the new quantity.
- Stock-batch FIFO consumption and restoration are handled, including fully consumed batches.
- Stock history records order-update restore/deduction operations.
- Duplicate products and invalid quantities/discounts are rejected.
- An update cannot make the order total lower than money already received.
- PENDING/CONFIRMED orders can be edited. SHIPPED/DELIVERED orders are locked. CANCELLED orders are immutable.
- Cancelling an unpaid/partially-unpaid editable order restores stock; paid orders cannot be cancelled through this endpoint because a refund flow is not present.
- New orders must start as PENDING or CONFIRMED; shipment status is kept in the shipment workflow.
- Frontend adds an Edit action, reuses the existing order form for edit mode, sends the new update DTO, and refreshes orders/products/GST summaries after success.


---

# Latest update: vendor discount on Product MRP, product stock/sales columns, order grand totals

## 1. Vendor discount is now on the ACTUAL Product MRP
The vendor gives ONE kind of discount (flat in rupees OR percent); the other values are calculated (`util/VendorPricingUtil`).

| Vendor says | You send | Backend calculates |
|---|---|---|
| MRP 1000, flat discount Rs 100 | `productMrp=1000`, `vendorDiscountAmount=100` | `vendorDiscountPercentage=10`, `storePrice=900` |
| MRP 1000, 10% discount | `productMrp=1000`, `vendorDiscountPercentage=10` | `vendorDiscountAmount=100`, `storePrice=900` |

- If both are sent, the flat discount amount wins. A flat amount of `0`/`null` means "not given".
- Flat discount cannot be more than `productMrp`; percentage must be 0-100.
- Purchase price (`storePrice` / `purchasePrice`) is always calculated; any value sent for it is ignored.
- Applies to `POST /api/products`, `PUT /api/products/{id}/add-stock` and `/api/stock-batches` purchase.
- Column `vendorDiscountAmount` on `products` and `product_stock_batches` (nullable, auto-added by Hibernate ddl-auto=update).
- Old `mrp` / `vendorMrp` is kept only for old data; it is no longer used for discount maths.

## 2. Product columns + grand totals
Every product response now has: `totalStock` (remaining + sold), `remainingStock`, `totalSold`, `totalAmount` (remaining x selling price, unchanged), `totalSoldAmount` (sales after discount, before GST).
- Computed from live orders (cancelled orders excluded) with ONE grouped query for the whole list, so nothing can go out of sync and no migration is needed.
- `GET /api/products/summary` returns the totals of all columns: `totalProducts`, `totalStock`, `totalRemainingStock`, `totalSold`, `totalAmount`, `totalSoldAmount`.

## 3. Orders grand totals
`GET /getOrdersGstSummary` now also returns `totalOrderAmount`, `totalPaidAmount`, `totalPendingAmount` (plus the existing `totalGstAmount` and CGST/SGST/UGST/IGST split, overdue count/amount). Cancelled orders are counted in `cancelledOrdersCount` and left out of money totals.
Per order, `totalAmount`, `paidAmount`, `pendingAmount`, `totalGstAmount` were already returned.
Optimization: payments for all orders are now loaded with one query instead of one query per order.


---

# Latest update: Buy Price Total

- New computed product column `buyPriceTotal` = remaining stock x buy price (`storePrice`), cost of the stock on hand. Not stored in DB (calculated like `totalAmount`).
- `GET /api/products/summary` now also returns `totalBuyPriceAmount` (sum of all products' `buyPriceTotal`).


---

# Latest update: Shipment cost + employee, edit-order fix

## Shipment
- New fields on `shipments`: `shipmentCost` (Double, cost to send the parcel, default 0, cannot be negative) and `employeeName` (employee who shipped the parcel). Sent in `POST /createShipment`.
- `trackingLink` is no longer sent by the frontend; backend fills it (and `companyName`) from the selected "Shipped By" courier record.
- `GET /getShipmentsSummary` returns `totalShipments`, `cancelledShipments`, `totalShipmentCost` (cancelled shipments excluded from the cost).

## Edit order bug
`PUT /updateOrder/{id}`: adding a product that is already in the order used to fail with "Duplicate product in order". Now lines of the same product are merged (quantities added, discount = quantity-weighted average). Stale duplicate lines of old orders are removed so totals stay correct.

---

# New changes (Order search, PDF / CSV downloads)

## 1. Order search + backend filter
`GET /getAllOrders` now accepts optional query params (none = every order, same as before):

| param | meaning |
|---|---|
| `search` | text matched (contains, case-insensitive) against order id, customer name, phone, email, city |
| `status` | `PENDING`, `CONFIRMED`, `SHIPPED`, `DELIVERED`, `CANCELLED` |
| `paymentMethod` | `CASH`, `UPI`, `CREDIT_CARD`, `DEBIT_CARD`, `NET_BANKING` (method of the newest received payment) |
| `fromDate`, `toDate` | order date range, `yyyy-MM-dd`, inclusive |

Implemented once in `OrderService.filterOrders(...)` (JPA `Specification`, `OrderRepository` now extends `JpaSpecificationExecutor`) and used by both the list and the export.
`GET /getAllShipments` accepts `search`, `status`, `fromDate`, `toDate` the same way (`ShipmentService.filterShipments`).

## 2. Download as PDF / CSV (filter-aware)
| endpoint | filters |
|---|---|
| `GET /exportOrders?format=pdf\|csv` | same as `/getAllOrders` |
| `GET /exportShipments?format=pdf\|csv` | same as `/getAllShipments` |
| `GET /api/products/export?format=pdf\|csv` | same as `GET /api/products` (`productName`, `productTypeId`, `brandId`, `vendorId`, `unitTypeId`) |

* The endpoints run the same filter as the list, so only the filtered rows are downloaded.
* CSV and PDF are rendered from one `ExportTable` (`util/ExportTable`, `CsvExportUtil`, `PdfTableExportUtil`), so both files always hold the same rows.
* CSV: UTF-8 with BOM (opens correctly in Excel), formula-injection safe, data rows only.
* PDF: landscape A4 (OpenPDF, already used for invoices) with title, generated time, active filters, page numbers and a totals row (cancelled orders / shipments excluded from money totals, as on screen). Amounts are shown as `Rs` because the built-in PDF font has no rupee glyph.
* `Content-Disposition` is exposed in CORS (`SecurityConfig`) so the browser can use the server's file name.
* Invalid `format`, status or date range -> 400 with a message (shown as a toast in the UI).


---

# Vendor Bills page (per-vendor payable summary)

New endpoints on `VendorBillController` (no new tables):
- `GET /api/vendor-bills/summary` — one row per vendor: `vendorName`, `totalAmount` (to pay), `paidAmount`, `remainingAmount`, `paymentStatus` (PAID / PARTIAL / UNPAID), `paidInInstallments`, `installmentCount`.
- `GET /api/vendor-bills/vendor/{vendorId}/detail` — vendor summary + every bill + every installment (`installmentNo`, `amountPaid`, `balanceAfter`, date, method, notes).

New files: `DTO/VendorBillSummaryDTO`, `DTO/VendorBillDetailDTO`, `service/VendorBillSummaryService`, `serviceimpl/VendorBillSummaryServiceImpl`.
Changed: `VendorPaymentHistoryRepository` (added `findByVendorBillIdIn`), `VendorBillController`.


---

# New changes (Dashboard pending amount, Sub categories, Shipment tracking + receipt, Installments)

Postman: `InventoryManagementSystem.postman_collection.json` (folders 0-8, ids/dates are filled automatically).

## 1. Dashboard - pending amount of orders
* `GET /dashboard/summary` now also returns `pendingOrders` (+ `customerInstallments`, `vendorInstallments`).
* `GET /dashboard/pending-orders` returns only the pending block:
  `totalPendingAmount`, `pendingOrdersCount`, `unpaidOrdersCount`, `partiallyPaidOrdersCount`,
  `overduePendingAmount`, `overdueOrdersCount`, `pendingToday`. Cancelled orders are ignored.

## 2. Sub categories (dynamic, linked to a product type)
* New table `product_sub_categories` (name unique per product type, soft delete).
* `POST /createSubCategory`, `PUT /updateSubCategory/{id}`, `GET /getAllSubCategories`,
  `GET /getSubCategoriesByProductType/{productTypeId}`, `GET /getSubCategoryById/{id}`,
  `DELETE /deleteSubCategory/{id}`, `GET /getProductTypesWithSubCategories` (for cascading dropdowns).
* Product has an optional `subCategory` (`{"subCategory": {"id": 3}}`); it must belong to the product's type.
  `GET /api/products` and `/api/products/export` accept `subCategoryId`.
* A sub category used by products cannot be deleted; a product type with sub categories cannot be deleted.

## 3. Shipment tracking id
* `trackingId` is generated automatically when it is not sent: `TRK` + `yyMMdd` + 6 characters, e.g. `TRK261011-K7QX4M`.
  A tracking id sent manually is still accepted if it is unique.
* The shipment JSON now contains `orderId` (it was always empty in lists before).

## 4. Shipment receipt
* `GET /api/v1/shipments/{shipmentId}/receipt` (PDF, inline) and `...?download=true` (attachment).

## 5. Installments
Two independent features that share the same rules. A plan always covers the amount that is STILL unpaid.
Schedule = AUTO (`numberOfInstallments`, `firstDueDate`, `frequency`) or CUSTOM (`installments: [{dueAmount, dueDate}]`).

Customer pays the store (`/api/order-installments`)
* `POST /order/{orderId}` create plan, `GET /order/{orderId}`, `DELETE /order/{orderId}` (only while nothing is paid)
* `POST /{installmentId}/pay` pays one installment. It is stored as a normal payment of the order (new columns
  `payments.order_installment_id` and `payments.transaction_reference`), so invoice, order paid/pending and the dashboard stay right.
* `GET /due?overdueOnly=&days=` follow-up list, `GET /summary`.
* While a plan exists: plain `POST /createPayment` and `PUT /updateOrder/{id}` are refused (remove the plan first).

Store pays a vendor (`/api/vendor-bill-installments`) - same endpoints on `/bill/{vendorBillId}`.
* Payments are stored in `vendor_payment_history` (new column `installment_id`), so the bill's paid / due / status stay right.
* While a plan exists: plain `POST /api/vendor-bills/{id}/payments` and `/mark-paid` are refused.

New tables / columns are created automatically only if `spring.jpa.hibernate.ddl-auto=update` is used; otherwise add:
`order_installments`, `vendor_bill_installments`, `product_sub_categories`, `products.sub_category_id`,
`payments.order_installment_id`, `payments.transaction_reference`, `vendor_payment_history.installment_id`.

## Razorpay (planned, not built)
Online payments can later plug into the same two entry points: after a verified Razorpay payment / webhook call
`OrderInstallmentService.payInstallment(...)` or `PaymentService.createPayment(...)` with `transactionReference`
= Razorpay payment id. Only a `RAZORPAY` value in `PaymentMethod` and an order-creation + webhook endpoint would be new.
