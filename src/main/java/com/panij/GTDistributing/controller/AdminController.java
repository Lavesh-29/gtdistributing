package com.panij.GTDistributing.controller;

import com.panij.GTDistributing.model.Customer;
import com.panij.GTDistributing.repository.CustomerRepository;
import com.panij.GTDistributing.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/")
public class AdminController {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CustomerService customerService;

    @GetMapping("/customer-admin")
    public String customerAdminPage(Model model) {
        List<Customer> customers = customerRepository.findAll();
        model.addAttribute("customers", customers);
        return "custadmin/customer-admin";
    }

    @GetMapping("/add-customer")
    public String showAddCustomerPage(Model model) {
        model.addAttribute("customer", new Customer()); // Pass an empty customer object if needed
        return "custadmin/add-customer"; // Ensure this matches the correct folder structure
    }

    @GetMapping("/editcustomer/{custNo}")
    public String editCustomerForm(@PathVariable("custNo") Integer custNo, Model model) {
        Optional<Customer> customer = customerRepository.findById(custNo);
        if (customer.isPresent()) {
            model.addAttribute("customer", customer.get());
            return "custadmin/editcustomer"; // Returns editcustomer.html
        } else {
            return "redirect:/custadmin/customer-admin"; // Redirect if customer not found
        }
    }

    @PostMapping("/save-customer")
    public String saveCustomer(@ModelAttribute Customer customer) {
        customerService.addCustomer(customer);
//        customerRepository.save(customer);
        return "redirect:/customer-admin";
    }

    @PostMapping("/update-customer")
    public String updateCustomer(@ModelAttribute Customer updatedCustomer) {
        Optional<Customer> existingCustomer = customerRepository.findById(updatedCustomer.getCustNo());

        if (existingCustomer.isPresent()) {
            Customer customer = existingCustomer.get();

            // Only update other fields, leave the password unchanged
            customer.setName(updatedCustomer.getName());
            customer.setAddress(updatedCustomer.getAddress());
            customer.setPhone(updatedCustomer.getPhone());
            customer.setCity(updatedCustomer.getCity());
            customer.setAddress2(updatedCustomer.getAddress2());
            customer.setCounty(updatedCustomer.getCounty());
            customer.setAltPhone(updatedCustomer.getAltPhone());
            customer.setState(updatedCustomer.getState());
            customer.setZip(updatedCustomer.getZip());
            customer.setName2(updatedCustomer.getName2());
            customerRepository.save(customer);
        }

        return "redirect:/customer-admin"; // Redirect after update
    }


    @DeleteMapping("/delete-customer/{custNo}")
    @ResponseBody
    public ResponseEntity<String> deleteCustomer(@PathVariable("custNo") Integer custNo) {
        if (customerRepository.existsById(custNo)) {
            customerRepository.deleteById(custNo);
            return ResponseEntity.ok("Customer deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found");
        }
    }


}

