package com.grocery.service;

import com.grocery.dto.PaymentVerifyRequest;
import com.grocery.entity.*;
import com.grocery.exception.BadRequestException;
import com.grocery.exception.ResourceNotFoundException;
import com.grocery.repository.*;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final OrderRepository orders;
    private final PaymentRepository payments;
    private final EmailService emailService;
    @Value("${razorpay.key.id}") private String keyId;
    @Value("${razorpay.key.secret}") private String keySecret;

    public Map<String, Object> createOrder(Long orderId, String email) throws Exception {
        Order order = orders.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getUser().getEmail().equals(email)) throw new ResourceNotFoundException("Order not found");
        payments.findByOrderId(orderId).ifPresent(existing -> {
            if (existing.getStatus() == PaymentStatus.PAID) throw new BadRequestException("Order is already paid");
        });
        RazorpayClient client = new RazorpayClient(keyId, keySecret);
        JSONObject options = new JSONObject();
        options.put("amount", order.getTotalAmount().multiply(BigDecimal.valueOf(100)).intValue());
        options.put("currency", "INR");
        options.put("receipt", "order_" + order.getId());
        var razorpayOrder = client.orders.create(options);
        Payment payment = payments.findByOrderId(orderId).orElseGet(Payment::new);
        payment.setOrder(order);
        payment.setPaymentMethod(PaymentMethod.RAZORPAY);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setRazorpayOrderId(razorpayOrder.get("id"));
        payment.setRazorpayPaymentId(null);
        payment.setRazorpaySignature(null);
        payment.setAmount(order.getTotalAmount());
        payments.save(payment);
        return Map.of("key", keyId, "razorpayOrderId", payment.getRazorpayOrderId(), "amount", payment.getAmount(), "currency", "INR", "orderId", order.getId());
    }

    public Payment createCashOnDelivery(Long orderId, String email) {
        Order order = orders.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getUser().getEmail().equals(email)) throw new ResourceNotFoundException("Order not found");
        Payment payment = payments.findByOrderId(orderId).orElseGet(Payment::new);
        if (payment.getStatus() == PaymentStatus.PAID) throw new BadRequestException("Order is already paid");
        payment.setOrder(order);
        payment.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmount(order.getTotalAmount());
        payment.setRazorpayOrderId(null);
        payment.setRazorpayPaymentId(null);
        payment.setRazorpaySignature(null);
        payment.setInvoiceNumber("GF-COD-" + order.getId() + "-" + System.currentTimeMillis());
        payment.setInvoiceGeneratedAt(LocalDateTime.now());
        Payment saved = payments.save(payment);
        sendConfirmation(order, saved);
        return saved;
    }

    public Payment verify(PaymentVerifyRequest request, String email) throws Exception {
        Payment payment = payments.findByRazorpayOrderId(request.getRazorpayOrderId()).orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        if (!payment.getOrder().getUser().getEmail().equals(email)) throw new ResourceNotFoundException("Payment not found");
        if (payment.getPaymentMethod() != PaymentMethod.RAZORPAY) throw new BadRequestException("Payment is not a Razorpay payment");
        JSONObject attributes = new JSONObject();
        attributes.put("razorpay_order_id", request.getRazorpayOrderId());
        attributes.put("razorpay_payment_id", request.getRazorpayPaymentId());
        attributes.put("razorpay_signature", request.getRazorpaySignature());
        if (!Utils.verifyPaymentSignature(attributes, keySecret)) throw new BadRequestException("Payment signature verification failed");
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        payment.setInvoiceNumber("GF-" + payment.getOrder().getId() + "-" + System.currentTimeMillis());
        payment.setInvoiceGeneratedAt(LocalDateTime.now());
        Payment saved = payments.save(payment);
        sendConfirmation(saved.getOrder(), saved);
        return saved;
    }

    public Map<String, Object> invoice(Long paymentId, String email) {
        Payment payment = payments.findById(paymentId).orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        if (!payment.getOrder().getUser().getEmail().equals(email)) throw new ResourceNotFoundException("Payment not found");
        if (payment.getStatus() != PaymentStatus.PAID && payment.getPaymentMethod() != PaymentMethod.CASH_ON_DELIVERY) {
            throw new BadRequestException("Invoice is available after successful payment");
        }
        return Map.ofEntries(
                Map.entry("invoiceNumber", payment.getInvoiceNumber()),
                Map.entry("generatedAt", payment.getInvoiceGeneratedAt()),
                Map.entry("orderNumber", payment.getOrder().getOrderNumber()),
                Map.entry("orderId", payment.getOrder().getId()),
                Map.entry("subtotal", payment.getOrder().getSubtotal()),
                Map.entry("gstAmount", payment.getOrder().getGstAmount()),
                Map.entry("deliveryCharge", payment.getOrder().getDeliveryCharge()),
                Map.entry("totalAmount", payment.getAmount()),
                Map.entry("paymentMethod", payment.getPaymentMethod()),
                Map.entry("paymentStatus", payment.getStatus()),
                Map.entry("customer", payment.getOrder().getUser().getName()),
                Map.entry("items", payment.getOrder().getItems())
        );
    }

    public Payment markCashOnDeliveryPaid(Long paymentId) {
        Payment payment = payments.findById(paymentId).orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        if (payment.getPaymentMethod() != PaymentMethod.CASH_ON_DELIVERY) throw new BadRequestException("Payment is not a COD payment");
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        if (payment.getInvoiceNumber() == null) {
            payment.setInvoiceNumber("GF-COD-" + payment.getOrder().getId() + "-" + System.currentTimeMillis());
            payment.setInvoiceGeneratedAt(LocalDateTime.now());
        }
        return payments.save(payment);
    }

    public Payment updateStatus(Long paymentId, PaymentStatus status) {
        Payment payment = payments.findById(paymentId).orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        payment.setStatus(status);
        if (status == PaymentStatus.PAID && payment.getPaidAt() == null) payment.setPaidAt(LocalDateTime.now());
        return payments.save(payment);
    }

    private void sendConfirmation(Order order, Payment payment) {
        try {
            emailService.sendOrderConfirmation(order.getUser().getEmail(), order, payment);
        } catch (Exception ignored) {
            // Order/payment persistence must not fail because SMTP is temporarily unavailable.
        }
    }
}
