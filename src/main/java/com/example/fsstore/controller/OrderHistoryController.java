package com.example.fsstore.controller;

import com.example.fsstore.entity.Order;
import com.example.fsstore.entity.User;
import com.example.fsstore.service.OrderService;
import com.example.fsstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class OrderHistoryController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @GetMapping("/order-history")
    public String showOrderHistory(Model model, Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username).orElseThrow();

        List<Order> orders = orderService.getOrdersByUser(user);
        model.addAttribute("orders", orders);

        return "order-history";
    }
}