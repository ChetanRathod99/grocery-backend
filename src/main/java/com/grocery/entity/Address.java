package com.grocery.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
public class Address {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JsonIgnore
    private User user;
    private String fullName;
    private String phoneNumber;
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String pincode;
    private String landmark;
    private String country = "India";
}
