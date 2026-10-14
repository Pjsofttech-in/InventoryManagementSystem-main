package com.InventoryManagementSystem.util;

import com.InventoryManagementSystem.DTO.InstallmentPlanRequestDTO;
import com.InventoryManagementSystem.DTO.InstallmentScheduleItemDTO;
import com.InventoryManagementSystem.Enum.InstallmentFrequency;
import com.InventoryManagementSystem.Enum.InstallmentStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Schedule building / validation and status rules shared by customer and vendor installments. */
public final class InstallmentUtil {

    public static final int MIN_INSTALLMENTS = 2;
    public static final int MAX_INSTALLMENTS = 60;

    private InstallmentUtil() {}

    /** One scheduled installment (amount + due date). */
    public record Row(BigDecimal amount, LocalDate dueDate) {}

    public static BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal money(Double value) {
        return money(value == null ? BigDecimal.ZERO : BigDecimal.valueOf(value));
    }

    public static BigDecimal money(double value) {
        return money(BigDecimal.valueOf(value));
    }

    /**
     * Builds and validates the schedule for a plan that has to cover exactly {@code planAmount}.
     * Custom rows win over the automatic fields.
     */
    public static List<Row> buildSchedule(InstallmentPlanRequestDTO request, BigDecimal planAmount) {
        if (request == null) {
            throw new RuntimeException("Installment plan details are required");
        }
        BigDecimal total = money(planAmount);
        if (total.signum() <= 0) {
            throw new RuntimeException("Nothing is pending, so an installment plan is not needed");
        }
        List<Row> rows = request.getInstallments() != null && !request.getInstallments().isEmpty()
                ? customRows(request.getInstallments())
                : automaticRows(request, total);

        if (rows.size() < MIN_INSTALLMENTS || rows.size() > MAX_INSTALLMENTS) {
            throw new RuntimeException("Number of installments must be between "
                    + MIN_INSTALLMENTS + " and " + MAX_INSTALLMENTS);
        }
        BigDecimal sum = rows.stream().map(Row::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.compareTo(total) != 0) {
            throw new RuntimeException("Installments add up to " + sum + " but the pending amount is " + total);
        }
        LocalDate previous = null;
        for (Row row : rows) {
            if (row.amount().signum() <= 0) {
                throw new RuntimeException("Every installment amount must be greater than 0");
            }
            if (row.dueDate() == null) {
                throw new RuntimeException("Every installment needs a due date");
            }
            if (previous != null && !row.dueDate().isAfter(previous)) {
                throw new RuntimeException("Installment due dates must be in increasing order");
            }
            previous = row.dueDate();
        }
        if (rows.get(0).dueDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("First installment due date cannot be in the past");
        }
        return rows;
    }

    private static List<Row> customRows(List<InstallmentScheduleItemDTO> items) {
        List<Row> rows = new ArrayList<>();
        for (InstallmentScheduleItemDTO item : items) {
            if (item.getDueAmount() == null) {
                throw new RuntimeException("Every installment needs a due amount");
            }
            rows.add(new Row(money(item.getDueAmount()), item.getDueDate()));
        }
        return rows;
    }

    private static List<Row> automaticRows(InstallmentPlanRequestDTO request, BigDecimal total) {
        Integer count = request.getNumberOfInstallments();
        if (count == null || count < MIN_INSTALLMENTS || count > MAX_INSTALLMENTS) {
            throw new RuntimeException("numberOfInstallments must be between "
                    + MIN_INSTALLMENTS + " and " + MAX_INSTALLMENTS + " (or send a custom installments list)");
        }
        if (request.getFirstDueDate() == null) {
            throw new RuntimeException("firstDueDate is required");
        }
        InstallmentFrequency frequency = request.getFrequency() == null
                ? InstallmentFrequency.MONTHLY : request.getFrequency();

        BigDecimal part = total.divide(BigDecimal.valueOf(count), 2, RoundingMode.DOWN);
        List<Row> rows = new ArrayList<>();
        BigDecimal allocated = BigDecimal.ZERO;
        for (int i = 0; i < count; i++) {
            BigDecimal amount = (i == count - 1) ? total.subtract(allocated) : part;   // last one takes the rounding
            allocated = allocated.add(amount);
            rows.add(new Row(amount, frequency.dueDate(request.getFirstDueDate(), i)));
        }
        return rows;
    }

    public static InstallmentStatus statusFor(BigDecimal paid, BigDecimal due) {
        if (paid == null || paid.signum() <= 0) return InstallmentStatus.PENDING;
        return paid.compareTo(due) >= 0 ? InstallmentStatus.PAID : InstallmentStatus.PARTIAL;
    }

    public static boolean isOverdue(InstallmentStatus status, LocalDate dueDate) {
        return status != InstallmentStatus.PAID && dueDate != null && dueDate.isBefore(LocalDate.now());
    }
}
