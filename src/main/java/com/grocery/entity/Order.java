package com.grocery.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String orderNumber;
    @ManyToOne(optional = false)
    @JsonIgnore
    private User user;
    @ManyToOne
    private Address address;
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.CONFIRMED;
    private BigDecimal subtotal;
    private BigDecimal gstAmount;
    private BigDecimal deliveryCharge;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    @OneToOne(mappedBy = "order")
    private Payment payment;
}
