# Backend changes

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
