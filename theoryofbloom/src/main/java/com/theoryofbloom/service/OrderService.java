package com.theoryofbloom.service;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.theoryofbloom.model.Order;
import com.theoryofbloom.model.OrderItem;
import com.theoryofbloom.model.User;
import com.theoryofbloom.model.dto.CartItem;
import com.theoryofbloom.repository.OrderRepository;
import com.theoryofbloom.repository.ProductRepository;
import com.theoryofbloom.model.Product;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartService cartService;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Transactional
    public Order createOrder(User user, String contactName, String shippingAddress, String contactPhone, String contactEmail, boolean isCod) throws RazorpayException {
        List<CartItem> cartItems = cartService.getItems();
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        BigDecimal totalAmount = cartService.getTotalPrice();
        BigDecimal deliveryFee = new BigDecimal("49.00");
        BigDecimal finalAmount = totalAmount.add(deliveryFee);

        Order order = new Order();
        order.setUser(user);
        order.setDeliveryFee(deliveryFee);
        order.setTotalAmount(finalAmount);
        order.setStatus(isCod ? "PENDING" : "CREATED");
        order.setShippingAddress(shippingAddress);
        order.setContactPhone(contactPhone);
        order.setContactEmail(contactEmail);
        order.setContactName(contactName);

        for (CartItem ci : cartItems) {
            OrderItem item = new OrderItem();
            item.setProduct(ci.getProduct());
            item.setQuantity(ci.getQuantity());
            item.setPrice(ci.getProduct().getPrice());
            order.addOrderItem(item);
            
            if (isCod) {
                Product product = ci.getProduct();
                if (product.getStockQuantity() != null) {
                    int newStock = product.getStockQuantity() - ci.getQuantity();
                    product.setStockQuantity(newStock >= 0 ? newStock : 0);
                    productRepository.save(product);
                }
            }
        }

        // Save order to get DB ID
        order = orderRepository.save(order);

        // If not COD, create Razorpay Order for payment gateway
        if (!isCod) {
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            JSONObject orderRequest = new JSONObject();
            // Razorpay amount is in sub-units (e.g. paise), multiply by 100
            orderRequest.put("amount", finalAmount.multiply(new BigDecimal(100)).intValue());
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "tob_rcpt_" + order.getId());

            com.razorpay.Order razorpayOrder = razorpay.orders.create(orderRequest);
            order.setRazorpayOrderId(razorpayOrder.get("id"));
            orderRepository.save(order);
        }

        return order;
    }

    @Transactional
    public void verifyAndConfirmPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        Optional<Order> orderOpt = orderRepository.findByRazorpayOrderId(razorpayOrderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            // NOTE: In production, verify the Razorpay Signature using Utils.verifyPaymentSignature
            // For Phase 2 test mocking, we automatically confirm. 
            order.setRazorpayPaymentId(razorpayPaymentId);
            order.setRazorpaySignature(razorpaySignature);
            order.setStatus("PAID");
            
            // Empty the active session cart
            cartService.clearCart();
            
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                if (product.getStockQuantity() != null) {
                    int newStock = product.getStockQuantity() - item.getQuantity();
                    product.setStockQuantity(newStock >= 0 ? newStock : 0);
                    productRepository.save(product);
                }
            }
            
            orderRepository.save(order);
        }
    }
    
    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @SuppressWarnings("null")
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public Optional<Order> findByRazorpayOrderId(String razorpayOrderId) {
        return orderRepository.findByRazorpayOrderId(razorpayOrderId);
    }

    /** Persist an updated order (e.g. status change) */
    @SuppressWarnings("null")
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    @Transactional
    public void processRefund(Order order) throws RazorpayException {
        if (order.getRazorpayPaymentId() != null) {
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            JSONObject refundRequest = new JSONObject();
            
            BigDecimal amountToRefund = order.getTotalAmount();
            if (order.getDeliveryFee() != null) {
                amountToRefund = amountToRefund.subtract(order.getDeliveryFee());
            }
            if (amountToRefund.compareTo(BigDecimal.ZERO) < 0) {
                amountToRefund = BigDecimal.ZERO;
            }
            
            refundRequest.put("amount", amountToRefund.multiply(new BigDecimal(100)).intValue());
            refundRequest.put("payment_id", order.getRazorpayPaymentId());
            razorpay.refunds.create(refundRequest);
        }
    }
}
