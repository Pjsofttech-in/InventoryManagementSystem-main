package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.DTO.OrderGstSummaryDTO;
import com.InventoryManagementSystem.DTO.OrderItemRequestDto;
import com.InventoryManagementSystem.DTO.UpdateOrderRequestDto;
import com.InventoryManagementSystem.Enum.OrderStatus;
import com.InventoryManagementSystem.Enum.PaymentMethod;
import com.InventoryManagementSystem.Enum.PaymentStatus;
import com.InventoryManagementSystem.entity.Customer;
import com.InventoryManagementSystem.entity.InventoryOrder;
import com.InventoryManagementSystem.entity.InventoryOrderItem;
import com.InventoryManagementSystem.entity.InventoryProduct;
import com.InventoryManagementSystem.entity.InventoryPayment;
import com.InventoryManagementSystem.entity.ProductStockBatch;
import com.InventoryManagementSystem.entity.StockHistory;
import com.InventoryManagementSystem.repository.CustomerRepository;
import com.InventoryManagementSystem.repository.OrderInstallmentRepository;
import com.InventoryManagementSystem.repository.OrderRepository;
import com.InventoryManagementSystem.repository.PaymentRepository;
import com.InventoryManagementSystem.repository.ProductRepository;
import com.InventoryManagementSystem.repository.ProductStockBatchRepository;
import com.InventoryManagementSystem.repository.StockHistoryRepository;
import com.InventoryManagementSystem.service.OrderService;
import com.InventoryManagementSystem.util.SearchUtil;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final ProductStockBatchRepository stockBatchRepository;
    private final StockHistoryRepository stockHistoryRepository;
    private final PaymentRepository paymentRepository;
    private final OrderInstallmentRepository orderInstallmentRepository;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            ProductStockBatchRepository stockBatchRepository,
            StockHistoryRepository stockHistoryRepository,
            PaymentRepository paymentRepository,
            OrderInstallmentRepository orderInstallmentRepository
    ) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.stockBatchRepository = stockBatchRepository;
        this.stockHistoryRepository = stockHistoryRepository;
        this.paymentRepository = paymentRepository;
        this.orderInstallmentRepository = orderInstallmentRepository;
    }

    @Override
    @Transactional
    public InventoryOrder createOrder(
            InventoryOrder order
    ) {

        /*
         * Customer validation
         */
        if (
                order.getCustomer() == null
                        || order.getCustomer().getId() == null
        ) {
            throw new RuntimeException(
                    "Customer ID is required"
            );
        }

        Customer customer =
                customerRepository
                        .findById(
                                order.getCustomer().getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Customer not found with id: "
                                                + order.getCustomer()
                                                .getId()
                                )
                        );

        order.setCustomer(customer);

        /*
         * Date and time
         */
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDate.now());
        }

        if (order.getOrderTime() == null) {
            order.setOrderTime(LocalTime.now());
        }

        if (order.getStatus() == null) {
            order.setStatus(com.InventoryManagementSystem.Enum.OrderStatus.PENDING);
        }

        if (order.getStatus() == com.InventoryManagementSystem.Enum.OrderStatus.SHIPPED
                || order.getStatus() == com.InventoryManagementSystem.Enum.OrderStatus.DELIVERED
                || order.getStatus() == com.InventoryManagementSystem.Enum.OrderStatus.CANCELLED) {
            throw new RuntimeException("New orders must start in PENDING or CONFIRMED status");
        }

        /*
         * Items validation
         */
        if (
                order.getOrderItems() == null
                        || order.getOrderItems().isEmpty()
        ) {
            throw new RuntimeException(
                    "Order must contain at least one product item"
            );
        }

        /*
         * Initialize totals
         */
        double subtotalAmount = 0.0;
        double totalDiscountAmount = 0.0;
        double taxableAmount = 0.0;
        double totalGstAmount = 0.0;
        double totalAmount = 0.0;

        double totalCgstAmount = 0.0;
        double totalSgstAmount = 0.0;
        double totalUgstAmount = 0.0;
        double totalIgstAmount = 0.0;

        /*
         * Process every order item
         */
        for (InventoryOrderItem item :
                order.getOrderItems()) {

            /*
             * Product validation
             */
            if (
                    item.getProduct() == null
                            || item.getProduct().getId() == null
            ) {
                throw new RuntimeException(
                        "Product ID is required for order items"
                );
            }

            InventoryProduct product =
                    productRepository
                            .findById(
                                    item.getProduct().getId()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Product not found with id: "
                                                    + item.getProduct()
                                                    .getId()
                                    )
                            );

            /*
             * Quantity validation
             */
            int quantity =
                    item.getQuantity() != null
                            ? item.getQuantity()
                            : 0;

            if (quantity <= 0) {
                throw new RuntimeException(
                        "Quantity must be greater than 0 for product: "
                                + product.getProductName()
                );
            }

            /*
             * Stock validation
             */
            int currentStock =
                    product.getStock() != null
                            ? product.getStock()
                            : 0;

            if (currentStock < quantity) {
                throw new RuntimeException(
                        "Insufficient stock for product: "
                                + product.getProductName()
                                + ". Available: "
                                + currentStock
                                + ", Requested: "
                                + quantity
                );
            }

            /*
             * IMPORTANT:
             *
             * Do not trust frontend price.
             *
             * Price comes from database product.
             */
            double customerPrice =
                    product.getCustomerPrice() != null
                            ? product.getCustomerPrice()
                            : 0.0;

            item.setUnitPrice(customerPrice);

            /*
             * Snapshot the product's actual MRP (the real MRP printed on
             * the product) at the time of order, so order screens can show
             * MRP + store price + discount without depending on the
             * product's current data.
             */
            item.setProductMrp(
                    product.getProductMrp() != null
                            ? product.getProductMrp()
                            : 0.0
            );

            /*
             * Copy product GST configuration.
             *
             * This creates a historical snapshot.
             * If product GST changes tomorrow,
             * today's order invoice stays unchanged.
             */
            boolean gstApplicable =
                    Boolean.TRUE.equals(
                            product.getGstApplicable()
                    );

            item.setCgstPercentage(
                    gstApplicable
                            && Boolean.TRUE.equals(
                            product.getApplyCgst()
                    )
                            && product.getCgstPercentage() != null
                            ? product.getCgstPercentage()
                            : 0.0
            );

            item.setSgstPercentage(
                    gstApplicable
                            && Boolean.TRUE.equals(
                            product.getApplySgst()
                    )
                            && product.getSgstPercentage() != null
                            ? product.getSgstPercentage()
                            : 0.0
            );

            item.setUgstPercentage(
                    gstApplicable
                            && Boolean.TRUE.equals(
                            product.getApplyUgst()
                    )
                            && product.getUgstPercentage() != null
                            ? product.getUgstPercentage()
                            : 0.0
            );

            item.setIgstPercentage(
                    gstApplicable
                            && Boolean.TRUE.equals(
                            product.getApplyIgst()
                    )
                            && product.getIgstPercentage() != null
                            ? product.getIgstPercentage()
                            : 0.0
            );

            /*
             * Set relationship before saving.
             */
            item.setProduct(product);
            item.setOrder(order);

            /*
             * Calculate:
             *
             * Unit Price
             * × Quantity
             * = Subtotal
             *
             * - Discount
             * = Taxable Amount
             *
             * + GST
             * = Final Total
             */
            item.calculateValues();

            /*
             * Add item values to order totals.
             */
            subtotalAmount +=
                    item.getSubtotalAmount() != null
                            ? item.getSubtotalAmount()
                            : 0.0;

            totalDiscountAmount +=
                    item.getDiscountAmount() != null
                            ? item.getDiscountAmount()
                            : 0.0;

            taxableAmount +=
                    item.getTaxableAmount() != null
                            ? item.getTaxableAmount()
                            : 0.0;

            totalGstAmount +=
                    item.getTotalGstAmount() != null
                            ? item.getTotalGstAmount()
                            : 0.0;

            totalCgstAmount +=
                    item.getCgstAmount() != null
                            ? item.getCgstAmount()
                            : 0.0;

            totalSgstAmount +=
                    item.getSgstAmount() != null
                            ? item.getSgstAmount()
                            : 0.0;

            totalUgstAmount +=
                    item.getUgstAmount() != null
                            ? item.getUgstAmount()
                            : 0.0;

            totalIgstAmount +=
                    item.getIgstAmount() != null
                            ? item.getIgstAmount()
                            : 0.0;

            totalAmount +=
                    item.getTotalPrice() != null
                            ? item.getTotalPrice()
                            : 0.0;

            /*
             * Reduce product stock.
             */
            int newStock =
                    currentStock - quantity;

            product.setStock(newStock);

            productRepository.save(product);

            /*
             * Consume stock batches FIFO.
             */
            consumeBatches(
                    product,
                    quantity
            );

            /*
             * Save stock history.
             */
            stockHistoryRepository.save(
                    new StockHistory(
                            product,
                            product.getVendor(),
                            currentStock,
                            -quantity,
                            newStock,
                            product.getMrp(),
                            product.getVendorDiscountPercentage(),
                            product.getStorePrice(),
                            product.getProductMrp(),
                            product.getCustomerPrice(),
                            "ORDER_DEDUCTION",
                            "Stock deducted for order"
                    )
            );
        }

        /*
         * Set complete order totals.
         */
        order.setSubtotalAmount(subtotalAmount);

        order.setTotalDiscountAmount(
                totalDiscountAmount
        );

        order.setTaxableAmount(taxableAmount);

        order.setTotalGstAmount(totalGstAmount);

        order.setTotalCgstAmount(totalCgstAmount);

        order.setTotalSgstAmount(totalSgstAmount);

        order.setTotalUgstAmount(totalUgstAmount);

        order.setTotalIgstAmount(totalIgstAmount);

        order.setTotalAmount(totalAmount);

        /*
         * Save order and order items.
         */
        return orderRepository.save(order);
    }

    private void consumeBatches(
            InventoryProduct product,
            int quantity
    ) {

        int remaining = quantity;

        List<ProductStockBatch> batches =
                stockBatchRepository
                        .findByProductIdAndAvailableQuantityGreaterThanAndIsDeletedFalseOrderByReceivedDateAsc(
                                product.getId(),
                                0
                        );

        for (ProductStockBatch batch : batches) {

            if (remaining <= 0) {
                break;
            }

            int available =
                    batch.getAvailableQuantity() != null
                            ? batch.getAvailableQuantity()
                            : 0;

            if (available <= 0) {
                continue;
            }

            int consumed =
                    Math.min(
                            available,
                            remaining
                    );

            batch.setAvailableQuantity(
                    available - consumed
            );

            stockBatchRepository.save(batch);

            remaining -= consumed;
        }

        if (remaining > 0) {
            throw new RuntimeException(
                    "Unable to consume complete stock from batches"
            );
        }
    }

    @Override
    @Transactional
    public InventoryOrder updateOrder(Long id, UpdateOrderRequestDto request) {
        if (request == null) {
            throw new RuntimeException("Order update data is required");
        }

        InventoryOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        // The installment amounts were calculated from the order total, so the order is frozen while a plan exists.
        if (orderInstallmentRepository.existsByOrderId(id)) {
            throw new RuntimeException("This order has an installment plan and cannot be modified. Remove the plan first "
                    + "(DELETE /api/order-installments/order/" + id + ")");
        }

        if (order.getStatus() == com.InventoryManagementSystem.Enum.OrderStatus.CANCELLED) {
            throw new RuntimeException("Cancelled orders cannot be modified");
        }

        if (order.getStatus() == com.InventoryManagementSystem.Enum.OrderStatus.SHIPPED
                || order.getStatus() == com.InventoryManagementSystem.Enum.OrderStatus.DELIVERED) {
            throw new RuntimeException("Shipped or delivered orders cannot be modified");
        }

        if (request.getCustomerId() == null) {
            throw new RuntimeException("Customer ID is required");
        }

        if (request.getOrderDate() == null) {
            throw new RuntimeException("Order date is required");
        }

        if (request.getPaymentDueDate() != null
                && request.getPaymentDueDate().isBefore(request.getOrderDate())) {
            throw new RuntimeException("Payment due date cannot be before order date");
        }

        com.InventoryManagementSystem.Enum.OrderStatus targetStatus =
                request.getStatus() != null
                        ? request.getStatus()
                        : order.getStatus();

        if (targetStatus == null) {
            targetStatus = com.InventoryManagementSystem.Enum.OrderStatus.PENDING;
        }

        // This endpoint owns stock-changing order edits. Shipment/delivery status
        // is intentionally kept out of it so a normal edit cannot silently modify
        // an order that has already left the store.
        if (targetStatus == com.InventoryManagementSystem.Enum.OrderStatus.SHIPPED
                || targetStatus == com.InventoryManagementSystem.Enum.OrderStatus.DELIVERED) {
            throw new RuntimeException("Use the shipment workflow to move an order to SHIPPED or DELIVERED");
        }

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException(
                        "Customer not found with id: " + request.getCustomerId()));

        // Payments already received belong to this order. Never allow an edit
        // that makes the new invoice total smaller than the amount already paid.
        double paidAmount = paymentRepository.findByOrderIdOrderByPaymentDateDesc(id)
                .stream()
                .filter(payment -> payment.getPaymentStatus() == com.InventoryManagementSystem.Enum.PaymentStatus.PAID
                        || payment.getPaymentStatus() == com.InventoryManagementSystem.Enum.PaymentStatus.PARTIAL)
                .mapToDouble(payment -> payment.getAmount() == null ? 0.0 : payment.getAmount())
                .sum();

        // Cancellation is a real stock operation, not just a status flag.
        if (targetStatus == com.InventoryManagementSystem.Enum.OrderStatus.CANCELLED) {
            if (paidAmount > 0.009) {
                throw new RuntimeException("Paid orders cannot be cancelled until the payment/refund is handled");
            }

            restoreOrderStock(order, "Stock restored after order cancellation");
            order.setCustomer(customer);
            order.setOrderDate(request.getOrderDate());
            order.setPaymentDueDate(request.getPaymentDueDate());
            order.setStatus(targetStatus);
            return orderRepository.save(order);
        }

        // Every non-cancelled order must contain at least one line.
        if (request.getOrderItems() == null || request.getOrderItems().isEmpty()) {
            throw new RuntimeException("Order must contain at least one product item");
        }

        // Validate every line, then MERGE lines of the same product into one line
        // (quantities are added, discount is the quantity-weighted average). This lets the
        // user add an already-present product again in "Edit Order" instead of failing.
        Map<Long, OrderItemRequestDto> mergedItems = new LinkedHashMap<>();
        for (OrderItemRequestDto itemRequest : request.getOrderItems()) {
            if (itemRequest == null || itemRequest.getProductId() == null) {
                throw new RuntimeException("Product ID is required for every order item");
            }
            int quantity = itemRequest.getQuantity() == null ? 0 : itemRequest.getQuantity();
            if (quantity <= 0) {
                throw new RuntimeException("Quantity must be greater than 0 for product ID: " + itemRequest.getProductId());
            }
            double discount = itemRequest.getCustomerDiscountPercentage() == null
                    ? 0.0 : itemRequest.getCustomerDiscountPercentage();
            if (discount < 0.0 || discount > 100.0) {
                throw new RuntimeException("Customer discount must be between 0 and 100 for product ID: " + itemRequest.getProductId());
            }

            OrderItemRequestDto merged = mergedItems.get(itemRequest.getProductId());
            if (merged == null) {
                merged = new OrderItemRequestDto();
                merged.setProductId(itemRequest.getProductId());
                merged.setQuantity(quantity);
                merged.setCustomerDiscountPercentage(discount);
                mergedItems.put(itemRequest.getProductId(), merged);
            } else {
                int totalQty = merged.getQuantity() + quantity;
                double weighted = (merged.getCustomerDiscountPercentage() * merged.getQuantity() + discount * quantity) / totalQty;
                merged.setQuantity(totalQty);
                merged.setCustomerDiscountPercentage(Math.round(weighted * 100.0) / 100.0);
            }
        }
        Collection<OrderItemRequestDto> finalItems = mergedItems.values();

        // Keep existing line entities where possible. IMPORTANT: order.getOrderItems()
        // is Hibernate's managed orphan-removal collection. Never replace it with
        // order.setOrderItems(newList) while the order is managed.
        Map<Long, InventoryOrderItem> existingItems = new HashMap<>();
        if (order.getOrderItems() != null) {
            for (InventoryOrderItem oldItem : order.getOrderItems()) {
                if (oldItem.getProduct() != null && oldItem.getProduct().getId() != null) {
                    existingItems.put(oldItem.getProduct().getId(), oldItem);
                }
            }
        }

        // First return the old order quantity to stock. The transaction guarantees
        // that if any later validation fails, the entire update is rolled back.
        restoreOrderStock(order, "Stock restored temporarily for order modification");

        List<InventoryOrderItem> updatedItems = new ArrayList<>();
        double newSubtotal = 0.0;
        double newDiscount = 0.0;
        double newTaxable = 0.0;
        double newGst = 0.0;
        double newCgst = 0.0;
        double newSgst = 0.0;
        double newUgst = 0.0;
        double newIgst = 0.0;
        double newTotal = 0.0;

        for (OrderItemRequestDto itemRequest : finalItems) {
            InventoryProduct product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException(
                            "Product not found with id: " + itemRequest.getProductId()));

            int quantity = itemRequest.getQuantity();
            int availableStock = product.getStock() == null ? 0 : product.getStock();
            if (availableStock < quantity) {
                throw new RuntimeException(
                        "Insufficient stock for product: " + product.getProductName()
                                + ". Available: " + availableStock + ", Requested: " + quantity);
            }

            InventoryOrderItem item = existingItems.get(product.getId());
            if (item == null) {
                item = new InventoryOrderItem();
            }

            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setCustomerDiscountPercentage(
                    itemRequest.getCustomerDiscountPercentage() == null
                            ? 0.0 : itemRequest.getCustomerDiscountPercentage());

            // Re-snapshot current product selling price and GST. This mirrors the
            // create-order rules and keeps the backend authoritative.
            item.setUnitPrice(product.getCustomerPrice() == null ? 0.0 : product.getCustomerPrice());
            item.setProductMrp(product.getProductMrp() == null ? 0.0 : product.getProductMrp());
            setGstSnapshot(item, product);
            item.calculateValues();

            int previousStock = availableStock;
            int newStock = previousStock - quantity;
            product.setStock(newStock);
            productRepository.save(product);
            consumeBatches(product, quantity);

            stockHistoryRepository.save(new StockHistory(
                    product,
                    product.getVendor(),
                    previousStock,
                    -quantity,
                    newStock,
                    product.getMrp(),
                    product.getVendorDiscountPercentage(),
                    product.getStorePrice(),
                    product.getProductMrp(),
                    product.getCustomerPrice(),
                    "ORDER_UPDATE_DEDUCTION",
                    "Stock deducted after order modification"
            ));

            updatedItems.add(item);
            newSubtotal += nz(item.getSubtotalAmount());
            newDiscount += nz(item.getDiscountAmount());
            newTaxable += nz(item.getTaxableAmount());
            newGst += nz(item.getTotalGstAmount());
            newCgst += nz(item.getCgstAmount());
            newSgst += nz(item.getSgstAmount());
            newUgst += nz(item.getUgstAmount());
            newIgst += nz(item.getIgstAmount());
            newTotal += nz(item.getTotalPrice());
        }

        if (paidAmount > newTotal + 0.009) {
            throw new RuntimeException(
                    "Updated order total (" + newTotal + ") is lower than the amount already paid (" + paidAmount + ")"
            );
        }

        order.setCustomer(customer);
        order.setOrderDate(request.getOrderDate());
        order.setPaymentDueDate(request.getPaymentDueDate());
        order.setStatus(targetStatus);

        /*
         * CRITICAL JPA FIX:
         *
         * InventoryOrder.orderItems uses cascade=ALL + orphanRemoval=true.
         * Hibernate wraps that field in a managed PersistentCollection.
         * Replacing it with order.setOrderItems(updatedItems) causes:
         *
         * "A collection with cascade=all-delete-orphan was no longer
         * referenced by the owning entity instance"
         *
         * Therefore we reconcile the existing managed collection in place.
         * Existing line entities are updated, removed lines are removed from
         * the managed collection (so orphanRemoval can delete them), and new
         * lines are added to the same collection.
         */
        List<InventoryOrderItem> managedItems = order.getOrderItems();
        if (managedItems == null) {
            throw new RuntimeException("Order items collection is not initialized");
        }

        // Remove every old line that is not one of the final lines (removed products and
        // also stale duplicate lines of an older order), so orphanRemoval deletes them.
        List<InventoryOrderItem> itemsToRemove = new ArrayList<>();
        for (InventoryOrderItem oldItem : managedItems) {
            boolean kept = false;
            for (InventoryOrderItem updatedItem : updatedItems) {
                if (updatedItem == oldItem) {
                    kept = true;
                    break;
                }
            }
            if (!kept) itemsToRemove.add(oldItem);
        }
        for (InventoryOrderItem itemToRemove : itemsToRemove) {
            itemToRemove.setOrder(null);
            managedItems.remove(itemToRemove);
        }

        // Add/update requested lines without replacing the Hibernate collection.
        for (InventoryOrderItem updatedItem : updatedItems) {
            Long productId = updatedItem.getProduct() != null
                    ? updatedItem.getProduct().getId()
                    : null;

            InventoryOrderItem managedItem = productId == null
                    ? null
                    : existingItems.get(productId);

            if (managedItem != null && managedItems.contains(managedItem)) {
                // The entity is already managed; all fields were calculated above.
                // No collection replacement is necessary.
                managedItem.setOrder(order);
            } else {
                updatedItem.setOrder(order);
                managedItems.add(updatedItem);
            }
        }

        order.setSubtotalAmount(newSubtotal);
        order.setTotalDiscountAmount(newDiscount);
        order.setTaxableAmount(newTaxable);
        order.setTotalGstAmount(newGst);
        order.setTotalCgstAmount(newCgst);
        order.setTotalSgstAmount(newSgst);
        order.setTotalUgstAmount(newUgst);
        order.setTotalIgstAmount(newIgst);
        order.setTotalAmount(newTotal);

        InventoryOrder saved = orderRepository.save(order);
        populatePaymentSummary(saved, paymentRepository.findByOrderIdOrderByPaymentDateDesc(saved.getId()));
        return saved;
    }

    private void setGstSnapshot(InventoryOrderItem item, InventoryProduct product) {
        boolean gstApplicable = Boolean.TRUE.equals(product.getGstApplicable());
        item.setCgstPercentage(gstApplicable && Boolean.TRUE.equals(product.getApplyCgst())
                && product.getCgstPercentage() != null ? product.getCgstPercentage() : 0.0);
        item.setSgstPercentage(gstApplicable && Boolean.TRUE.equals(product.getApplySgst())
                && product.getSgstPercentage() != null ? product.getSgstPercentage() : 0.0);
        item.setUgstPercentage(gstApplicable && Boolean.TRUE.equals(product.getApplyUgst())
                && product.getUgstPercentage() != null ? product.getUgstPercentage() : 0.0);
        item.setIgstPercentage(gstApplicable && Boolean.TRUE.equals(product.getApplyIgst())
                && product.getIgstPercentage() != null ? product.getIgstPercentage() : 0.0);
    }

    private double nz(Double value) {
        return value == null ? 0.0 : value;
    }

    private void restoreOrderStock(InventoryOrder order, String remarks) {
        if (order.getOrderItems() == null) {
            return;
        }

        for (InventoryOrderItem item : order.getOrderItems()) {
            InventoryProduct product = item.getProduct();
            int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
            if (product == null || quantity <= 0) {
                continue;
            }

            int previousStock = product.getStock() == null ? 0 : product.getStock();
            int newStock = previousStock + quantity;
            product.setStock(newStock);
            productRepository.save(product);

            restoreToNewestBatch(product, quantity);

            stockHistoryRepository.save(new StockHistory(
                    product,
                    product.getVendor(),
                    previousStock,
                    quantity,
                    newStock,
                    product.getMrp(),
                    product.getVendorDiscountPercentage(),
                    product.getStorePrice(),
                    product.getProductMrp(),
                    product.getCustomerPrice(),
                    "ORDER_UPDATE_RESTORE",
                    remarks
            ));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryOrder> getAllOrders() {
        List<InventoryOrder> orders = orderRepository.findAll();
        populatePaymentSummaries(orders);
        return orders;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryOrder> filterOrders(String search, String status, String paymentMethod,
                                             LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new RuntimeException("From date cannot be after To date");
        }

        final String term = SearchUtil.clean(search);
        final OrderStatus orderStatus = parseEnum(OrderStatus.class, status, "order status");
        final PaymentMethod method = parseEnum(PaymentMethod.class, paymentMethod, "payment method");

        // Everything stored on the order itself is filtered in the database ...
        Specification<InventoryOrder> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (term != null) {
                String like = SearchUtil.likePattern(term);
                Join<InventoryOrder, Customer> customer = root.join("customer", JoinType.LEFT);
                predicates.add(cb.or(
                        cb.like(root.get("id").as(String.class), like, '\\'),
                        cb.like(cb.lower(customer.get("customerName")), like, '\\'),
                        cb.like(cb.lower(customer.get("phone")), like, '\\'),
                        cb.like(cb.lower(customer.get("email")), like, '\\'),
                        cb.like(cb.lower(customer.get("city")), like, '\\')
                ));
            }
            if (orderStatus != null) {
                predicates.add(cb.equal(root.get("status"), orderStatus));
            }
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.<LocalDate>get("orderDate"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.<LocalDate>get("orderDate"), toDate));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<InventoryOrder> orders = orderRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "id"));
        populatePaymentSummaries(orders);

        // ... the payment method is derived from the payments table (not a column), so it is applied here.
        if (method != null) {
            orders = orders.stream().filter(o -> o.getPaymentMethod() == method).toList();
        }
        return orders;
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String value, String label) {
        if (value == null || value.isBlank()) return null;
        try {
            return Enum.valueOf(type, value.trim().toUpperCase().replace(' ', '_'));
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid " + label + ": " + value);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryOrder getOrderById(Long id) {
        InventoryOrder order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        populatePaymentSummary(order, paymentRepository.findByOrderIdOrderByPaymentDateDesc(id));
        return order;
    }

    /** Loads payments of ALL given orders in one query, then fills paid/pending/status per order. */
    private void populatePaymentSummaries(List<InventoryOrder> orders) {
        if (orders.isEmpty()) return;
        Map<Long, List<InventoryPayment>> paymentsByOrder = new HashMap<>();
        for (InventoryPayment p : paymentRepository.findByOrderIdInOrderByPaymentDateDesc(
                orders.stream().map(InventoryOrder::getId).toList())) {
            paymentsByOrder.computeIfAbsent(p.getOrder().getId(), k -> new ArrayList<>()).add(p);
        }
        orders.forEach(o -> populatePaymentSummary(o, paymentsByOrder.getOrDefault(o.getId(), List.of())));
    }

    private static boolean isReceived(InventoryPayment p) {
        return p.getPaymentStatus() == PaymentStatus.PAID || p.getPaymentStatus() == PaymentStatus.PARTIAL;
    }

    private void populatePaymentSummary(InventoryOrder order, List<InventoryPayment> payments) {
        double paid = 0.0;
        for (InventoryPayment p : payments) {
            if (!isReceived(p)) continue;
            paid += nz(p.getAmount());
            if (order.getPaymentMethod() == null) order.setPaymentMethod(p.getPaymentMethod()); // newest received payment
        }
        double total = nz(order.getTotalAmount());
        paid = Math.min(paid, total);
        double due = Math.max(0.0, total - paid);
        order.setPaidAmount(paid);
        order.setPendingAmount(due);

        if (total <= 0.0 || due <= 0.009) order.setPaymentStatus(PaymentStatus.PAID);
        else if (paid > 0.009) order.setPaymentStatus(PaymentStatus.PARTIAL);
        else order.setPaymentStatus(PaymentStatus.UNPAID);

        // Overdue is recalculated on every fetch, so it always reflects "today".
        order.setPaymentOverdue(false);
        order.setOverdueDays(0L);
        if (order.getPaymentDueDate() != null && due > 0.009) {
            LocalDate today = LocalDate.now();
            if (today.isAfter(order.getPaymentDueDate())) {
                order.setPaymentOverdue(true);
                order.setOverdueDays(ChronoUnit.DAYS.between(order.getPaymentDueDate(), today));
                order.setPaymentStatus(PaymentStatus.OVERDUE);
            }
        }
    }

    /**
     * Grand totals across all orders: total order amount, paid, pending and GST (with split),
     * plus the overdue summary. Cancelled orders are counted separately and excluded from money totals.
     */
    @Override
    @Transactional(readOnly = true)
    public OrderGstSummaryDTO getOrdersGstSummary() {
        List<InventoryOrder> orders = orderRepository.findAll();
        populatePaymentSummaries(orders);

        OrderGstSummaryDTO s = new OrderGstSummaryDTO();
        double amount = 0, paid = 0, pending = 0, gst = 0, cgst = 0, sgst = 0, ugst = 0, igst = 0, overdueAmount = 0;
        int overdueOrders = 0, cancelled = 0;

        for (InventoryOrder o : orders) {
            if (o.getStatus() == OrderStatus.CANCELLED) { cancelled++; continue; }
            amount += nz(o.getTotalAmount());
            paid += nz(o.getPaidAmount());
            pending += nz(o.getPendingAmount());
            gst += nz(o.getTotalGstAmount());
            cgst += nz(o.getTotalCgstAmount());
            sgst += nz(o.getTotalSgstAmount());
            ugst += nz(o.getTotalUgstAmount());
            igst += nz(o.getTotalIgstAmount());
            if (Boolean.TRUE.equals(o.getPaymentOverdue())) {
                overdueOrders++;
                overdueAmount += nz(o.getPendingAmount());
            }
        }

        s.setTotalOrders(orders.size());
        s.setCancelledOrdersCount(cancelled);
        s.setTotalOrderAmount(round2(amount));
        s.setTotalPaidAmount(round2(paid));
        s.setTotalPendingAmount(round2(pending));
        s.setTotalGstAmount(round2(gst));
        s.setTotalCgstAmount(round2(cgst));
        s.setTotalSgstAmount(round2(sgst));
        s.setTotalUgstAmount(round2(ugst));
        s.setTotalIgstAmount(round2(igst));
        s.setOverdueOrdersCount(overdueOrders);
        s.setTotalOverdueAmount(round2(overdueAmount));
        return s;
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        InventoryOrder order = getOrderById(id);

        if (order.getStatus() == com.InventoryManagementSystem.Enum.OrderStatus.SHIPPED
                || order.getStatus() == com.InventoryManagementSystem.Enum.OrderStatus.DELIVERED) {
            throw new RuntimeException("Shipped or delivered orders cannot be deleted");
        }

        List<InventoryPayment> payments = paymentRepository.findByOrderIdOrderByPaymentDateDesc(id);
        boolean hasReceivedPayment = payments.stream()
                .anyMatch(payment -> payment.getAmount() != null && payment.getAmount() > 0.009
                        && (payment.getPaymentStatus() == com.InventoryManagementSystem.Enum.PaymentStatus.PAID
                        || payment.getPaymentStatus() == com.InventoryManagementSystem.Enum.PaymentStatus.PARTIAL));
        if (hasReceivedPayment) {
            throw new RuntimeException("Orders with received payments cannot be deleted; cancel/refund the payment first");
        }

        // A cancelled order has already returned its stock. Do not restore it a
        // second time when the cancelled record itself is deleted.
        if (order.getStatus() != com.InventoryManagementSystem.Enum.OrderStatus.CANCELLED) {
            restoreOrderStock(order, "Stock restored after order deletion");
        }

        orderInstallmentRepository.deleteByOrderId(id);   // unpaid installment plan goes away with the order
        orderRepository.delete(order);
    }

    private void restoreToNewestBatch(
            InventoryProduct product,
            int quantity
    ) {

        int remaining = quantity;

        List<ProductStockBatch> batches =
                stockBatchRepository
                        .findByProductIdAndIsDeletedFalseOrderByReceivedDateAsc(
                                product.getId()
                        );

        for (
                int i = batches.size() - 1;
                i >= 0 && remaining > 0;
                i--
        ) {

            ProductStockBatch batch =
                    batches.get(i);

            int available =
                    batch.getAvailableQuantity() != null
                            ? batch.getAvailableQuantity()
                            : 0;

            int initial =
                    batch.getInitialQuantity() != null
                            ? batch.getInitialQuantity()
                            : available;

            int capacity =
                    Math.max(
                            0,
                            initial - available
                    );

            int restored =
                    Math.min(
                            capacity,
                            remaining
                    );

            batch.setAvailableQuantity(
                    available + restored
            );

            stockBatchRepository.save(batch);

            remaining -= restored;
        }

        if (remaining > 0) {
            throw new RuntimeException(
                    "Unable to restore complete stock to stock batches for product: "
                            + product.getProductName()
            );
        }
    }
}