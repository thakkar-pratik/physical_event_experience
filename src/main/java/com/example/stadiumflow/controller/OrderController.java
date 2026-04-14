package com.example.stadiumflow.controller;

import com.example.stadiumflow.domain.ConcessionOrder;
import com.example.stadiumflow.repository.OrderRepository;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderRepository orderRepository;

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @PostMapping
    public String placeOrder(@Valid @RequestBody ConcessionOrder order) {
        orderRepository.save(order);
        System.out.println("✅ BACKEND SAVED NEW DB RECORD: " + order.getItemMenu() + " | Price: $" + order.getPaidPrice() + " | OfflineMesh: " + order.isOfflineMesh());
        return "{\"status\": \"success\", \"message\": \"Order Saved\"}";
    }
}
