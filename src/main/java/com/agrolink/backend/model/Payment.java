package com.agrolink.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private int id;

    @Column(name = "payment_method")
    private String method;

    @Column(name = "payment_status")
    private String status;

    @Column(name = "payment_date")
    private Date paymentDate;

    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "gateway_payment_id")
    private String gatewayPaymentId;

    @Column(name = "payment_reference")
    private String reference;

    @ManyToOne   // safer than OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    public Payment() {}
}
