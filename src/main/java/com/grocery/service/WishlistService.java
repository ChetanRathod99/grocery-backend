package com.grocery.service;

import com.grocery.entity.*;
import com.grocery.exception.ResourceNotFoundException;
import com.grocery.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {
    private final WishlistRepository wishlist;
    private final UserRepository users;
    private final ProductRepository products;

    public List<WishlistItem> list(String email) {
        User user = users.findByEmail(email).orElseThrow();
        return wishlist.findByUserId(user.getId());
    }

    public WishlistItem add(String email, Long productId) {
        User user = users.findByEmail(email).orElseThrow();
        Product product = products.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return wishlist.findByUserIdAndProductId(user.getId(), productId).orElseGet(() -> {
            WishlistItem item = new WishlistItem();
            item.setUser(user);
            item.setProduct(product);
            return wishlist.save(item);
        });
    }

    public void remove(String email, Long productId) {
        User user = users.findByEmail(email).orElseThrow();
        wishlist.findByUserIdAndProductId(user.getId(), productId).ifPresent(wishlist::delete);
    }
}
