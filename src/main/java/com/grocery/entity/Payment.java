package com.grocery.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(optional = false)
    @JsonIgnore
    private Order order;
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod = PaymentMethod.RAZORPAY;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
    private String invoiceNumber;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;
    private BigDecimal amount;
    private LocalDateTime paidAt;
    private LocalDateTime invoiceGeneratedAt;
}
