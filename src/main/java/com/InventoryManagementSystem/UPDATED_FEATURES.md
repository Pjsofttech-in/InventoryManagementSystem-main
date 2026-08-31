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
