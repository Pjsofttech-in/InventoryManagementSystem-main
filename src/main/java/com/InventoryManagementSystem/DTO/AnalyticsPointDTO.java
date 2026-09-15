package com.InventoryManagementSystem.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AnalyticsPointDTO {
    private String label;
    private BigDecimal revenue;
    private Long unitsSold;
}
