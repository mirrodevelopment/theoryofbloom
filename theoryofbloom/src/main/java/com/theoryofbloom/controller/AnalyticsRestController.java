package com.theoryofbloom.controller;

import com.theoryofbloom.model.Order;
import com.theoryofbloom.model.OrderItem;
import com.theoryofbloom.model.Product;
import com.theoryofbloom.repository.OrderRepository;
import com.theoryofbloom.repository.ProductRepository;
import com.theoryofbloom.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Product Analytics REST API — real-time sales data for the admin dashboard.
 * Returns JSON for charts and summary cards.
 */
@RestController
@RequestMapping("/api/admin/analytics")
@SuppressWarnings("null")
public class AnalyticsRestController {

    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;

    // ── Auth helper ────────────────────────────────────────────────────────
    private boolean hasAccess(Principal p) {
        if (p == null) return false;
        return userRepository.findByEmail(p.getName())
                .map(u -> "ROLE_ADMIN".equals(u.getRole()) || "ROLE_SUB_ADMIN".equals(u.getRole()))
                .orElse(false);
    }

    private boolean isActiveOrder(Order o) {
        return o.getStatus() != null
                && !o.getStatus().equalsIgnoreCase("CANCELLED")
                && !o.getStatus().equalsIgnoreCase("RETURNED")
                && !o.getStatus().equalsIgnoreCase("REFUNDED");
    }

    // ── Product overview card ──────────────────────────────────────────────
    @GetMapping("/products/overview")
    public ResponseEntity<?> productOverview(Principal principal) {
        if (!hasAccess(principal)) return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));

        long totalProducts = productRepository.count();
        List<Order> orders = orderRepository.findAll();

        // Total sold units and revenue
        long totalSoldUnits = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalProductRevenue = BigDecimal.ZERO;
        Map<Long, Long>  productSalesCount = new HashMap<>();
        Map<Long, BigDecimal> productRevenue = new HashMap<>();

        for (Order o : orders) {
            if (!isActiveOrder(o)) continue;
            if (o.getOrderItems() == null) continue;
            for (OrderItem item : o.getOrderItems()) {
                if (item.getProduct() == null) continue;
                Long pid = item.getProduct().getId();
                long qty = item.getQuantity();
                BigDecimal rev = item.getSubtotal() != null ? item.getSubtotal() : BigDecimal.ZERO;
                totalSoldUnits += qty;
                totalProductRevenue = totalProductRevenue.add(rev);
                productSalesCount.merge(pid, qty, Long::sum);
                productRevenue.merge(pid, rev, BigDecimal::add);
            }
            if (o.getTotalAmount() != null) totalRevenue = totalRevenue.add(o.getTotalAmount());
        }

        // Top 5 products by units sold
        List<Map<String, Object>> topProducts = productSalesCount.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    Product p = productRepository.findById(e.getKey()).orElse(null);
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", e.getKey());
                    m.put("name", p != null ? p.getName() : "Unknown");
                    m.put("category", p != null ? p.getCategory() : "N/A");
                    m.put("imageUrl", p != null ? p.getImageUrl() : null);
                    m.put("unitsSold", e.getValue());
                    m.put("revenue", productRevenue.getOrDefault(e.getKey(), BigDecimal.ZERO));
                    return m;
                })
                .collect(Collectors.toList());

        // Category-wise sales (for pie chart)
        Map<String, Long> catSales = new LinkedHashMap<>();
        for (Map.Entry<Long, Long> e : productSalesCount.entrySet()) {
            Product p = productRepository.findById(e.getKey()).orElse(null);
            String cat = (p != null && p.getCategory() != null) ? p.getCategory() : "Other";
            catSales.merge(cat, e.getValue(), Long::sum);
        }

        // Calculate stock statistics
        long totalStock = 0;
        long lowStockCount = 0;
        long outOfStockCount = 0;
        for (Product p : productRepository.findAll()) {
            if (p.getStockQuantity() != null) {
                totalStock += p.getStockQuantity();
                if (p.getStockQuantity() == 0) {
                    outOfStockCount++;
                } else if (p.getStockQuantity() < 10) { // arbitrary threshold for low stock
                    lowStockCount++;
                }
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalProducts", totalProducts);
        result.put("totalSoldUnits", totalSoldUnits);
        result.put("totalRevenue", totalRevenue);
        result.put("totalProductRevenue", totalProductRevenue);
        result.put("totalStock", totalStock);
        result.put("lowStockCount", lowStockCount);
        result.put("outOfStockCount", outOfStockCount);
        result.put("topProducts", topProducts);
        result.put("categorySales", catSales);
        return ResponseEntity.ok(result);
    }

    // ── Sales by date / month / custom range ──────────────────────────────
    @GetMapping("/sales/by-date")
    public ResponseEntity<?> salesByDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false, defaultValue = "day") String groupBy,
            Principal principal) {

        if (!hasAccess(principal)) return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));

        if (from == null) from = LocalDate.now().minusDays(29);
        if (to == null)   to = LocalDate.now();

        LocalDate fFrom = from;
        LocalDate fTo   = to;

        List<Order> orders = orderRepository.findAll().stream()
                .filter(this::isActiveOrder)
                .filter(o -> o.getCreatedAt() != null)
                .filter(o -> {
                    LocalDate d = o.getCreatedAt().toLocalDate();
                    return !d.isBefore(fFrom) && !d.isAfter(fTo);
                })
                .collect(Collectors.toList());

        // Build a sorted map: key = "YYYY-MM-DD" or "YYYY-MM", value = {orders, revenue, units}
        Map<String, Map<String, Object>> grouped = new TreeMap<>();

        for (Order o : orders) {
            LocalDate d = o.getCreatedAt().toLocalDate();
            String key = "month".equals(groupBy)
                    ? d.getYear() + "-" + String.format("%02d", d.getMonthValue())
                    : d.toString();

            grouped.computeIfAbsent(key, k -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("label", k);
                m.put("orders", 0L);
                m.put("revenue", BigDecimal.ZERO);
                m.put("units", 0L);
                return m;
            });

            Map<String, Object> entry = grouped.get(key);
            entry.put("orders", (Long) entry.get("orders") + 1);
            if (o.getTotalAmount() != null)
                entry.put("revenue", ((BigDecimal) entry.get("revenue")).add(o.getTotalAmount()));
            if (o.getOrderItems() != null)
                entry.put("units", (Long) entry.get("units") + o.getOrderItems().stream().mapToLong(OrderItem::getQuantity).sum());
        }

        return ResponseEntity.ok(Map.of(
            "from", fFrom.toString(),
            "to", fTo.toString(),
            "groupBy", groupBy,
            "data", grouped.values()
        ));
    }

    // ── Per-product sales breakdown ───────────────────────────────────────
    @GetMapping("/products/all-sales")
    public ResponseEntity<?> allProductSales(Principal principal) {
        if (!hasAccess(principal)) return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));

        Map<Long, Map<String, Object>> map = new LinkedHashMap<>();
        List<Order> orders = orderRepository.findAll();

        for (Order o : orders) {
            if (!isActiveOrder(o) || o.getOrderItems() == null) continue;
            for (OrderItem item : o.getOrderItems()) {
                if (item.getProduct() == null) continue;
                Long pid = item.getProduct().getId();
                map.computeIfAbsent(pid, k -> {
                    Product p = productRepository.findById(k).orElse(null);
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", k);
                    m.put("name", p != null ? p.getName() : "Unknown");
                    m.put("category", p != null ? p.getCategory() : "N/A");
                    m.put("price", p != null ? p.getPrice() : BigDecimal.ZERO);
                    m.put("stock", p != null ? p.getStockQuantity() : 0);
                    m.put("unitsSold", 0L);
                    m.put("revenue", BigDecimal.ZERO);
                    return m;
                });
                Map<String, Object> entry = map.get(pid);
                entry.put("unitsSold", (Long) entry.get("unitsSold") + item.getQuantity());
                entry.put("revenue", ((BigDecimal) entry.get("revenue")).add(
                        item.getSubtotal() != null ? item.getSubtotal() : BigDecimal.ZERO));
            }
        }

        List<Map<String, Object>> result = map.values().stream()
                .sorted(Comparator.comparingLong(m -> -((Long) m.get("unitsSold"))))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
