package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.DTO.InstallmentDueDTO;
import com.InventoryManagementSystem.DTO.InstallmentPaymentDTO;
import com.InventoryManagementSystem.DTO.InstallmentPaymentRequestDTO;
import com.InventoryManagementSystem.DTO.InstallmentPlanRequestDTO;
import com.InventoryManagementSystem.DTO.InstallmentPlanResponseDTO;
import com.InventoryManagementSystem.DTO.InstallmentResponseDTO;
import com.InventoryManagementSystem.DTO.InstallmentSummaryDTO;
import com.InventoryManagementSystem.DTO.VendorBillPaymentRequest;
import com.InventoryManagementSystem.Enum.InstallmentStatus;
import com.InventoryManagementSystem.entity.InventoryVendor;
import com.InventoryManagementSystem.entity.VendorBill;
import com.InventoryManagementSystem.entity.VendorBillInstallment;
import com.InventoryManagementSystem.entity.VendorPaymentHistory;
import com.InventoryManagementSystem.repository.VendorBillInstallmentRepository;
import com.InventoryManagementSystem.repository.VendorPaymentHistoryRepository;
import com.InventoryManagementSystem.service.VendorBillInstallmentService;
import com.InventoryManagementSystem.service.VendorBillPaymentService;
import com.InventoryManagementSystem.util.InstallmentUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VendorBillInstallmentServiceImpl implements VendorBillInstallmentService {

    private static final String TYPE = "VENDOR_BILL";

    private final VendorBillInstallmentRepository installmentRepository;
    private final VendorPaymentHistoryRepository paymentRepository;
    private final VendorBillPaymentService vendorBillPaymentService;

    public VendorBillInstallmentServiceImpl(VendorBillInstallmentRepository installmentRepository,
                                            VendorPaymentHistoryRepository paymentRepository,
                                            VendorBillPaymentService vendorBillPaymentService) {
        this.installmentRepository = installmentRepository;
        this.paymentRepository = paymentRepository;
        this.vendorBillPaymentService = vendorBillPaymentService;
    }

    // ------------------------------------------------------------------ plan

    @Override
    @Transactional
    public InstallmentPlanResponseDTO createPlan(Long vendorBillId, InstallmentPlanRequestDTO request) {
        VendorBill bill = vendorBillPaymentService.getVendorBill(vendorBillId);   // also checks "not deleted"
        if (installmentRepository.existsByVendorBillId(vendorBillId)) {
            throw new RuntimeException("Vendor bill " + vendorBillId + " already has an installment plan. Remove it first to create a new one");
        }

        List<VendorBillInstallment> saved = new ArrayList<>();
        int number = 1;
        for (InstallmentUtil.Row row : InstallmentUtil.buildSchedule(request, bill.getDueAmount())) {
            VendorBillInstallment installment = new VendorBillInstallment();
            installment.setVendorBill(bill);
            installment.setInstallmentNumber(number++);
            installment.setDueAmount(row.amount());
            installment.setDueDate(row.dueDate());
            installment.setStatus(InstallmentStatus.PENDING);
            saved.add(installmentRepository.save(installment));
        }
        return toPlan(bill, saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InstallmentPlanResponseDTO getPlan(Long vendorBillId) {
        VendorBill bill = vendorBillPaymentService.getVendorBill(vendorBillId);
        List<VendorBillInstallment> installments = installmentRepository.findByVendorBillIdOrderByInstallmentNumberAsc(vendorBillId);
        if (installments.isEmpty()) {
            throw new RuntimeException("Vendor bill " + vendorBillId + " has no installment plan");
        }
        return toPlan(bill, installments);
    }

    @Override
    @Transactional
    public void deletePlan(Long vendorBillId) {
        List<VendorBillInstallment> installments = installmentRepository.findByVendorBillIdOrderByInstallmentNumberAsc(vendorBillId);
        if (installments.isEmpty()) {
            throw new RuntimeException("Vendor bill " + vendorBillId + " has no installment plan");
        }
        if (!paymentRepository.findByInstallmentIdIn(ids(installments)).isEmpty()) {
            throw new RuntimeException("This plan already has payments, so it cannot be removed");
        }
        installmentRepository.deleteByVendorBillId(vendorBillId);
    }

    // ------------------------------------------------------------------ payment

    @Override
    @Transactional
    public InstallmentPlanResponseDTO payInstallment(Long installmentId, InstallmentPaymentRequestDTO request) {
        if (request == null || request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new RuntimeException("Payment amount must be greater than 0");
        }
        VendorBillInstallment installment = installmentRepository.findById(installmentId)
                .orElseThrow(() -> new RuntimeException("Installment not found with ID: " + installmentId));

        BigDecimal amount = InstallmentUtil.money(request.getAmount());
        BigDecimal pending = installment.getDueAmount().subtract(paidFor(List.of(installmentId)).getOrDefault(installmentId, BigDecimal.ZERO));
        if (pending.signum() <= 0) {
            throw new RuntimeException("Installment " + installment.getInstallmentNumber() + " is already fully paid");
        }
        if (amount.compareTo(pending) > 0) {
            throw new RuntimeException("Payment amount cannot be greater than the pending amount of this installment: " + pending);
        }

        VendorBillPaymentRequest payment = new VendorBillPaymentRequest();
        payment.setAmountPaid(amount);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setTransactionReference(request.getTransactionReference());
        payment.setNotes(request.getNotes() != null && !request.getNotes().isBlank()
                ? request.getNotes() : "Installment " + installment.getInstallmentNumber());
        if (request.getPaymentDate() != null) {
            payment.setPaymentDate(request.getPaymentDate().atStartOfDay());
        }

        // Normal vendor payment path: checks the bill's due amount and updates paid / due / status of the bill.
        vendorBillPaymentService.addPayment(installment.getVendorBill().getId(), payment, installment);

        installment.setStatus(InstallmentUtil.statusFor(
                paidFor(List.of(installmentId)).getOrDefault(installmentId, BigDecimal.ZERO), installment.getDueAmount()));
        installmentRepository.save(installment);

        VendorBill bill = installment.getVendorBill();
        return toPlan(bill, installmentRepository.findByVendorBillIdOrderByInstallmentNumberAsc(bill.getId()));
    }

    // ------------------------------------------------------------------ follow-up + dashboard

    @Override
    @Transactional(readOnly = true)
    public List<InstallmentDueDTO> getDueInstallments(boolean overdueOnly, int days) {
        LocalDate until = overdueOnly ? LocalDate.now().minusDays(1) : LocalDate.now().plusDays(Math.max(0, days));
        List<VendorBillInstallment> open = installmentRepository.findOpenDueUntil(InstallmentStatus.PAID, until);
        Map<Long, BigDecimal> paid = paidFor(ids(open));

        List<InstallmentDueDTO> result = new ArrayList<>();
        for (VendorBillInstallment i : open) {
            BigDecimal paidAmount = paid.getOrDefault(i.getId(), BigDecimal.ZERO);
            BigDecimal pending = i.getDueAmount().subtract(paidAmount);
            if (pending.signum() <= 0) continue;
            InstallmentStatus status = InstallmentUtil.statusFor(paidAmount, i.getDueAmount());
            boolean overdue = InstallmentUtil.isOverdue(status, i.getDueDate());
            InventoryVendor vendor = i.getVendorBill().getVendor();
            result.add(new InstallmentDueDTO(i.getId(), TYPE, i.getVendorBill().getId(), i.getVendorBill().getBillNumber(),
                    vendor == null ? null : vendor.getVendorName(),
                    vendor == null ? null : vendor.getPhone(),
                    i.getInstallmentNumber(), i.getDueAmount(), paidAmount, pending, i.getDueDate(), status, overdue,
                    overdue ? ChronoUnit.DAYS.between(i.getDueDate(), LocalDate.now()) : 0));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public InstallmentSummaryDTO getSummary() {
        List<VendorBillInstallment> open = installmentRepository.findOpenDueUntil(InstallmentStatus.PAID, LocalDate.of(9999, 12, 31));
        Map<Long, BigDecimal> paid = paidFor(ids(open));
        List<InstallmentSummaryCalculator.Item> items = new ArrayList<>();
        for (VendorBillInstallment i : open) {
            items.add(new InstallmentSummaryCalculator.Item(i.getDueAmount().subtract(paid.getOrDefault(i.getId(), BigDecimal.ZERO)), i.getDueDate()));
        }
        return InstallmentSummaryCalculator.summarize(items);
    }

    // ------------------------------------------------------------------ helpers

    private List<Long> ids(Collection<VendorBillInstallment> installments) {
        return installments.stream().map(VendorBillInstallment::getId).toList();
    }

    /** installmentId -> money paid against it (one query for all). */
    private Map<Long, BigDecimal> paidFor(Collection<Long> installmentIds) {
        Map<Long, BigDecimal> paid = new HashMap<>();
        if (installmentIds.isEmpty()) return paid;
        for (VendorPaymentHistory p : paymentRepository.findByInstallmentIdIn(installmentIds)) {
            paid.merge(p.getInstallment().getId(), InstallmentUtil.money(p.getAmountPaid()), BigDecimal::add);
        }
        return paid;
    }

    private InstallmentPlanResponseDTO toPlan(VendorBill bill, List<VendorBillInstallment> installments) {
        Map<Long, List<VendorPaymentHistory>> paymentsByInstallment = new HashMap<>();
        if (!installments.isEmpty()) {
            for (VendorPaymentHistory p : paymentRepository.findByInstallmentIdIn(ids(installments))) {
                paymentsByInstallment.computeIfAbsent(p.getInstallment().getId(), k -> new ArrayList<>()).add(p);
            }
        }

        List<InstallmentResponseDTO> rows = new ArrayList<>();
        BigDecimal planAmount = BigDecimal.ZERO, paidTotal = BigDecimal.ZERO, overdueAmount = BigDecimal.ZERO;
        int paidCount = 0;
        LocalDate nextDue = null;

        for (VendorBillInstallment i : installments) {
            List<VendorPaymentHistory> history = new ArrayList<>(paymentsByInstallment.getOrDefault(i.getId(), List.of()));
            history.sort(Comparator.comparing(VendorPaymentHistory::getPaymentDate, Comparator.nullsLast(Comparator.naturalOrder())));

            List<InstallmentPaymentDTO> payments = new ArrayList<>();
            BigDecimal paid = BigDecimal.ZERO;
            for (VendorPaymentHistory p : history) {
                BigDecimal amount = InstallmentUtil.money(p.getAmountPaid());
                paid = paid.add(amount);
                payments.add(new InstallmentPaymentDTO(p.getId(), amount,
                        p.getPaymentDate() == null ? null : p.getPaymentDate().toLocalDate(),
                        p.getPaymentMethod(), p.getTransactionReference(), p.getNotes()));
            }

            InstallmentStatus status = InstallmentUtil.statusFor(paid, i.getDueAmount());
            BigDecimal pending = i.getDueAmount().subtract(paid).max(BigDecimal.ZERO);
            boolean overdue = InstallmentUtil.isOverdue(status, i.getDueDate());

            planAmount = planAmount.add(i.getDueAmount());
            paidTotal = paidTotal.add(paid.min(i.getDueAmount()));
            if (status == InstallmentStatus.PAID) {
                paidCount++;
            } else if (nextDue == null) {
                nextDue = i.getDueDate();
            }
            if (overdue) overdueAmount = overdueAmount.add(pending);
            rows.add(new InstallmentResponseDTO(i.getId(), i.getInstallmentNumber(), i.getDueAmount(), paid, pending,
                    i.getDueDate(), status, overdue, payments));
        }

        InventoryVendor vendor = bill.getVendor();
        return new InstallmentPlanResponseDTO(TYPE, bill.getId(), vendor == null ? null : vendor.getVendorName(),
                planAmount, paidTotal, planAmount.subtract(paidTotal), rows.size(), paidCount, overdueAmount, nextDue, rows);
    }
}
