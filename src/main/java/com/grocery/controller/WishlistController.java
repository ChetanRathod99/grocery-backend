package com.grocery.controller;

import com.grocery.entity.WishlistItem;
import com.grocery.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {
    private final WishlistService wishlistService;

    @GetMapping
    public List<WishlistItem> list(Authentication auth) {
        return wishlistService.list(auth.getName());
    }

    @PostMapping("/{productId}")
    public WishlistItem add(Authentication auth, @PathVariable Long productId) {
        return wishlistService.add(auth.getName(), productId);
    }

    @DeleteMapping("/{productId}")
    public void remove(Authentication auth, @PathVariable Long productId) {
        wishlistService.remove(auth.getName(), productId);
    }
}
