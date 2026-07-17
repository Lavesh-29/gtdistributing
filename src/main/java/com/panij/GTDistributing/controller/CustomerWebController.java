package com.panij.GTDistributing.controller;

import com.panij.GTDistributing.model.Customer;
import com.panij.GTDistributing.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/customers")
public class CustomerWebController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/add")
    public String showAddCustomerForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "Admin/addCustomer.html"; // Name of your addCustomer.html file
    }

    @PostMapping("/add")
    public String addCustomer(@ModelAttribute Customer customer, Model model) {
        Customer savedCustomer = customerService.addCustomer(customer);
        model.addAttribute("message", "Customer added successfully with ID: " + savedCustomer.getCustNo());
        return "custadmin/add-customer"; // Name of your customer-success.html file
    }

    @GetMapping("/list")
    public String listCustomers(Model model) {
        model.addAttribute("customers", customerService.getAllCustomers());
        return "customerList"; // Name of your customerList.html file
    }
}