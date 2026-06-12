package com.agrolink.backend.dto;

import com.agrolink.backend.model.Product;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderItemRequest {

    private Integer productId;
    private Product product;
    private Integer quantity;
    private Double price;

    public Integer resolveProductId() {
        if (productId != null) {
            return productId;
        }
        return product != null ? product.getId() : null;
    }
}
