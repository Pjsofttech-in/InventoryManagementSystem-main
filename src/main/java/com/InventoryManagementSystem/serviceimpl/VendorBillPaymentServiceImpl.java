package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.DTO.VendorBillPaymentRequest;
import com.InventoryManagementSystem.Enum.PaymentStatus;
import com.InventoryManagementSystem.entity.VendorBill;
import com.InventoryManagementSystem.entity.VendorPaymentHistory;
import com.InventoryManagementSystem.repository.VendorBillRepository;
import com.InventoryManagementSystem.repository.VendorPaymentHistoryRepository;
import com.InventoryManagementSystem.service.VendorBillPaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VendorBillPaymentServiceImpl implements VendorBillPaymentService {

    private final VendorBillRepository vendorBillRepository;
    private final VendorPaymentHistoryRepository vendorPaymentHistoryRepository;

    public VendorBillPaymentServiceImpl(
            VendorBillRepository vendorBillRepository,
            VendorPaymentHistoryRepository vendorPaymentHistoryRepository
    ) {
        this.vendorBillRepository = vendorBillRepository;
        this.vendorPaymentHistoryRepository = vendorPaymentHistoryRepository;
    }

    @Override
    @Transactional
    public VendorPaymentHistory addPayment(Long vendorBillId, VendorBillPaymentRequest request) {
        if (request == null || request.getAmountPaid() == null || request.getAmountPaid().signum() <= 0) {
            throw new RuntimeException("Payment amount must be greater than 0");
        }

        VendorBill bill = getActiveBill(vendorBillId);
        BigDecimal due = nvl(bill.getDueAmount());
        BigDecimal amount = request.getAmountPaid();

        if (due.signum() <= 0) {
            throw new RuntimeException("This vendor bill is already fully paid");
        }

        if (amount.compareTo(due) > 0) {
            throw new RuntimeException("Payment amount cannot be greater than due amount: " + due);
        }

        VendorPaymentHistory history = VendorPaymentHistory.builder()
                .vendorBill(bill)
                .amountPaid(amount)
                .paymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDateTime.now())
                .paymentMethod(request.getPaymentMethod())
                .transactionReference(trimToNull(request.getTransactionReference()))
                .notes(trimToNull(request.getNotes()))
                .build();

        VendorPaymentHistory savedHistory = vendorPaymentHistoryRepository.save(history);

        BigDecimal paid = nvl(bill.getPaidAmount()).add(amount);
        BigDecimal newDue = nvl(bill.getTotalAmount()).subtract(paid);
        if (newDue.signum() < 0) newDue = BigDecimal.ZERO;

        bill.setPaidAmount(paid);
        bill.setDueAmount(newDue);
        bill.setPaymentStatus(
                newDue.signum() == 0
                        ? PaymentStatus.PAID
                        : (paid.signum() > 0 ? PaymentStatus.PARTIAL : PaymentStatus.UNPAID)
        );

        vendorBillRepository.save(bill);
        return savedHistory;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorPaymentHistory> getPaymentHistory(Long vendorBillId) {
        getActiveBill(vendorBillId);
        return vendorPaymentHistoryRepository.findByVendorBillIdOrderByPaymentDateDescIdDesc(vendorBillId);
    }

    @Override
    @Transactional(readOnly = true)
    public VendorBill getVendorBill(Long vendorBillId) {
        return getActiveBill(vendorBillId);
    }

    private VendorBill getActiveBill(Long vendorBillId) {
        return vendorBillRepository.findById(vendorBillId)
                .filter(bill -> !Boolean.TRUE.equals(bill.getIsDeleted()))
                .orElseThrow(() -> new RuntimeException("Vendor bill not found with ID: " + vendorBillId));
    }

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
