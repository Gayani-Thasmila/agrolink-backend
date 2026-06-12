package com.agrolink.backend.service;

import com.agrolink.backend.model.Product;
import com.agrolink.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repo;

    public List<Product> getAllProducts() {
        return repo.findAll();
    }

    public long countProducts() {
        return repo.count();
    }

    public Product save(Product p) {
        return repo.save(p);
    }

    public Product update(int id, Product product) {
        Product existing = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setStock(product.getStock());
        existing.setImageUrl(product.getImageUrl());
        existing.setCategory(product.getCategory());

        return repo.save(existing);
    }

    public void delete(int id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("Product not found");
        }

        repo.deleteById(id);
    }
}
