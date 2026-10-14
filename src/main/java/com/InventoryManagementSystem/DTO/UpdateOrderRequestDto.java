package com.InventoryManagementSystem.DTO;

import com.InventoryManagementSystem.Enum.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class UpdateOrderRequestDto {
    private Long customerId;
    private LocalDate orderDate;
    private LocalDate paymentDueDate;
    private OrderStatus status;
    private List<OrderItemRequestDto> orderItems;
}
