package com.grocery.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
@Data
@NoArgsConstructor
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JsonIgnore
    private User user;
    @ManyToOne(optional = false)
    private Product product;
    private int rating;
    @Column(length = 1000)
    private String comment;
    private LocalDateTime createdAt = LocalDateTime.now();
}
