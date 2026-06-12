package com.agrolink.backend.repository;

import com.agrolink.backend.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Optional<Payment> findTopByOrderIdOrderByIdDesc(int orderId);
}
