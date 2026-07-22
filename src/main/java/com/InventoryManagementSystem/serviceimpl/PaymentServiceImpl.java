package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.entity.InventoryOrder;
import com.InventoryManagementSystem.entity.InventoryPayment;
import com.InventoryManagementSystem.repository.OrderRepository;
import com.InventoryManagementSystem.repository.PaymentRepository;
import com.InventoryManagementSystem.service.PaymentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public InventoryPayment createPayment(Long orderId, InventoryPayment payment) {

        InventoryOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        payment.setOrder(order);

        return paymentRepository.save(payment);
    }

    @Override
    public List<InventoryPayment> getAllPayments()
    {
        return paymentRepository.findAll();
    }

    @Override
    public InventoryPayment getPaymentById(Long id)
    {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    @Override
    public void deletePayment(Long id)
    {
        paymentRepository.deleteById(id);
    }
}