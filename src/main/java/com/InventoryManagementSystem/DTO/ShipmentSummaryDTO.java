package com.InventoryManagementSystem.DTO;

import lombok.Getter;
import lombok.Setter;

/** Grand totals for the Shipments page. Cancelled shipments are not counted in the cost. */
@Getter
@Setter
public class ShipmentSummaryDTO {
    private int totalShipments;
    private int cancelledShipments;
    private double totalShipmentCost;   // sum of shipmentCost of all non-cancelled shipments
}
