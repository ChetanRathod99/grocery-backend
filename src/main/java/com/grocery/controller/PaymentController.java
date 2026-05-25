package com.grocery.controller;

import com.grocery.dto.PaymentVerifyRequest;
import com.grocery.entity.Payment;
import com.grocery.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/razorpay/order/{orderId}")
    public Map<String, Object> create(Authentication auth, @PathVariable Long orderId) throws Exception {
        return paymentService.createOrder(orderId, auth.getName());
    }

    @PostMapping("/cod/order/{orderId}")
    public Payment createCashOnDelivery(Authentication auth, @PathVariable Long orderId) {
        return paymentService.createCashOnDelivery(orderId, auth.getName());
    }

    @PostMapping("/razorpay/verify")
    public Payment verify(Authentication auth, @Valid @RequestBody PaymentVerifyRequest request) throws Exception {
        return paymentService.verify(request, auth.getName());
    }

    @GetMapping("/{paymentId}/invoice")
    public Map<String, Object> invoice(Authentication auth, @PathVariable Long paymentId) {
        return paymentService.invoice(paymentId, auth.getName());
    }

    @PatchMapping("/cod/{paymentId}/paid")
    @PreAuthorize("hasRole('ADMIN')")
    public Payment markCashOnDeliveryPaid(@PathVariable Long paymentId) {
        return paymentService.markCashOnDeliveryPaid(paymentId);
    }
}
