package com.InventoryManagementSystem.DTO;

import com.InventoryManagementSystem.Enum.InstallmentFrequency;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * Request to put an order (customer installments) or a vendor bill (vendor installments) on an installment plan.
 * The plan always covers the amount that is STILL UNPAID at that moment.
 * <p>
 * Two ways to describe the schedule:
 * <ul>
 *   <li>AUTO   - numberOfInstallments + firstDueDate (+ frequency, default MONTHLY): equal parts, the last one takes the rounding.</li>
 *   <li>CUSTOM - installments: [{dueAmount, dueDate}, ...]; the amounts must add up to the unpaid amount.</li>
 * </ul>
 * When "installments" is sent it wins over the AUTO fields.
 */
@Getter
@Setter
public class InstallmentPlanRequestDTO {
    private Integer numberOfInstallments;
    private LocalDate firstDueDate;
    private InstallmentFrequency frequency;
    private List<InstallmentScheduleItemDTO> installments;
}
