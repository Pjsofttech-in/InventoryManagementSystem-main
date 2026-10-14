package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.DTO.VendorBillDetailDTO;
import com.InventoryManagementSystem.DTO.VendorBillSummaryDTO;
import com.InventoryManagementSystem.entity.InventoryVendor;
import com.InventoryManagementSystem.entity.VendorBill;
import com.InventoryManagementSystem.entity.VendorPaymentHistory;
import com.InventoryManagementSystem.repository.VendorBillRepository;
import com.InventoryManagementSystem.repository.VendorPaymentHistoryRepository;
import com.InventoryManagementSystem.service.VendorBillSummaryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VendorBillSummaryServiceImpl implements VendorBillSummaryService {

    private final VendorBillRepository vendorBillRepository;
    private final VendorPaymentHistoryRepository paymentHistoryRepository;

    public VendorBillSummaryServiceImpl(
            VendorBillRepository vendorBillRepository,
            VendorPaymentHistoryRepository paymentHistoryRepository
    ) {
        this.vendorBillRepository = vendorBillRepository;
        this.paymentHistoryRepository = paymentHistoryRepository;
    }

    // ------------------------------------------------------------------
    // Vendor list (one row per vendor)
    // ------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public List<VendorBillSummaryDTO> getVendorSummaries() {
        List<VendorBill> bills = vendorBillRepository.findByIsDeletedFalseOrderByIdDesc();
        Map<Long, List<VendorPaymentHistory>> paymentsByBill = loadPayments(bills);

        // keep vendors in the order of their most recent bill
        Map<Long, List<VendorBill>> billsByVendor = new LinkedHashMap<>();
        for (VendorBill bill : bills) {
            if (bill.getVendor() == null) continue;
            billsByVendor.computeIfAbsent(bill.getVendor().getId(), k -> new ArrayList<>()).add(bill);
        }

        List<VendorBillSummaryDTO> result = new ArrayList<>();
        for (List<VendorBill> vendorBills : billsByVendor.values()) {
            result.add(buildSummary(vendorBills.get(0).getVendor(), vendorBills, paymentsByBill));
        }
        return result;
    }

    // ------------------------------------------------------------------
    // One vendor in detail
    // ------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public VendorBillDetailDTO getVendorDetail(Long vendorId) {
        List<VendorBill> bills = vendorBillRepository.findByVendorIdAndIsDeletedFalseOrderByIdDesc(vendorId);
        if (bills.isEmpty()) {
            throw new RuntimeException("No vendor bills found for vendor ID: " + vendorId);
        }

        Map<Long, List<VendorPaymentHistory>> paymentsByBill = loadPayments(bills);
        InventoryVendor vendor = bills.get(0).getVendor();

        List<VendorBillDetailDTO.BillDetail> billDetails = bills.stream()
                .map(bill -> buildBillDetail(bill, paymentsByBill.getOrDefault(bill.getId(), List.of())))
                .collect(Collectors.toList());

        return VendorBillDetailDTO.builder()
                .summary(buildSummary(vendor, bills, paymentsByBill))
                .bills(billDetails)
                .build();
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------
    private Map<Long, List<VendorPaymentHistory>> loadPayments(List<VendorBill> bills) {
        if (bills.isEmpty()) return new HashMap<>();
        List<Long> ids = bills.stream().map(VendorBill::getId).collect(Collectors.toList());
        return paymentHistoryRepository.findByVendorBillIdIn(ids).stream()
                .collect(Collectors.groupingBy(p -> p.getVendorBill().getId()));
    }

    private VendorBillSummaryDTO buildSummary(
            InventoryVendor vendor,
            List<VendorBill> bills,
            Map<Long, List<VendorPaymentHistory>> paymentsByBill
    ) {
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal paid = BigDecimal.ZERO;
        int paidBills = 0;
        int installments = 0;
        LocalDateTime lastPayment = null;

        for (VendorBill bill : bills) {
            total = total.add(nvl(bill.getTotalAmount()));
            paid = paid.add(nvl(bill.getPaidAmount()));
            if (nvl(bill.getDueAmount()).signum() <= 0) paidBills++;

            for (VendorPaymentHistory p : paymentsByBill.getOrDefault(bill.getId(), List.of())) {
                installments++;
                if (p.getPaymentDate() != null && (lastPayment == null || p.getPaymentDate().isAfter(lastPayment))) {
                    lastPayment = p.getPaymentDate();
                }
            }
        }

        BigDecimal remaining = total.subtract(paid);
        if (remaining.signum() < 0) remaining = BigDecimal.ZERO;

        String status;
        if (total.signum() > 0 && remaining.signum() == 0) status = "PAID";
        else if (paid.signum() > 0) status = "PARTIAL";
        else status = "UNPAID";

        return VendorBillSummaryDTO.builder()
                .vendorId(vendor.getId())
                .vendorName(vendor.getVendorName())
                .companyName(vendor.getCompanyName())
                .phone(vendor.getPhone())
                .email(vendor.getEmail())
                .totalBills(bills.size())
                .paidBills(paidBills)
                .pendingBills(bills.size() - paidBills)
                .totalAmount(total)
                .paidAmount(paid)
                .remainingAmount(remaining)
                .installmentCount(installments)
                .paidInInstallments("PARTIAL".equals(status))
                .paymentStatus(status)
                .lastPaymentDate(lastPayment)
                .build();
    }

    private VendorBillDetailDTO.BillDetail buildBillDetail(VendorBill bill, List<VendorPaymentHistory> payments) {
        // oldest first so installment #1 is the first payment made
        List<VendorPaymentHistory> ordered = new ArrayList<>(payments);
        ordered.sort(Comparator
                .comparing(VendorPaymentHistory::getPaymentDate, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(VendorPaymentHistory::getId));

        BigDecimal running = nvl(bill.getTotalAmount());
        List<VendorBillDetailDTO.InstallmentDetail> installments = new ArrayList<>();
        int no = 1;
        for (VendorPaymentHistory p : ordered) {
            running = running.subtract(nvl(p.getAmountPaid()));
            BigDecimal balance = running.signum() < 0 ? BigDecimal.ZERO : running;
            installments.add(VendorBillDetailDTO.InstallmentDetail.builder()
                    .id(p.getId())
                    .installmentNo(no++)
                    .amountPaid(p.getAmountPaid())
                    .balanceAfter(balance)
                    .paymentDate(p.getPaymentDate())
                    .paymentMethod(p.getPaymentMethod() != null ? p.getPaymentMethod().name() : null)
                    .transactionReference(p.getTransactionReference())
                    .notes(p.getNotes())
                    .build());
        }

        return VendorBillDetailDTO.BillDetail.builder()
                .id(bill.getId())
                .billNumber(bill.getBillNumber())
                .billDate(bill.getBillDate())
                .dueDate(bill.getDueDate())
                .productName(bill.getPurchaseProductName())
                .batchNumber(bill.getPurchaseBatchNumber())
                .quantity(bill.getPurchaseQuantity())
                .unitPrice(bill.getPurchaseUnitPrice())
                .totalAmount(nvl(bill.getTotalAmount()))
                .paidAmount(nvl(bill.getPaidAmount()))
                .remainingAmount(nvl(bill.getDueAmount()))
                .paymentStatus(bill.getPaymentStatus() != null ? bill.getPaymentStatus().name() : "UNPAID")
                .installments(installments)
                .build();
    }

    private BigDecimal nvl(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
