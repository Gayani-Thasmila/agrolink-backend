package com.agrolink.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "payhere")
public class PayHereProperties {
    private boolean sandbox = true;
    private String merchantId;
    private String merchantSecret;
    private String returnUrl = "http://localhost:3000/payment/success";
    private String cancelUrl = "http://localhost:3000/payment/cancel";
    private String notifyUrl = "http://localhost:8080/api/payments/payhere/notify";
}
