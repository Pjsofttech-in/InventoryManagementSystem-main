package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.DTO.CustomerRequestDTO;
import com.InventoryManagementSystem.DTO.CustomerResponseDTO;
import com.InventoryManagementSystem.entity.Customer;
import com.InventoryManagementSystem.entity.InventoryVendor;
import com.InventoryManagementSystem.Enum.State;
import com.InventoryManagementSystem.repository.CustomerRepository;
import com.InventoryManagementSystem.repository.VendorRepository;
import com.InventoryManagementSystem.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Override
    public CustomerResponseDTO saveCustomer(CustomerRequestDTO dto) {

        if (dto.getVendorId() == null) {
            throw new RuntimeException("Vendor ID must not be null");
        }

        Customer customer = new Customer();

        customer.setCustomerName(dto.getCustomerName());
        customer.setLandmark(dto.getLandmark());
        customer.setCity(dto.getCity());
        customer.setTaluka(dto.getTaluka());
        customer.setPincode(dto.getPincode());
        customer.setEmail(dto.getEmail());
        customer.setPhone(dto.getPhone());

        try {
            customer.setState(State.valueOf(dto.getState().trim().toUpperCase()));
        } catch (Exception e) {
            throw new RuntimeException("Invalid State value");
        }

        InventoryVendor vendor = vendorRepository.findById(dto.getVendorId())
                .orElseThrow(() -> new RuntimeException("Vendor not found with id: " + dto.getVendorId()));

        customer.setVendor(vendor);

        Customer saved = customerRepository.save(customer);

        return mapToResponse(saved);
    }

    @Override
    public List<CustomerResponseDTO> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerResponseDTO getCustomerById(Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        return mapToResponse(customer);
    }


    @Override
    public CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO dto) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        customer.setCustomerName(dto.getCustomerName());
        customer.setLandmark(dto.getLandmark());
        customer.setCity(dto.getCity());
        customer.setTaluka(dto.getTaluka());
        customer.setPincode(dto.getPincode());
        customer.setEmail(dto.getEmail());
        customer.setPhone(dto.getPhone());

        try {
            customer.setState(State.valueOf(dto.getState().trim().toUpperCase()));
        } catch (Exception e) {
            throw new RuntimeException("Invalid State value");
        }

        if (dto.getVendorId() != null) {
            InventoryVendor vendor = vendorRepository.findById(dto.getVendorId())
                    .orElseThrow(() -> new RuntimeException("Vendor not found"));
            customer.setVendor(vendor);
        }

        Customer updated = customerRepository.save(customer);

        return mapToResponse(updated);
    }

    @Override
    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }

    private CustomerResponseDTO mapToResponse(Customer customer) {

        CustomerResponseDTO dto = new CustomerResponseDTO();

        dto.setId(customer.getId());
        dto.setCustomerName(customer.getCustomerName());
        dto.setCity(customer.getCity());
        dto.setState(customer.getState().name());
        dto.setEmail(customer.getEmail());

        if (customer.getVendor() != null) {
            dto.setVendorId(customer.getVendor().getId());
            dto.setVendorName(customer.getVendor().getVendorName());
            dto.setCompanyName(customer.getVendor().getCompanyName());
        }

        return dto;
    }
}