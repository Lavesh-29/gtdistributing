package com.panij.GTDistributing.controller;

import com.panij.GTDistributing.model.Customer;
import com.panij.GTDistributing.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping("/validateCustomer")
    public ResponseEntity<Map<String, String>> validateCustomer(@RequestBody Map<String, String> request) {
        String password = request.get("password");

        Optional<Customer> customerOptional = customerService.findCustomerByPassword(password);
        Map<String, String> response = new HashMap<>();

        if (customerOptional.isPresent()) {
            Customer customer = customerOptional.get();
            response.put("customerName", customer.getName());
            response.put("accountNo", String.valueOf(customer.getCustNo()));
            response.put("password", password);
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Invalid password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}