package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.entity.Customer;
import com.InventoryManagementSystem.entity.InventoryOrder;
import com.InventoryManagementSystem.entity.InventoryOrderItem;
import com.InventoryManagementSystem.entity.InventoryProduct;
import com.InventoryManagementSystem.entity.InventoryPayment;
import com.InventoryManagementSystem.entity.ProductStockBatch;
import com.InventoryManagementSystem.entity.StockHistory;
import com.InventoryManagementSystem.repository.CustomerRepository;
import com.InventoryManagementSystem.repository.OrderRepository;
import com.InventoryManagementSystem.repository.PaymentRepository;
import com.InventoryManagementSystem.repository.ProductRepository;
import com.InventoryManagementSystem.repository.ProductStockBatchRepository;
import com.InventoryManagementSystem.repository.StockHistoryRepository;
import com.InventoryManagementSystem.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final ProductStockBatchRepository stockBatchRepository;
    private final StockHistoryRepository stockHistoryRepository;
    private final PaymentRepository paymentRepository;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            ProductStockBatchRepository stockBatchRepository,
            StockHistoryRepository stockHistoryRepository,
            PaymentRepository paymentRepository
    ) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.stockBatchRepository = stockBatchRepository;
        this.stockHistoryRepository = stockHistoryRepository;
        this.paymentRepository = paymentRepository;
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
    public List<InventoryOrder> getAllOrders() {
        List<InventoryOrder> orders = orderRepository.findAll();
        orders.forEach(this::populatePaymentSummary);
        return orders;
    }

    @Override
    public InventoryOrder getOrderById(Long id) {
        InventoryOrder order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        populatePaymentSummary(order);
        return order;
    }

    private void populatePaymentSummary(InventoryOrder order) {
        List<InventoryPayment> payments = paymentRepository.findByOrderIdOrderByPaymentDateDesc(order.getId());
        double paid = payments.stream()
                .filter(p -> p.getPaymentStatus() == com.InventoryManagementSystem.Enum.PaymentStatus.PAID || p.getPaymentStatus() == com.InventoryManagementSystem.Enum.PaymentStatus.PARTIAL)
                .mapToDouble(p -> p.getAmount() == null ? 0.0 : p.getAmount()).sum();
        double total = order.getTotalAmount() == null ? 0.0 : order.getTotalAmount();
        paid = Math.min(paid, total);
        double due = Math.max(0.0, total - paid);
        order.setPaidAmount(paid);
        order.setPendingAmount(due);
        if (total <= 0.0 || due <= 0.009) order.setPaymentStatus(com.InventoryManagementSystem.Enum.PaymentStatus.PAID);
        else if (paid > 0.009) order.setPaymentStatus(com.InventoryManagementSystem.Enum.PaymentStatus.PARTIAL);
        else order.setPaymentStatus(com.InventoryManagementSystem.Enum.PaymentStatus.UNPAID);
        payments.stream().filter(p -> p.getPaymentStatus() == com.InventoryManagementSystem.Enum.PaymentStatus.PAID || p.getPaymentStatus() == com.InventoryManagementSystem.Enum.PaymentStatus.PARTIAL).findFirst().ifPresent(p -> order.setPaymentMethod(p.getPaymentMethod()));
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {

        InventoryOrder order =
                getOrderById(id);

        if (order.getOrderItems() != null) {

            for (
                    InventoryOrderItem item :
                    order.getOrderItems()
            ) {

                InventoryProduct product =
                        item.getProduct();

                int quantity =
                        item.getQuantity() != null
                                ? item.getQuantity()
                                : 0;

                if (
                        product != null
                                && quantity > 0
                ) {

                    int previousStock =
                            product.getStock() != null
                                    ? product.getStock()
                                    : 0;

                    int newStock =
                            previousStock + quantity;

                    product.setStock(newStock);

                    productRepository.save(product);

                    restoreToNewestBatch(
                            product,
                            quantity
                    );

                    stockHistoryRepository.save(
                            new StockHistory(
                                    product,
                                    product.getVendor(),
                                    previousStock,
                                    quantity,
                                    newStock,
                                    product.getMrp(),
                                    product.getVendorDiscountPercentage(),
                                    product.getStorePrice(),
                                    product.getCustomerPrice(),
                                    "ORDER_RESTORE",
                                    "Stock restored after order deletion"
                            )
                    );
                }
            }
        }

        orderRepository.delete(order);
    }

    private void restoreToNewestBatch(
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
    }
}