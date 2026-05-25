package com.grocery.controller;

import com.grocery.entity.*;
import com.grocery.repository.*;
import com.grocery.service.OrderService;
import com.grocery.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    private final UserRepository users;
    private final ProductRepository products;
    private final OrderService orders;
    private final PaymentService payments;
    private final CategoryRepository categories;

    @GetMapping("/analytics")
    public Map<String, Object> analytics() {
        return Map.of(
                "users", users.count(),
                "products", products.count(),
                "lowInventoryProducts", products.findByStockLessThanEqualAndActiveTrue(10).size(),
                "categories", categories.count(),
                "orders", orders.all().size(),
                "revenue", orders.revenue()
        );
    }

    @GetMapping("/orders")
    public Object allOrders(@RequestParam(required = false) String q, @RequestParam(required = false) OrderStatus status) {
        return orders.search(q, status);
    }

    @PatchMapping("/orders/{id}/status")
    public Order updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        return orders.updateStatus(id, status);
    }

    @PatchMapping("/orders/{id}/cancel")
    public Order cancelOrder(@PathVariable Long id) {
        return orders.adminCancel(id);
    }

    @PatchMapping("/payments/{id}/status")
    public Payment updatePaymentStatus(@PathVariable Long id, @RequestParam PaymentStatus status) {
        return payments.updateStatus(id, status);
    }

    @GetMapping("/users")
    public Object allUsers() {
        return users.findAll();
    }

    @GetMapping("/products/low-stock")
    public Object lowStockProducts(@RequestParam(defaultValue = "10") int threshold) {
        return products.findByStockLessThanEqualAndActiveTrue(threshold);
    }
}
