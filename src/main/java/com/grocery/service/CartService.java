package com.grocery.service;

import com.grocery.dto.CartItemRequest;
import com.grocery.entity.*;
import com.grocery.exception.ResourceNotFoundException;
import com.grocery.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository carts;
    private final CartItemRepository cartItems;
    private final ProductRepository products;
    private final UserRepository users;

    public Cart getCart(String email) {
        User user = users.findByEmail(email).orElseThrow();
        return carts.findByUserId(user.getId()).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(user);
            return carts.save(cart);
        });
    }

    @Transactional
    public Cart add(String email, CartItemRequest request) {
        Cart cart = getCart(email);
        Product product = products.findById(request.getProductId()).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        CartItem item = cartItems.findByCartIdAndProductId(cart.getId(), product.getId()).orElseGet(() -> {
            CartItem created = new CartItem();
            created.setCart(cart);
            created.setProduct(product);
            cart.getItems().add(created);
            return created;
        });
        item.setQuantity(item.getQuantity() + request.getQuantity());
        cartItems.save(item);
        return getCart(email);
    }

    @Transactional
    public Cart update(String email, Long itemId, int quantity) {
        Cart cart = getCart(email);
        CartItem item = cartItems.findById(itemId).orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        if (!item.getCart().getId().equals(cart.getId())) throw new ResourceNotFoundException("Cart item not found");
        item.setQuantity(quantity);
        return cart;
    }

    public void remove(String email, Long itemId) {
        Cart cart = getCart(email);
        CartItem item = cartItems.findById(itemId).orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        if (item.getCart().getId().equals(cart.getId())) cartItems.delete(item);
    }

    public BigDecimal total(Cart cart) {
        return cart.getItems().stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
