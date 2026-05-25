package com.grocery.controller;

import com.grocery.dto.CartItemRequest;
import com.grocery.entity.Cart;
import com.grocery.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping
    public Map<String, Object> get(Authentication auth) {
        Cart cart = cartService.getCart(auth.getName());
        BigDecimal total = cartService.total(cart);
        return Map.of("cart", cart, "total", total);
    }

    @PostMapping
    public Cart add(Authentication auth, @Valid @RequestBody CartItemRequest request) {
        return cartService.add(auth.getName(), request);
    }

    @PutMapping("/{itemId}")
    public Cart update(Authentication auth, @PathVariable Long itemId, @RequestParam int quantity) {
        return cartService.update(auth.getName(), itemId, quantity);
    }

    @DeleteMapping("/{itemId}")
    public void remove(Authentication auth, @PathVariable Long itemId) {
        cartService.remove(auth.getName(), itemId);
    }
}
