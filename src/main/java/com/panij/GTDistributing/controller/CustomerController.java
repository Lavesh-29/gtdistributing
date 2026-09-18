package com.panij.GTDistributing.controller;

import com.panij.GTDistributing.model.Customer;
import com.panij.GTDistributing.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;


    // =====================================================
    // VALIDATE CUSTOMER
    // POST:
    // http://localhost:8080/customer/validateCustomer
    // =====================================================
    @PostMapping("/validateCustomer")
    @ResponseBody
    public ResponseEntity<Map<String, String>> validateCustomer(
            @RequestBody Map<String, String> request) {

        String password = request.get("password");

        Map<String, String> response = new HashMap<>();

        // Check if password was supplied
        if (password == null || password.trim().isEmpty()) {

            response.put("error", "Password is required");

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }


        // Find customer
        Optional<Customer> customerOptional =
                customerService.findCustomerByPassword(password);


        // Customer found
        if (customerOptional.isPresent()) {

            Customer customer = customerOptional.get();

            response.put(
                    "customerName",
                    customer.getName()
            );

            response.put(
                    "accountNo",
                    String.valueOf(customer.getCustNo())
            );

            response.put(
                    "password",
                    password
            );

            return ResponseEntity.ok(response);
        }


        // Customer not found
        response.put("error", "Invalid password");

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }
}