package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.entity.InventoryOrder;
import com.InventoryManagementSystem.entity.InventoryOrderItem;
import com.InventoryManagementSystem.entity.InventoryProduct;
import com.InventoryManagementSystem.repository.OrderItemRepository;
import com.InventoryManagementSystem.repository.OrderRepository;
import com.InventoryManagementSystem.repository.ProductRepository;
import com.InventoryManagementSystem.service.OrderItemService;
import org.springframework.stereotype.Service;

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
    public InventoryOrderItem createOrderItem(Long orderId, Long productId, Integer quantity) {

        InventoryOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        InventoryProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        InventoryOrderItem item = new InventoryOrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(quantity);

        return orderItemRepository.save(item);
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