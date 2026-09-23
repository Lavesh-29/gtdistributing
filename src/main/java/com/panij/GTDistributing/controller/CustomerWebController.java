package com.panij.GTDistributing.controller;

import com.panij.GTDistributing.model.Customer;
import com.panij.GTDistributing.service.CustomerService;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/customers")
public class CustomerWebController {

    @Autowired
    private CustomerService customerService;

    // Customer List
    @GetMapping
    public String customerList(Model model) {

        model.addAttribute(
                "customers",
                customerService.getAllCustomers()
        );

        return "Admin/customerList";
    }

    // Show Add Customer Page
    @GetMapping("/add")
    public String showAddCustomerForm(
            @RequestParam(value = "success", required = false) Boolean success,
            Model model) {

        model.addAttribute("customer", new Customer());

        if (Boolean.TRUE.equals(success)) {
            model.addAttribute(
                    "successMessage",
                    "Customer added successfully!"
            );
        }

        return "Admin/addCustomer";
    }

    // Add Customer
    @PostMapping("/add")
    public String addCustomer(
            @ModelAttribute("customer") Customer customer,
            Model model) {

        try {

            customerService.addCustomer(customer);

            return "redirect:/customers/add?success=true";

        } catch (Exception e) {

            model.addAttribute(
                    "errorMessage",
                    "Error adding customer: " + e.getMessage()
            );

            return "Admin/addCustomer";
        }
    }

    // Show Edit Customer Page
    @GetMapping("/edit/{custNo}")
    public String showEditCustomerForm(
            @PathVariable Integer custNo,
            Model model) {

        Optional<Customer> customer =
                customerService.getCustomerById(custNo);

        if (customer.isEmpty()) {
            return "redirect:/customers";
        }

        model.addAttribute(
                "customer",
                customer.get()
        );

        return "Admin/editCustomer";
    }

    // Update Customer
    @PostMapping("/edit")
    public String updateCustomer(
            @ModelAttribute("customer") Customer customer) {

        customerService.updateCustomer(
                customer.getCustNo(),
                customer
        );

        return "redirect:/customers?updated=true";
    }

    // Delete Customer
    @GetMapping("/delete/{custNo}")
    public String deleteCustomer(
            @PathVariable Integer custNo) {

        customerService.deleteCustomer(custNo);

        return "redirect:/customers?deleted=true";
    }
}

