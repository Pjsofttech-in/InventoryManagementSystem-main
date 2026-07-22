package com.InventoryManagementSystem.service;

import com.InventoryManagementSystem.DTO.CustomerRequestDTO;
import com.InventoryManagementSystem.DTO.CustomerResponseDTO;

import java.util.List;

public interface CustomerService {

    CustomerResponseDTO saveCustomer(CustomerRequestDTO dto);

    List<CustomerResponseDTO> getAllCustomers();

    CustomerResponseDTO getCustomerById(Long id);

    CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO dto);

    void deleteCustomer(Long id);
}