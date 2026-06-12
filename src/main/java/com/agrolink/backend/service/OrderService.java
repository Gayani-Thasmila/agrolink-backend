package com.agrolink.backend.service;

import com.agrolink.backend.dto.AdminDashboardResponse;
import com.agrolink.backend.dto.CreateOrderItemRequest;
import com.agrolink.backend.dto.CreateOrderRequest;
import com.agrolink.backend.model.Order;
import com.agrolink.backend.model.OrderItem;
import com.agrolink.backend.model.Product;
import com.agrolink.backend.model.User;
import com.agrolink.backend.repository.OrderRepository;
import com.agrolink.backend.repository.ProductRepository;
import com.agrolink.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private FirebaseMessagingService firebaseMessagingService;

    @Transactional
    public Order saveOrder(CreateOrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Order request is required");
        }

        Integer userId = request.resolveUserId();
        if (userId == null) {
            throw new IllegalArgumentException("User id is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setStatus("PENDING");
        order.setOrderDate(new Date());

        List<OrderItem> orderItems = buildOrderItems(order, request.getOrderItems());
        order.setOrderItems(orderItems);

        if (request.getTotalPrice() != null) {
            order.setTotalPrice(request.getTotalPrice());
        } else {
            double total = orderItems.stream()
                    .mapToDouble(item -> item.getPrice() * item.getQuantity())
                    .sum();
            order.setTotalPrice(total);
        }

        Order savedOrder = orderRepo.save(order);

        if (user.getFcmToken() != null && !user.getFcmToken().isBlank()) {
            firebaseMessagingService.sendNotification(
                    user.getFcmToken(),
                    "Order Placed",
                    "Your order #" + savedOrder.getId() + " has been received."
            );
        }

        return savedOrder;
    }

    @Transactional
    public Order updateOrderStatus(int orderId, String status) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Order status is required");
        }

        String normalizedStatus = status.trim();
        order.setStatus(normalizedStatus);
        Order savedOrder = orderRepo.save(order);

        if (savedOrder.getUser() != null && savedOrder.getUser().getFcmToken() != null) {
            firebaseMessagingService.sendNotification(
                    savedOrder.getUser().getFcmToken(),
                    "Order Update",
                    "Your order #" + savedOrder.getId() + " is now " + normalizedStatus
            );
        }

        return savedOrder;
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByUserId(int userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }
        return orderRepo.findByUserIdOrderByOrderDateDescIdDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepo.findAllByOrderByOrderDateDescIdDesc();
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboardStats(long productCount, long userCount) {
        List<Order> orders = orderRepo.findAll();
        long orderCount = orders.size();
        double totalRevenue = orders.stream()
                .mapToDouble(Order::getTotalPrice)
                .sum();

        return new AdminDashboardResponse(userCount, productCount, orderCount, totalRevenue);
    }

    private List<OrderItem> buildOrderItems(Order order, List<CreateOrderItemRequest> requestedItems) {
        List<OrderItem> orderItems = new ArrayList<>();
        if (requestedItems == null) {
            return orderItems;
        }

        for (CreateOrderItemRequest itemRequest : requestedItems) {
            if (itemRequest == null) {
                continue;
            }

            Integer productId = itemRequest.resolveProductId();
            if (productId == null) {
                throw new IllegalArgumentException("Product id is required for each order item");
            }

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

            int quantity = itemRequest.getQuantity() == null ? 1 : itemRequest.getQuantity();
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero");
            }

            double price = itemRequest.getPrice() == null ? product.getPrice() : itemRequest.getPrice();

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setPrice(price);
            orderItems.add(orderItem);
        }

        return orderItems;
    }
}
