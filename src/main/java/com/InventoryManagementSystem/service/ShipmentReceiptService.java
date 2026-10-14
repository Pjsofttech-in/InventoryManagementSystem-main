package com.InventoryManagementSystem.service;

/** Printable receipt (PDF) of a shipment: tracking id, courier, customer, parcel contents and shipping charge. */
public interface ShipmentReceiptService {

    byte[] generateReceipt(Long shipmentId);
}
