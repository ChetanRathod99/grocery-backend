package com.grocery.repository;

import com.grocery.entity.Order;
import com.grocery.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Order> findByOrderNumberContainingIgnoreCaseOrUserEmailContainingIgnoreCaseOrderByCreatedAtDesc(String orderNumber, String email);
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);
    boolean existsByOrderNumber(String orderNumber);
}
