package com.agrolink.backend.dto;

import com.agrolink.backend.model.User;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class CreateOrderRequest {

    private Double totalPrice;
    private String status;
    private Date orderDate;
    private Integer userId;
    private User user;
    private List<CreateOrderItemRequest> orderItems;

    public Integer resolveUserId() {
        if (userId != null) {
            return userId;
        }
        return user != null ? user.getId() : null;
    }
}
