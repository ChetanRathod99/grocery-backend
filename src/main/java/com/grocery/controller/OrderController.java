package com.grocery.controller;

import com.grocery.dto.AddressRequest;
import com.grocery.entity.Order;
import com.grocery.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public Order place(Authentication auth, @Valid @RequestBody AddressRequest address) {
        return orderService.place(auth.getName(), address);
    }

    @GetMapping
    public List<Order> history(Authentication auth) {
        return orderService.history(auth.getName());
    }

    @GetMapping("/{id}")
    public Order track(Authentication auth, @PathVariable Long id) {
        return orderService.get(auth.getName(), id);
    }

    @PatchMapping("/{id}/cancel")
    public Order cancel(Authentication auth, @PathVariable Long id) {
        return orderService.cancel(auth.getName(), id);
    }
}
