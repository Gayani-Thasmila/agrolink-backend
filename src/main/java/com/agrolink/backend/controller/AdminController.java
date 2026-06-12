package com.agrolink.backend.controller;

import com.agrolink.backend.dto.AdminAuthResponse;
import com.agrolink.backend.dto.AdminDashboardResponse;
import com.agrolink.backend.dto.LoginRequest;
import com.agrolink.backend.dto.UpdateOrderStatusRequest;
import com.agrolink.backend.model.Order;
import com.agrolink.backend.model.Product;
import com.agrolink.backend.service.AdminTokenService;
import com.agrolink.backend.service.OrderService;
import com.agrolink.backend.service.ProductService;
import com.agrolink.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private AdminTokenService adminTokenService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @PostMapping("/login")
    public ResponseEntity<AdminAuthResponse> login(@RequestBody LoginRequest request) {
        AdminAuthResponse response = userService.adminLogin(request);
        HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping({"/dashboard", "/stats"})
    public ResponseEntity<?> getDashboard(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            adminTokenService.requireAdmin(authorizationHeader);
            AdminDashboardResponse response = orderService.getDashboardStats(productService.countProducts(), userService.countUsers());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }

    @GetMapping("/products")
    public ResponseEntity<?> getProducts(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            adminTokenService.requireAdmin(authorizationHeader);
            return ResponseEntity.ok(productService.getAllProducts());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }

    @PostMapping("/products")
    public ResponseEntity<?> addProduct(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody Product product
    ) {
        try {
            adminTokenService.requireAdmin(authorizationHeader);
            return ResponseEntity.status(HttpStatus.CREATED).body(productService.save(product));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<?> updateProduct(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable int id,
            @RequestBody Product product
    ) {
        try {
            adminTokenService.requireAdmin(authorizationHeader);
            return ResponseEntity.ok(productService.update(id, product));
        } catch (IllegalArgumentException ex) {
            HttpStatus status = "Product not found".equals(ex.getMessage()) ? HttpStatus.NOT_FOUND : HttpStatus.UNAUTHORIZED;
            return ResponseEntity.status(status).body(ex.getMessage());
        }
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable int id
    ) {
        try {
            adminTokenService.requireAdmin(authorizationHeader);
            productService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            HttpStatus status = "Product not found".equals(ex.getMessage()) ? HttpStatus.NOT_FOUND : HttpStatus.UNAUTHORIZED;
            return ResponseEntity.status(status).body(ex.getMessage());
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            adminTokenService.requireAdmin(authorizationHeader);
            List<Order> orders = orderService.getAllOrders();
            return ResponseEntity.ok(orders);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }


    @GetMapping("/users")
    public ResponseEntity<?> getUsers(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            adminTokenService.requireAdmin(authorizationHeader);
            return ResponseEntity.ok(userService.getAllUsers());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }

    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable int id,
            @RequestBody UpdateOrderStatusRequest request
    ) {
        try {
            adminTokenService.requireAdmin(authorizationHeader);
            return ResponseEntity.ok(orderService.updateOrderStatus(id, request.getStatus()));
        } catch (IllegalArgumentException ex) {
            HttpStatus status = "Order not found".equals(ex.getMessage()) ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(ex.getMessage());
        }
    }
}
