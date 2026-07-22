package com.InventoryManagementSystem.service;

import com.InventoryManagementSystem.entity.InventoryPayment;
import java.util.List;

public interface PaymentService {

    InventoryPayment createPayment(Long orderId, InventoryPayment payment);

    List<InventoryPayment> getAllPayments();

    InventoryPayment getPaymentById(Long id);

    void deletePayment(Long id);
}