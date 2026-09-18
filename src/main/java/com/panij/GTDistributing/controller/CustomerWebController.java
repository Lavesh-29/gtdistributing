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

    // =====================================================
    // SHOW ADD CUSTOMER PAGE
    // GET: http://localhost:8080/customers/add
    // =====================================================
    @GetMapping("/add")
    public String showAddCustomerForm(Model model) {

        model.addAttribute("customer", new Customer());

        return "Admin/addCustomer";
    }


    // =====================================================
    // ADD / SAVE CUSTOMER
    // POST: http://localhost:8080/customers/add
    // =====================================================
    @PostMapping("/add")
    public String addCustomer(
            @ModelAttribute("customer") Customer customer,
            Model model) {

        try {

            Customer savedCustomer =
                    customerService.addCustomer(customer);

            model.addAttribute(
                    "message",
                    "Customer added successfully with ID: "
                            + savedCustomer.getCustNo()
            );

            model.addAttribute("success", true);

        } catch (Exception e) {

            model.addAttribute(
                    "message",
                    "Error adding customer: " + e.getMessage()
            );

            model.addAttribute("success", false);
        }

        // Success/error page
        return "custadmin/add-customer";
    }


    // =====================================================
    // CUSTOMER LIST
    // GET: http://localhost:8080/customers/list
    // =====================================================
    @GetMapping("/list")
    public String listCustomers(Model model) {

        model.addAttribute(
                "customers",
                customerService.getAllCustomers()
        );

        return "customerList";
    }
}