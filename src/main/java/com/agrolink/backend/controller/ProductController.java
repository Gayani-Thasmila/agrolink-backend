package com.agrolink.backend.controller;

import com.agrolink.backend.model.Product;
import com.agrolink.backend.service.ProductService;
import com.agrolink.backend.util.FirebaseAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private ProductService service;

    @Autowired
    private FirebaseAuthService firebaseAuthService;

    @Value("${app.security.require-auth:true}")
    private boolean requireAuth;

    @GetMapping("/products")
    public List<Product> getProducts(@RequestHeader(value = "Authorization", required = false) String header) {
        if (requireAuth) {
            String token = header == null ? "" : header.replace("Bearer ", "");
            String uid = firebaseAuthService.verifyToken(token);

            if (uid == null) {
                throw new RuntimeException("Unauthorized");
            }
        }

        return service.getAllProducts();
    }
}
