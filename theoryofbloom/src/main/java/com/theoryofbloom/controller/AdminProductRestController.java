package com.theoryofbloom.controller;

import com.theoryofbloom.model.Order;
import com.theoryofbloom.model.Product;
import com.theoryofbloom.model.User;
import com.theoryofbloom.repository.OrderRepository;
import com.theoryofbloom.repository.ProductRepository;
import com.theoryofbloom.repository.UserRepository;
import com.theoryofbloom.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminProductRestController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    private boolean isAdmin(Principal principal) {
        if (principal == null) return false;
        User u = userRepository.findByEmail(principal.getName()).orElse(null);
        return u != null && "ROLE_ADMIN".equals(u.getRole());
    }

    // ── Product: Toggle Featured Blend ────────────────────────────────────
    @PostMapping("/products/{id}/toggle-featured")
    public ResponseEntity<?> toggleFeatured(@PathVariable Long id, Principal principal) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Unauthorized"));
        }
        Product p = productRepository.findById(id).orElse(null);
        if (p == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Product not found"));
        }
        p.setFeatured(!p.isFeatured());
        productRepository.save(p);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "featured", p.isFeatured(),
            "message", p.isFeatured()
                ? "'" + p.getName() + "' added to Featured Blends on Home page"
                : "'" + p.getName() + "' removed from Featured Blends"
        ));
    }

    // ── Product: Toggle Bestseller ─────────────────────────────────────────
    @PostMapping("/products/{id}/toggle-bestseller")
    public ResponseEntity<?> toggleBestseller(@PathVariable Long id, Principal principal) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Unauthorized"));
        }
        Product p = productRepository.findById(id).orElse(null);
        if (p == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Product not found"));
        }
        p.setBestseller(!p.isBestseller());
        productRepository.save(p);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "bestseller", p.isBestseller(),
            "message", p.isBestseller()
                ? "'" + p.getName() + "' marked as Bestseller"
                : "'" + p.getName() + "' removed from Bestsellers"
        ));
    }

    // ── Product: Toggle New Arrival ────────────────────────────────────────
    @PostMapping("/products/{id}/toggle-new-arrival")
    public ResponseEntity<?> toggleNewArrival(@PathVariable Long id, Principal principal) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Unauthorized"));
        }
        Product p = productRepository.findById(id).orElse(null);
        if (p == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Product not found"));
        }
        p.setNewArrival(!p.isNewArrival());
        productRepository.save(p);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "newArrival", p.isNewArrival(),
            "message", p.isNewArrival()
                ? "'" + p.getName() + "' marked as New Arrival"
                : "'" + p.getName() + "' removed from New Arrivals"
        ));
    }

    // ── Product: Toggle Top Rated ─────────────────────────────────────────
    @PostMapping("/products/{id}/toggle-top-rated")
    public ResponseEntity<?> toggleTopRated(@PathVariable Long id, Principal principal) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Unauthorized"));
        }
        Product p = productRepository.findById(id).orElse(null);
        if (p == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Product not found"));
        }
        p.setTopRated(!p.isTopRated());
        productRepository.save(p);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "topRated", p.isTopRated(),
            "message", p.isTopRated()
                ? "'" + p.getName() + "' marked as Top Rated"
                : "'" + p.getName() + "' removed from Top Rated"
        ));
    }

    // ── Stock Overview ────────────────────────────────────────────────────
    @GetMapping("/analytics/stock/overview")
    public ResponseEntity<?> stockOverview(Principal principal) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }
        List<Product> all = productRepository.findAll();
        long totalStock = all.stream().mapToLong(p -> p.getStockQuantity() != null ? p.getStockQuantity() : 0).sum();
        long outOfStock = all.stream().filter(p -> p.getStockQuantity() != null && p.getStockQuantity() == 0).count();
        long lowStock = all.stream().filter(p -> p.getStockQuantity() != null && p.getStockQuantity() > 0 && p.getStockQuantity() <= 10).count();
        long inStock = all.stream().filter(p -> p.getStockQuantity() != null && p.getStockQuantity() > 10).count();
        return ResponseEntity.ok(Map.of(
            "totalStock", totalStock,
            "outOfStockCount", outOfStock,
            "lowStockCount", lowStock,
            "inStockCount", inStock
        ));
    }

    // ── User: Secure Role Change (AJAX, requires admin password) ─────────
    @PostMapping("/users/{id}/change-role")
    public ResponseEntity<?> changeUserRole(
            @PathVariable Long id,
            @RequestParam String newRole,
            @RequestParam String adminPassword,
            Principal principal) {

        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Unauthorized"));
        }

        // Verify the calling admin's password
        User adminUser = userRepository.findByEmail(principal.getName()).orElse(null);
        if (adminUser == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Admin user not found"));
        }

        // Check password using BCrypt
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        if (!encoder.matches(adminPassword, adminUser.getPassword())) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Incorrect admin password"));
        }

        // Find target user
        User targetUser = userRepository.findById(id).orElse(null);
        if (targetUser == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not found"));
        }

        // Block modification of main admin
        if ("admin@theoryofbloom.com".equals(targetUser.getEmail())) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Cannot modify the main admin account"));
        }

        // Validate new role
        if (!"ROLE_USER".equals(newRole) && !"ROLE_ADMIN".equals(newRole)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid role value"));
        }

        String oldRole = targetUser.getRole();
        targetUser.setRole(newRole);
        userRepository.save(targetUser);

        String displayRole = "ROLE_ADMIN".equals(newRole) ? "Admin" : "User";
        System.out.println("[AUDIT] Role changed: user=" + targetUser.getEmail()
            + " from=" + oldRole + " to=" + newRole
            + " by=" + principal.getName());

        return ResponseEntity.ok(Map.of(
            "success", true,
            "newRole", newRole,
            "displayRole", displayRole,
            "message", targetUser.getFullName() + " is now a " + displayRole
        ));
    }

    // ── Refund: Approve and trigger Razorpay refund ───────────────────────
    @PostMapping("/orders/{id}/approve-refund")
    public ResponseEntity<?> approveRefund(@PathVariable Long id, Principal principal) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Unauthorized"));
        }
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Order not found"));
        }
        // Guard: already refunded
        if ("REFUNDED".equals(order.getRefundStatus()) || "APPROVED".equals(order.getRefundStatus())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Refund already processed for this order"));
        }
        // Guard: must be return requested
        if (!"RETURN_REQUESTED".equals(order.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Order is not in a returnable state"));
        }

        try {
            order.setReturnStatus("APPROVED");
            // If Razorpay payment exists, trigger real refund
            if (order.getRazorpayPaymentId() != null && !order.getRazorpayPaymentId().isBlank()) {
                orderService.processRefund(order);
                order.setRefundStatus("REFUNDED");
                order.setStatus("REFUNDED");
            } else {
                // COD — just mark as returned
                order.setRefundStatus("APPROVED");
                order.setStatus("RETURNED");
            }
            orderRepository.save(order);
            return ResponseEntity.ok(Map.of("success", true, "message",
                "Refund approved" + (order.getRazorpayPaymentId() != null ? " and Razorpay refund initiated" : " (COD order marked as Returned)")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Refund failed: " + e.getMessage()));
        }
    }

    // ── Refund: Reject refund request ─────────────────────────────────────
    @PostMapping("/orders/{id}/reject-refund")
    public ResponseEntity<?> rejectRefund(
            @PathVariable Long id,
            @RequestParam(required = false) String adminMessage,
            Principal principal) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Unauthorized"));
        }
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Order not found"));
        }
        if ("REFUNDED".equals(order.getStatus()) || "APPROVED".equals(order.getReturnStatus())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Refund already processed"));
        }
        order.setReturnStatus("REJECTED");
        order.setStatus("DELIVERED"); // Restore to delivered
        if (adminMessage != null && !adminMessage.isBlank()) {
            order.setReturnAdminMessage(adminMessage);
        }
        orderRepository.save(order);
        return ResponseEntity.ok(Map.of("success", true, "message", "Refund request rejected"));
    }

    // ── Order: Status update via REST (for AJAX selects) ─────────────────
    @PostMapping("/orders/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Principal principal) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Unauthorized"));
        }
        // Validate final states cannot be overridden
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Order not found"));
        }
        // Prevent changing status of already-returned or refunded orders
        if ("RETURNED".equals(order.getStatus()) || "REFUNDED".equals(order.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message",
                "Cannot change status: order is already in a final state (" + order.getStatus() + ")"));
        }
        order.setStatus(status);
        orderRepository.save(order);
        return ResponseEntity.ok(Map.of("success", true, "status", status, "message", "Order #" + id + " status updated to " + status));
    }
}
