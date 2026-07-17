package com.panij.GTDistributing.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

@Controller
public class LoginController {

    @GetMapping("/emplogin")
    public String loginPage() {
        return "emplogin";
    }
    @PostMapping("/emplogin")
    public String doLogin(@RequestParam String employeeName,
                          HttpSession session) {

        session.setAttribute("employeeName", employeeName);

        return "redirect:/warehouse/orders";
    }
}