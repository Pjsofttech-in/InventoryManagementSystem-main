package com.InventoryManagementSystem.DTO;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One row of a custom installment schedule. */
@Getter
@Setter
public class InstallmentScheduleItemDTO {
    private BigDecimal dueAmount;
    private LocalDate dueDate;
}
