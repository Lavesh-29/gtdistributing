package com.panij.GTDistributing.service;

import com.panij.GTDistributing.repository.CustomerRepository;
import com.panij.GTDistributing.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> getCustomerById(Integer custNo) {
        return customerRepository.findById(custNo);
    }

    // Added to match the controller call: customerService.saveCustomer(...)
    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    // Kept your original addCustomer method as well
    public Customer addCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Integer custNo, Customer updatedCustomer) {
        if (customerRepository.existsById(custNo)) {
            updatedCustomer.setCustNo(custNo);
            return customerRepository.save(updatedCustomer);
        }
        return null; // Or throw an exception
    }

    public void deleteCustomer(Integer custNo) {
        customerRepository.deleteById(custNo);
    }

    public boolean checkCustomerExists(Integer custNo) {
        boolean exists = customerRepository.existsByCustNo(custNo);
        System.out.println("Checking existence for custNo=" + custNo + ": " + exists);
        return exists;
    }

    public Optional<Customer> validateCustomer(Integer custNo, String password) {
        return customerRepository.findByCustNoAndPassword(custNo, password);
    }

    public Optional<Customer> findCustomerByPassword(String password) {
        List<Customer> customers = customerRepository.findByPassword(password);
        return customers.isEmpty() ? Optional.empty() : Optional.of(customers.get(0));
    }
}