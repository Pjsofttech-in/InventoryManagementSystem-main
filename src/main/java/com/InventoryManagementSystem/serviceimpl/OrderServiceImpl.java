package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.entity.Customer;
import com.InventoryManagementSystem.entity.InventoryOrder;
import com.InventoryManagementSystem.entity.InventoryOrderItem;
import com.InventoryManagementSystem.entity.InventoryProduct;
import com.InventoryManagementSystem.repository.CustomerRepository;
import com.InventoryManagementSystem.repository.OrderRepository;
import com.InventoryManagementSystem.repository.ProductRepository;
import com.InventoryManagementSystem.service.OrderService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            CustomerRepository customerRepository,
                            ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @Override
    public InventoryOrder createOrder(InventoryOrder order) {


        Customer customer = customerRepository.findById(order.getCustomer().getId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        order.setCustomer(customer);

        double totalAmount = 0.0;


        for (InventoryOrderItem item : order.getOrderItems()) {

            InventoryProduct product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            item.setProduct(product);
            item.setOrder(order);


            double price = product.getPrice() != null ? product.getPrice() : 0.0;
            int qty = item.getQuantity() != null ? item.getQuantity() : 0;

            double gst = 0.0;

            if (Boolean.TRUE.equals(product.getGstApplicable())
                    && product.getGstPercentage() != null) {

                gst = (price * qty * product.getGstPercentage()) / 100;
            }

            double total = (price * qty) + gst;

            item.setPrice(price);
            item.setGstAmount(gst);
            item.setTotalPrice(total);

            totalAmount += total;
        }


        order.setTotalAmount(totalAmount);


        return orderRepository.save(order);
    }

    @Override
    public List<InventoryOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public InventoryOrder getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Override
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}