package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.DTO.InstallmentDueDTO;
import com.InventoryManagementSystem.DTO.InstallmentPaymentDTO;
import com.InventoryManagementSystem.DTO.InstallmentPaymentRequestDTO;
import com.InventoryManagementSystem.DTO.InstallmentPlanRequestDTO;
import com.InventoryManagementSystem.DTO.InstallmentPlanResponseDTO;
import com.InventoryManagementSystem.DTO.InstallmentResponseDTO;
import com.InventoryManagementSystem.DTO.InstallmentSummaryDTO;
import com.InventoryManagementSystem.Enum.InstallmentStatus;
import com.InventoryManagementSystem.Enum.OrderStatus;
import com.InventoryManagementSystem.Enum.PaymentStatus;
import com.InventoryManagementSystem.entity.BankAccount;
import com.InventoryManagementSystem.entity.Customer;
import com.InventoryManagementSystem.entity.InventoryOrder;
import com.InventoryManagementSystem.entity.InventoryPayment;
import com.InventoryManagementSystem.entity.OrderInstallment;
import com.InventoryManagementSystem.repository.OrderInstallmentRepository;
import com.InventoryManagementSystem.repository.OrderRepository;
import com.InventoryManagementSystem.repository.PaymentRepository;
import com.InventoryManagementSystem.service.OrderInstallmentService;
import com.InventoryManagementSystem.service.PaymentService;
import com.InventoryManagementSystem.util.InstallmentUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderInstallmentServiceImpl implements OrderInstallmentService {

    private static final String TYPE = "ORDER";

    private final OrderRepository orderRepository;
    private final OrderInstallmentRepository installmentRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    public OrderInstallmentServiceImpl(OrderRepository orderRepository,
                                       OrderInstallmentRepository installmentRepository,
                                       PaymentRepository paymentRepository,
                                       PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.installmentRepository = installmentRepository;
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
    }

    // ------------------------------------------------------------------ plan

    @Override
    @Transactional
    public InstallmentPlanResponseDTO createPlan(Long orderId, InstallmentPlanRequestDTO request) {
        InventoryOrder order = findOrder(orderId);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("A cancelled order cannot have an installment plan");
        }
        if (installmentRepository.existsByOrderId(orderId)) {
            throw new RuntimeException("Order " + orderId + " already has an installment plan. Remove it first to create a new one");
        }

        BigDecimal pending = InstallmentUtil.money(order.getTotalAmount())
                .subtract(InstallmentUtil.money(orderPaidAmount(orderId)));

        List<OrderInstallment> saved = new ArrayList<>();
        int number = 1;
        for (InstallmentUtil.Row row : InstallmentUtil.buildSchedule(request, pending)) {
            OrderInstallment installment = new OrderInstallment();
            installment.setOrder(order);
            installment.setInstallmentNumber(number++);
            installment.setDueAmount(row.amount());
            installment.setDueDate(row.dueDate());
            installment.setStatus(InstallmentStatus.PENDING);
            saved.add(installmentRepository.save(installment));
        }
        return toPlan(order, saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InstallmentPlanResponseDTO getPlan(Long orderId) {
        InventoryOrder order = findOrder(orderId);
        List<OrderInstallment> installments = installmentRepository.findByOrderIdOrderByInstallmentNumberAsc(orderId);
        if (installments.isEmpty()) {
            throw new RuntimeException("Order " + orderId + " has no installment plan");
        }
        return toPlan(order, installments);
    }

    @Override
    @Transactional
    public void deletePlan(Long orderId) {
        List<OrderInstallment> installments = installmentRepository.findByOrderIdOrderByInstallmentNumberAsc(orderId);
        if (installments.isEmpty()) {
            throw new RuntimeException("Order " + orderId + " has no installment plan");
        }
        List<Long> ids = installments.stream().map(OrderInstallment::getId).toList();
        if (!paymentRepository.findByInstallmentIdIn(ids).isEmpty()) {
            throw new RuntimeException("This plan already has payments, so it cannot be removed");
        }
        installmentRepository.deleteByOrderId(orderId);
    }

    // ------------------------------------------------------------------ payment

    @Override
    @Transactional
    public InstallmentPlanResponseDTO payInstallment(Long installmentId, InstallmentPaymentRequestDTO request) {
        if (request == null || request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new RuntimeException("Payment amount must be greater than 0");
        }
        OrderInstallment installment = installmentRepository.findById(installmentId)
                .orElseThrow(() -> new RuntimeException("Installment not found with ID: " + installmentId));

        BigDecimal amount = InstallmentUtil.money(request.getAmount());
        BigDecimal pending = installment.getDueAmount().subtract(paidFor(List.of(installmentId)).getOrDefault(installmentId, BigDecimal.ZERO));
        if (pending.signum() <= 0) {
            throw new RuntimeException("Installment " + installment.getInstallmentNumber() + " is already fully paid");
        }
        if (amount.compareTo(pending) > 0) {
            throw new RuntimeException("Payment amount cannot be greater than the pending amount of this installment: " + pending);
        }

        InventoryPayment payment = new InventoryPayment();
        payment.setAmount(amount.doubleValue());
        payment.setPaymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setTransactionReference(request.getTransactionReference() == null || request.getTransactionReference().isBlank()
                ? null : request.getTransactionReference().trim());
        payment.setInstallment(installment);
        if (request.getReceivingAccountId() != null) {
            BankAccount account = new BankAccount();
            account.setId(request.getReceivingAccountId());
            payment.setReceivingAccount(account);   // verified by PaymentService
        }

        // Normal payment path: checks the order total, the bank account and sets PAID / PARTIAL.
        paymentService.createPayment(installment.getOrder().getId(), payment);

        installment.setStatus(InstallmentUtil.statusFor(
                paidFor(List.of(installmentId)).getOrDefault(installmentId, BigDecimal.ZERO), installment.getDueAmount()));
        installmentRepository.save(installment);

        Long orderId = installment.getOrder().getId();
        return toPlan(installment.getOrder(), installmentRepository.findByOrderIdOrderByInstallmentNumberAsc(orderId));
    }

    // ------------------------------------------------------------------ follow-up + dashboard

    @Override
    @Transactional(readOnly = true)
    public List<InstallmentDueDTO> getDueInstallments(boolean overdueOnly, int days) {
        LocalDate until = overdueOnly ? LocalDate.now().minusDays(1) : LocalDate.now().plusDays(Math.max(0, days));
        List<OrderInstallment> open = installmentRepository.findOpenDueUntil(InstallmentStatus.PAID, until);
        Map<Long, BigDecimal> paid = paidFor(ids(open));

        List<InstallmentDueDTO> result = new ArrayList<>();
        for (OrderInstallment i : open) {
            BigDecimal paidAmount = paid.getOrDefault(i.getId(), BigDecimal.ZERO);
            BigDecimal pending = i.getDueAmount().subtract(paidAmount);
            if (pending.signum() <= 0) continue;
            InstallmentStatus status = InstallmentUtil.statusFor(paidAmount, i.getDueAmount());
            boolean overdue = InstallmentUtil.isOverdue(status, i.getDueDate());
            Customer customer = i.getOrder().getCustomer();
            result.add(new InstallmentDueDTO(i.getId(), TYPE, i.getOrder().getId(), "#" + i.getOrder().getId(),
                    customer == null ? null : customer.getCustomerName(),
                    customer == null ? null : customer.getPhone(),
                    i.getInstallmentNumber(), i.getDueAmount(), paidAmount, pending, i.getDueDate(), status, overdue,
                    overdue ? ChronoUnit.DAYS.between(i.getDueDate(), LocalDate.now()) : 0));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public InstallmentSummaryDTO getSummary() {
        List<OrderInstallment> open = installmentRepository.findOpenDueUntil(InstallmentStatus.PAID, LocalDate.of(9999, 12, 31));
        Map<Long, BigDecimal> paid = paidFor(ids(open));
        List<InstallmentSummaryCalculator.Item> items = new ArrayList<>();
        for (OrderInstallment i : open) {
            items.add(new InstallmentSummaryCalculator.Item(i.getDueAmount().subtract(paid.getOrDefault(i.getId(), BigDecimal.ZERO)), i.getDueDate()));
        }
        return InstallmentSummaryCalculator.summarize(items);
    }

    // ------------------------------------------------------------------ helpers

    private InventoryOrder findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
    }

    private double orderPaidAmount(Long orderId) {
        return paymentRepository.findByOrderIdOrderByPaymentDateDesc(orderId).stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.PAID || p.getPaymentStatus() == PaymentStatus.PARTIAL)
                .mapToDouble(p -> p.getAmount() == null ? 0 : p.getAmount())
                .sum();
    }

    private List<Long> ids(Collection<OrderInstallment> installments) {
        return installments.stream().map(OrderInstallment::getId).toList();
    }

    /** installmentId -> money paid against it (one query for all). */
    private Map<Long, BigDecimal> paidFor(Collection<Long> installmentIds) {
        Map<Long, BigDecimal> paid = new HashMap<>();
        if (installmentIds.isEmpty()) return paid;
        for (InventoryPayment p : paymentRepository.findByInstallmentIdIn(installmentIds)) {
            if (p.getPaymentStatus() == PaymentStatus.PAID || p.getPaymentStatus() == PaymentStatus.PARTIAL) {
                paid.merge(p.getInstallment().getId(), InstallmentUtil.money(p.getAmount() == null ? 0 : p.getAmount()), BigDecimal::add);
            }
        }
        return paid;
    }

    private InstallmentPlanResponseDTO toPlan(InventoryOrder order, List<OrderInstallment> installments) {
        Map<Long, List<InventoryPayment>> paymentsByInstallment = new HashMap<>();
        if (!installments.isEmpty()) {
            for (InventoryPayment p : paymentRepository.findByInstallmentIdIn(ids(installments))) {
                paymentsByInstallment.computeIfAbsent(p.getInstallment().getId(), k -> new ArrayList<>()).add(p);
            }
        }

        List<InstallmentResponseDTO> rows = new ArrayList<>();
        BigDecimal planAmount = BigDecimal.ZERO, paidTotal = BigDecimal.ZERO, overdueAmount = BigDecimal.ZERO;
        int paidCount = 0;
        LocalDate nextDue = null;

        for (OrderInstallment i : installments) {
            List<InstallmentPaymentDTO> payments = new ArrayList<>();
            BigDecimal paid = BigDecimal.ZERO;
            for (InventoryPayment p : paymentsByInstallment.getOrDefault(i.getId(), List.of())) {
                if (p.getPaymentStatus() != PaymentStatus.PAID && p.getPaymentStatus() != PaymentStatus.PARTIAL) continue;
                BigDecimal amount = InstallmentUtil.money(p.getAmount() == null ? 0 : p.getAmount());
                paid = paid.add(amount);
                payments.add(new InstallmentPaymentDTO(p.getId(), amount, p.getPaymentDate(), p.getPaymentMethod(),
                        p.getTransactionReference(), null));
            }
            payments.sort((a, b) -> a.paymentDate().compareTo(b.paymentDate()));

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

        Customer customer = order.getCustomer();
        return new InstallmentPlanResponseDTO(TYPE, order.getId(), customer == null ? null : customer.getCustomerName(),
                planAmount, paidTotal, planAmount.subtract(paidTotal), rows.size(), paidCount, overdueAmount, nextDue, rows);
    }
}
