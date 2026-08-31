package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.entity.InventoryOrder;
import com.InventoryManagementSystem.entity.InventoryOrderItem;
import com.InventoryManagementSystem.entity.InventoryProduct;
import com.InventoryManagementSystem.repository.OrderItemRepository;
import com.InventoryManagementSystem.repository.OrderRepository;
import com.InventoryManagementSystem.repository.ProductRepository;
import com.InventoryManagementSystem.service.OrderItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderItemServiceImpl(OrderItemRepository orderItemRepository,
                                OrderRepository orderRepository,
                                ProductRepository productRepository) {
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public InventoryOrderItem createOrderItem(Long orderId, Long productId, Integer quantity) {

        InventoryOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        InventoryProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // 1. Stock Check
        int currentStock = product.getStock() != null ? product.getStock() : 0;
        if (currentStock < quantity) {
            throw new RuntimeException("Insufficient stock for product: " + product.getProductName()
                    + ". Available: " + currentStock + ", Requested: " + quantity);
        }

        // 2. Reduce Stock
        product.setStock(currentStock - quantity);
        productRepository.save(product);

        InventoryOrderItem item = new InventoryOrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(quantity);

        // Calculations run using fetched customerPrice from Product
        item.calculateValues();

        InventoryOrderItem savedItem = orderItemRepository.save(item);

        // Recalculate Order Total
        order.calculateGrandTotal();
        orderRepository.save(order);

        return savedItem;
    }

    @Override
    public List<InventoryOrderItem> getAllOrderItems() {
        return orderItemRepository.findAll();
    }

    @Override
    public InventoryOrderItem getOrderItemById(Long id) {
        return orderItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OrderItem not found"));
    }

    @Override
    public void deleteOrderItem(Long id) {
        orderItemRepository.deleteById(id);
    }
}