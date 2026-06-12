package com.agrolink.backend.repository;

import com.agrolink.backend.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findByUserIdOrderByOrderDateDescIdDesc(int userId);

    List<Order> findAllByOrderByOrderDateDescIdDesc();
}
