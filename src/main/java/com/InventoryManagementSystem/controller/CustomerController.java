package com.InventoryManagementSystem.controller;

import com.InventoryManagementSystem.DTO.CustomerRequestDTO;
import com.InventoryManagementSystem.DTO.CustomerResponseDTO;
import com.InventoryManagementSystem.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "https://pjsofttech.com")
@RestController
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping("/createCustomer")
    public CustomerResponseDTO createCustomer(@RequestBody CustomerRequestDTO dto)
    {
        return customerService.saveCustomer(dto);
    }

    @GetMapping("/getAllCustomers")
    public List<CustomerResponseDTO> getAllCustomers()
    {
        return customerService.getAllCustomers();
    }

    @GetMapping("/getCustomerById/{id}")
    public CustomerResponseDTO getCustomerById(@PathVariable Long id)
    {
        return customerService.getCustomerById(id);
    }

    @PutMapping("/updateCustomer/{id}")
    public CustomerResponseDTO updateCustomer(@PathVariable Long id,
                                              @RequestBody CustomerRequestDTO dto)
    {
        return customerService.updateCustomer(id, dto);
    }

    @DeleteMapping("/deleteCustomer/{id}")
    public String deleteCustomer(@PathVariable Long id)
    {
        customerService.deleteCustomer(id);
        return "Customer ID " + id + " deleted successfully";
    }
}