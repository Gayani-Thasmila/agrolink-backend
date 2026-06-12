package com.agrolink.backend.controller;

import com.agrolink.backend.dto.CreateOrderRequest;
import com.agrolink.backend.dto.PayHereCheckoutResponse;
import com.agrolink.backend.dto.PayHereRequest;
import com.agrolink.backend.model.Order;
import com.agrolink.backend.service.OrderService;
import com.agrolink.backend.service.PayHereService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private OrderService service;

    @Autowired
    private PayHereService payHereService;

    @PostMapping("/orders")
    public Order createOrder(@RequestBody CreateOrderRequest request) {
        return service.saveOrder(request);
    }

    @GetMapping("/orders")
    public List<Order> getOrdersByUser(@RequestParam("userId") int userId) {
        return service.getOrdersByUserId(userId);
    }

    @GetMapping("/orders/user/{userId}")
    public List<Order> getOrdersByUserPath(@PathVariable int userId) {
        return service.getOrdersByUserId(userId);
    }

    @GetMapping("/orders/{userId}")
    public List<Order> getOrdersByLegacyPath(@PathVariable int userId) {
        return service.getOrdersByUserId(userId);
    }

    @PostMapping("/payments/payhere/session")
    public PayHereCheckoutResponse createPayHereSession(@RequestBody PayHereRequest request) {
        return payHereService.createCheckoutSession(request);
    }

    @PostMapping(value = "/payments/payhere/notify", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, String>> handlePayHereNotification(@RequestParam MultiValueMap<String, String> formData) {
        Map<String, String> payload = new HashMap<>();
        formData.forEach((key, value) -> payload.put(key, value == null || value.isEmpty() ? null : value.get(0)));
        payHereService.handleNotification(payload);
        return ResponseEntity.ok(Map.of("status", "accepted"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }
}
