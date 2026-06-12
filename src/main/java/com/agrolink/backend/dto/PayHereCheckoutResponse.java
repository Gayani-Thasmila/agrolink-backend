package com.agrolink.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PayHereCheckoutResponse {
    private final boolean sandbox;
    private final String checkoutUrl;
    private final String merchantId;
    private final String returnUrl;
    private final String cancelUrl;
    private final String notifyUrl;
    private final String orderId;
    private final String items;
    private final String currency;
    private final String amount;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phone;
    private final String address;
    private final String city;
    private final String country;
    private final String hash;

    @JsonProperty("checkout_url")
    public String getCheckoutUrlSnakeCase() {
        return checkoutUrl;
    }

    @JsonProperty("merchant_id")
    public String getMerchantIdSnakeCase() {
        return merchantId;
    }

    @JsonProperty("return_url")
    public String getReturnUrlSnakeCase() {
        return returnUrl;
    }

    @JsonProperty("cancel_url")
    public String getCancelUrlSnakeCase() {
        return cancelUrl;
    }

    @JsonProperty("notify_url")
    public String getNotifyUrlSnakeCase() {
        return notifyUrl;
    }

    @JsonProperty("order_id")
    public String getOrderIdSnakeCase() {
        return orderId;
    }

    @JsonProperty("first_name")
    public String getFirstNameSnakeCase() {
        return firstName;
    }

    @JsonProperty("last_name")
    public String getLastNameSnakeCase() {
        return lastName;
    }
}
