package com.agrolink.backend.service;

import com.agrolink.backend.dto.CreateOrderItemRequest;
import com.agrolink.backend.dto.CreateOrderRequest;
import com.agrolink.backend.model.Order;
import com.agrolink.backend.model.Product;
import com.agrolink.backend.model.User;
import com.agrolink.backend.repository.OrderRepository;
import com.agrolink.backend.repository.ProductRepository;
import com.agrolink.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepo;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void saveOrderAcceptsIdBasedPayload() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(7);
        request.setTotalPrice(2500.0);

        CreateOrderItemRequest item = new CreateOrderItemRequest();
        item.setProductId(12);
        item.setQuantity(2);
        item.setPrice(1250.0);
        request.setOrderItems(List.of(item));

        User user = new User();
        user.setId(7);

        Product product = new Product();
        product.setId(12);
        product.setPrice(1250.0);

        when(userRepository.findById(7)).thenReturn(Optional.of(user));
        when(productRepository.findById(12)).thenReturn(Optional.of(product));
        when(orderRepo.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order saved = orderService.saveOrder(request);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepo).save(orderCaptor.capture());

        Order persisted = orderCaptor.getValue();
        assertThat(saved.getUser().getId()).isEqualTo(7);
        assertThat(saved.getOrderItems()).hasSize(1);
        assertThat(saved.getOrderItems().get(0).getProduct().getId()).isEqualTo(12);
        assertThat(saved.getOrderItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(persisted.getStatus()).isEqualTo("PENDING");
        assertThat(persisted.getOrderDate()).isNotNull();
    }

    @Test
    void getOrdersByUserIdReturnsLatestOrdersFirst() {
        Order first = new Order();
        Order second = new Order();
        List<Order> expected = List.of(first, second);

        when(userRepository.existsById(7)).thenReturn(true);
        when(orderRepo.findByUserIdOrderByOrderDateDescIdDesc(7)).thenReturn(expected);

        List<Order> actual = orderService.getOrdersByUserId(7);

        assertThat(actual).isSameAs(expected);
        verify(orderRepo).findByUserIdOrderByOrderDateDescIdDesc(7);
    }
}
