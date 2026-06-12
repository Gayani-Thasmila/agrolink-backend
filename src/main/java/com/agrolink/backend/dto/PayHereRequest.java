package com.agrolink.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayHereRequest {
    @JsonAlias("order_id")
    private Integer orderId;
    private String items;
    private String currency = "LKR";
    @JsonAlias("first_name")
    private String firstName;
    @JsonAlias("last_name")
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String country = "Sri Lanka";
}
