package com.grocery.service;

import com.grocery.dto.AddressRequest;
import com.grocery.entity.*;
import com.grocery.exception.BadRequestException;
import com.grocery.exception.ResourceNotFoundException;
import com.grocery.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final UserRepository users;
    private final OrderRepository orders;
    private final AddressRepository addresses;
    private final CartService cartService;
    private final CartRepository carts;
    private final ProductRepository products;

    @Transactional
    public Order place(String email, AddressRequest addressRequest) {
        User user = users.findByEmail(email).orElseThrow();
        Cart cart = cartService.getCart(email);
        if (cart.getItems().isEmpty()) throw new BadRequestException("Cart is empty");
        cart.getItems().forEach(cartItem -> {
            Product product = cartItem.getProduct();
            if (!product.isActive()) throw new BadRequestException(product.getName() + " is unavailable");
            if (product.getStock() <= 0) throw new BadRequestException(product.getName() + " is out of stock");
            if (product.getStock() < cartItem.getQuantity()) {
                throw new BadRequestException("Only " + product.getStock() + " unit(s) available for " + product.getName());
            }
        });
        Address address = new Address();
        address.setUser(user);
        address.setFullName(addressRequest.getFullName());
        address.setPhoneNumber(addressRequest.getPhoneNumber());
        address.setLine1(addressRequest.getLine1());
        address.setLine2(addressRequest.getLine2());
        address.setCity(addressRequest.getCity());
        address.setState(addressRequest.getState());
        address.setPincode(addressRequest.getPincode());
        address.setLandmark(addressRequest.getLandmark());
        address.setCountry(addressRequest.getCountry());
        addresses.save(address);
        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(generateOrderNumber());
        order.setAddress(address);
        BigDecimal subtotal = cartService.total(cart);
        BigDecimal gst = subtotal.multiply(BigDecimal.valueOf(0.05));
        BigDecimal delivery = subtotal.compareTo(BigDecimal.valueOf(499)) >= 0 ? BigDecimal.ZERO : BigDecimal.valueOf(40);
        order.setSubtotal(subtotal);
        order.setGstAmount(gst);
        order.setDeliveryCharge(delivery);
        order.setTotalAmount(subtotal.add(gst).add(delivery));
        order.setStatus(OrderStatus.CONFIRMED);
        cart.getItems().forEach(cartItem -> {
            Product product = cartItem.getProduct();
            product.setStock(product.getStock() - cartItem.getQuantity());
            products.save(product);
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(cartItem.getQuantity());
            item.setPrice(product.getPrice());
            order.getItems().add(item);
        });
        Order saved = orders.save(order);
        cart.getItems().clear();
        carts.save(cart);
        return saved;
    }

    public List<Order> history(String email) {
        User user = users.findByEmail(email).orElseThrow();
        return orders.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    public Order get(String email, Long id) {
        User user = users.findByEmail(email).orElseThrow();
        Order order = orders.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getUser().getId().equals(user.getId())) throw new ResourceNotFoundException("Order not found");
        return order;
    }

    public List<Order> all() {
        return orders.findAll();
    }

    public List<Order> search(String q, OrderStatus status) {
        if (status != null) return orders.findByStatusOrderByCreatedAtDesc(status);
        if (q != null && !q.isBlank()) return orders.findByOrderNumberContainingIgnoreCaseOrUserEmailContainingIgnoreCaseOrderByCreatedAtDesc(q, q);
        return all();
    }

    public Order updateStatus(Long id, OrderStatus status) {
        Order order = orders.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setStatus(status);
        order.setUpdatedAt(java.time.LocalDateTime.now());
        return orders.save(order);
    }

    @Transactional
    public Order cancel(String email, Long id) {
        User user = users.findByEmail(email).orElseThrow();
        Order order = orders.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getUser().getId().equals(user.getId())) throw new ResourceNotFoundException("Order not found");
        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.OUT_FOR_DELIVERY || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Order cannot be cancelled after shipping");
        }
        return cancelInternal(order);
    }

    @Transactional
    public Order adminCancel(Long id) {
        Order order = orders.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return cancelInternal(order);
    }

    public BigDecimal revenue() {
        return orders.findAll().stream().map(Order::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Order cancelInternal(Order order) {
        if (order.getStatus() == OrderStatus.CANCELLED) return order;
        order.getItems().forEach(item -> {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            products.save(product);
        });
        if (order.getPayment() != null) {
            Payment payment = order.getPayment();
            payment.setStatus(payment.getStatus() == PaymentStatus.PAID ? PaymentStatus.REFUNDED : PaymentStatus.FAILED);
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(java.time.LocalDateTime.now());
        return orders.save(order);
    }

    private String generateOrderNumber() {
        String value;
        do {
            value = "GR" + (10000 + new java.security.SecureRandom().nextInt(90000));
        } while (orders.existsByOrderNumber(value));
        return value;
    }
}
