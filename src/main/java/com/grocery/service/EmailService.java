package com.grocery.service;

import com.grocery.entity.Order;
import com.grocery.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendOtp(String to, String subject, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText("Your Grocery Fresh OTP is " + otp + ". It expires in 10 minutes.");
        mailSender.send(message);
    }

    public void sendOrderConfirmation(String to, Order order, Payment payment) {
        String products = order.getItems().stream()
                .map(item -> "- " + item.getProduct().getName() + " x " + item.getQuantity() + " = Rs. " + item.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))
                .collect(Collectors.joining("\n"));
        String address = order.getAddress().getFullName() + ", " + order.getAddress().getPhoneNumber() + "\n"
                + order.getAddress().getLine1() + ", " + order.getAddress().getLine2() + "\n"
                + order.getAddress().getCity() + ", " + order.getAddress().getState() + " - " + order.getAddress().getPincode()
                + (order.getAddress().getLandmark() == null ? "" : "\nLandmark: " + order.getAddress().getLandmark());
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Order " + order.getOrderNumber() + " confirmed");
        message.setText("""
                Your Grocery Fresh order has been placed successfully.

                Order ID: %s
                Order Status: %s
                Payment Method: %s
                Payment Status: %s

                Products:
                %s

                Subtotal: Rs. %s
                GST: Rs. %s
                Delivery Charges: Rs. %s
                Total: Rs. %s

                Delivery Address:
                %s
                """.formatted(
                order.getOrderNumber(),
                order.getStatus(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                products,
                order.getSubtotal(),
                order.getGstAmount(),
                order.getDeliveryCharge(),
                order.getTotalAmount(),
                address
        ));
        mailSender.send(message);
    }
}
