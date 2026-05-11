package com.theoryofbloom.controller;

import com.theoryofbloom.model.Order;
import com.theoryofbloom.model.Product;
import com.theoryofbloom.model.User;
import com.theoryofbloom.repository.*;
import com.theoryofbloom.service.SiteContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;
import com.theoryofbloom.service.OrderService;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
@SuppressWarnings("null")
public class AdminController {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ContactMessageRepository contactMessageRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private OrderService orderService;
    @Autowired
    private SiteContentService siteContentService;

    // ── Auth guard helper ──────────────────────────────────────────────────
    private boolean isAdmin(Principal principal) {
        if (principal == null)
            return false;
        User u = userRepository.findByEmail(principal.getName()).orElse(null);
        return u != null && "ROLE_ADMIN".equals(u.getRole());
    }

    private String saveImage(MultipartFile file) {
        if (file == null || file.isEmpty())
            return null;
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path path = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "static", "images",
                    "products");
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            Path filePath = path.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return "/images/products/" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ── Dashboard ──────────────────────────────────────────────────────────
    @GetMapping
    public String dashboard(@RequestParam(required = false, defaultValue = "overall") String preset,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model, Principal principal) {
        if (!isAdmin(principal))
            return "redirect:/admin-login";
        List<Order> allOrders = orderRepository.findAll();

        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate start = null;
        java.time.LocalDate end = null;

        if ("today".equals(preset)) {
            start = today;
            end = today;
        } else if ("yesterday".equals(preset)) {
            start = today.minusDays(1);
            end = today.minusDays(1);
        } else if ("selective".equals(preset)) {
            if (startDate != null && !startDate.isEmpty()) {
                start = java.time.LocalDate.parse(startDate);
                end = start;
            }
        } else if ("custom".equals(preset)) {
            if (startDate != null && !startDate.isEmpty())
                start = java.time.LocalDate.parse(startDate);
            if (endDate != null && !endDate.isEmpty())
                end = java.time.LocalDate.parse(endDate);
        }

        BigDecimal filteredRevenue = BigDecimal.ZERO;
        for (Order o : allOrders) {
            if (!"CANCELLED".equals(o.getStatus()) && !"RETURNED".equals(o.getStatus())) {
                if (o.getTotalAmount() != null && o.getCreatedAt() != null) {
                    java.time.LocalDate orderDate = o.getCreatedAt().toLocalDate();
                    boolean include = true;
                    if (start != null && orderDate.isBefore(start))
                        include = false;
                    if (end != null && orderDate.isAfter(end))
                        include = false;

                    if (include) {
                        filteredRevenue = filteredRevenue.add(o.getTotalAmount());
                    }
                }
            }
        }

        List<Order> recentlyUpdatedOrders = allOrders.stream()
                .filter(o -> o.getUpdatedAt() != null && o.getCreatedAt() != null
                        && o.getUpdatedAt().isAfter(o.getCreatedAt().plusSeconds(5)))
                .sorted((o1, o2) -> o2.getUpdatedAt().compareTo(o1.getUpdatedAt()))
                .limit(15)
                .collect(java.util.stream.Collectors.toList());
        List<User> allUsers = userRepository.findAll();
        List<Order> returnOrders = allOrders.stream()
                .filter(o -> "RETURN_REQUESTED".equals(o.getStatus()) || "RETURNED".equals(o.getStatus())
                        || o.getReturnStatus() != null)
                .collect(java.util.stream.Collectors.toList());

        long totalMemberships = allUsers.stream().filter(User::isPremium).count();
        List<Product> allProducts = productRepository.findAll();

        // Stock analytics
        long totalStock = allProducts.stream()
                .mapToLong(p -> p.getStockQuantity() != null ? p.getStockQuantity() : 0).sum();
        long outOfStockCount = allProducts.stream()
                .filter(p -> p.getStockQuantity() != null && p.getStockQuantity() == 0).count();
        long lowStockCount = allProducts.stream()
                .filter(p -> p.getStockQuantity() != null && p.getStockQuantity() > 0 && p.getStockQuantity() <= 10).count();

        model.addAttribute("totalOrders", allOrders.size());
        model.addAttribute("totalProducts", productRepository.count());
        model.addAttribute("totalMemberships", totalMemberships);
        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("recentOrders", allOrders);
        model.addAttribute("recentlyUpdatedOrders", recentlyUpdatedOrders);
        model.addAttribute("recentMessages", contactMessageRepository.findByOrderByCreatedAtDesc());
        model.addAttribute("allUsers", allUsers);
        model.addAttribute("allProducts", allProducts);
        model.addAttribute("returnOrders", returnOrders);
        model.addAttribute("totalStock", totalStock);
        model.addAttribute("outOfStockCount", outOfStockCount);
        model.addAttribute("lowStockCount", lowStockCount);

        model.addAttribute("filteredRevenue", filteredRevenue);
        model.addAttribute("preset", preset);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        // Site content for CMS tab
        model.addAttribute("siteContent", siteContentService.load());

        return "admin-dashboard";
    }

    // ══ ORDER MANAGEMENT ══════════════════════════════════════════════════

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
            @RequestParam String status,
            Principal principal,
            RedirectAttributes ra) {
        if (!isAdmin(principal))
            return "redirect:/admin-login";
        orderRepository.findById(id).ifPresent(o -> {
            o.setStatus(status);
            orderRepository.save(o);
        });
        ra.addFlashAttribute("success", "Order #" + id + " status updated to " + status);
        return "redirect:/admin#orders";
    }

    @PostMapping("/orders/{id}/delete")
    public String deleteOrder(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        if (!isAdmin(principal))
            return "redirect:/admin-login";
        orderRepository.deleteById(id);
        ra.addFlashAttribute("success", "Order #" + id + " deleted.");
        return "redirect:/admin#orders";
    }

    @PostMapping("/returns/{id}/process")
    public String processReturn(@PathVariable Long id,
            @RequestParam String decision,
            @RequestParam(required = false) String adminMessage,
            Principal principal, RedirectAttributes ra) {
        if (!isAdmin(principal))
            return "redirect:/admin-login";
        Order o = orderRepository.findById(id).orElse(null);
        if (o != null) {
            if ("APPROVE".equals(decision)) {
                o.setReturnStatus("APPROVED");
                if ("REPLACEMENT".equals(o.getReturnAction())) {
                    o.setStatus("REPLACED");
                } else {
                    if (o.getRazorpayPaymentId() != null) {
                        try {
                            orderService.processRefund(o);
                        } catch (Exception e) {
                            ra.addFlashAttribute("error", "Refund failed: " + e.getMessage());
                            return "redirect:/admin#returns";
                        }
                    }
                    o.setStatus("RETURNED");
                }
            } else {
                o.setReturnStatus("REJECTED");
                o.setStatus("DELIVERED");
            }
            o.setReturnAdminMessage(adminMessage);
            orderRepository.save(o);
        }
        ra.addFlashAttribute("success", "Return request processed successfully.");
        return "redirect:/admin#returns";
    }

    // ══ PRODUCT MANAGEMENT ════════════════════════════════════════════════

    @PostMapping("/products/create")
    public String createProduct(@RequestParam String name,
            @RequestParam String shortDescription,
            @RequestParam(defaultValue = "0") BigDecimal price,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) MultipartFile imageFile,
            @RequestParam(required = false) MultipartFile[] subImageFiles,
            @RequestParam(required = false) String ingredients,
            @RequestParam(required = false) String benefits,
            @RequestParam(required = false) String longDescription,
            @RequestParam(required = false) String usageInstructions,
            @RequestParam(required = false) String faq,
            @RequestParam(required = false) String directions,
            @RequestParam(required = false) String netWeight,
            @RequestParam(defaultValue = "0") Integer stockQuantity,
            @RequestParam(defaultValue = "false") boolean featured,
            @RequestParam(defaultValue = "false") boolean bestseller,
            @RequestParam(defaultValue = "false") boolean newArrival,
            @RequestParam(defaultValue = "false") boolean topRated,
            Principal principal, RedirectAttributes ra) {
        if (!isAdmin(principal))
            return "redirect:/admin-login";
        Product p = new Product();
        p.setName(name);
        p.setShortDescription(shortDescription);
        p.setPrice(price);
        p.setCategory(category);

        String savedImageUrl = saveImage(imageFile);
        if (savedImageUrl != null)
            p.setImageUrl(savedImageUrl);
        else
            p.setImageUrl("/images/placeholder.png");

        if (subImageFiles != null && subImageFiles.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (MultipartFile f : subImageFiles) {
                String u = saveImage(f);
                if (u != null) {
                    if (sb.length() > 0)
                        sb.append(",");
                    sb.append(u);
                }
            }
            if (sb.length() > 0)
                p.setSubImages(sb.toString());
        }

        p.setIngredients(ingredients);
        p.setBenefits(benefits);
        p.setLongDescription(longDescription);
        p.setUsageInstructions(usageInstructions);
        p.setFaq(faq);
        p.setDirections(directions);
        p.setNetWeight(netWeight);
        p.setStockQuantity(stockQuantity);
        p.setFeatured(featured);
        p.setBestseller(bestseller);
        p.setNewArrival(newArrival);
        p.setTopRated(topRated);
        productRepository.save(p);
        ra.addFlashAttribute("success", "Product '" + name + "' created.");
        return "redirect:/admin#products";
    }

    @PostMapping("/products/{id}/update")
    public String updateProduct(@PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String shortDescription,
            @RequestParam(required = false) BigDecimal price,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) MultipartFile imageFile,
            @RequestParam(required = false) MultipartFile[] subImageFiles,
            @RequestParam(required = false) String ingredients,
            @RequestParam(required = false) String benefits,
            @RequestParam(required = false) String longDescription,
            @RequestParam(required = false) String usageInstructions,
            @RequestParam(required = false) String faq,
            @RequestParam(required = false) String directions,
            @RequestParam(required = false) String netWeight,
            @RequestParam(required = false) Integer stockQuantity,
            @RequestParam(defaultValue = "false") boolean featured,
            @RequestParam(defaultValue = "false") boolean bestseller,
            @RequestParam(defaultValue = "false") boolean newArrival,
            @RequestParam(defaultValue = "false") boolean topRated,
            Principal principal, RedirectAttributes ra) {
        if (!isAdmin(principal))
            return "redirect:/admin-login";
        productRepository.findById(id).ifPresent(p -> {
            if (name != null && !name.isEmpty())
                p.setName(name);
            if (shortDescription != null && !shortDescription.isEmpty())
                p.setShortDescription(shortDescription);
            if (price != null)
                p.setPrice(price);
            if (category != null && !category.isEmpty())
                p.setCategory(category);

            if (imageFile != null && !imageFile.isEmpty()) {
                String savedImageUrl = saveImage(imageFile);
                if (savedImageUrl != null)
                    p.setImageUrl(savedImageUrl);
            }

            if (subImageFiles != null && subImageFiles.length > 0 && !subImageFiles[0].isEmpty()) {
                StringBuilder sb = new StringBuilder();
                if (p.getSubImages() != null && !p.getSubImages().isEmpty()) {
                    sb.append(p.getSubImages());
                }
                for (MultipartFile f : subImageFiles) {
                    if (f.isEmpty())
                        continue;
                    String u = saveImage(f);
                    if (u != null) {
                        if (sb.length() > 0)
                            sb.append(",");
                        sb.append(u);
                    }
                }
                if (sb.length() > 0)
                    p.setSubImages(sb.toString());
            }

            if (ingredients != null && !ingredients.isEmpty())
                p.setIngredients(ingredients);
            if (benefits != null && !benefits.isEmpty())
                p.setBenefits(benefits);
            if (longDescription != null && !longDescription.isEmpty())
                p.setLongDescription(longDescription);
            if (usageInstructions != null && !usageInstructions.isEmpty())
                p.setUsageInstructions(usageInstructions);
            if (faq != null && !faq.isEmpty())
                p.setFaq(faq);
            if (directions != null && !directions.isEmpty())
                p.setDirections(directions);
            if (netWeight != null && !netWeight.isEmpty())
                p.setNetWeight(netWeight);
            if (stockQuantity != null)
                p.setStockQuantity(stockQuantity);
            p.setFeatured(featured);
            p.setBestseller(bestseller);
            p.setNewArrival(newArrival);
            p.setTopRated(topRated);
            productRepository.save(p);
        });
        ra.addFlashAttribute("success", "Product updated.");
        return "redirect:/admin#products";
    }

    @PostMapping("/products/{id}/out-of-stock")
    public String setOutOfStock(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        if (!isAdmin(principal))
            return "redirect:/admin-login";
        productRepository.findById(id).ifPresent(p -> {
            p.setStockQuantity(0);
            productRepository.save(p);
        });
        ra.addFlashAttribute("success", "Product marked as Out of Stock.");
        return "redirect:/admin#products";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        if (!isAdmin(principal))
            return "redirect:/admin-login";
        productRepository.deleteById(id);
        ra.addFlashAttribute("success", "Product #" + id + " deleted.");
        return "redirect:/admin#products";
    }

    // ══ USER MANAGEMENT ═══════════════════════════════════════════════════

    @PostMapping("/users/{id}/role")
    public String updateUserRole(@PathVariable Long id,
            @RequestParam String role,
            Principal principal, RedirectAttributes ra) {
        if (!isAdmin(principal))
            return "redirect:/admin-login";
        userRepository.findById(id).ifPresent(u -> {
            u.setRole(role);
            userRepository.save(u);
        });
        ra.addFlashAttribute("success", "User role updated.");
        return "redirect:/admin#users";
    }

    @PostMapping("/users/{id}/premium")
    public String togglePremium(@PathVariable Long id,
            @RequestParam boolean premium,
            Principal principal, RedirectAttributes ra) {
        if (!isAdmin(principal))
            return "redirect:/admin-login";
        userRepository.findById(id).ifPresent(u -> {
            u.setPremium(premium);
            userRepository.save(u);
        });
        ra.addFlashAttribute("success", "Premium status updated.");
        return "redirect:/admin#users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        if (!isAdmin(principal))
            return "redirect:/admin-login";
        userRepository.deleteById(id);
        ra.addFlashAttribute("success", "User #" + id + " deleted.");
        return "redirect:/admin#users";
    }

    @PostMapping("/users/create")
    public String createUser(@RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String password,
            @RequestParam String role,
            @RequestParam(required = false) String adminPassword,
            Principal principal, RedirectAttributes ra) {
        if (!isAdmin(principal))
            return "redirect:/admin-login";
        if (userRepository.findByEmail(email).isPresent()) {
            ra.addFlashAttribute("error", "Email already exists.");
            return "redirect:/admin#users";
        }

        if ("ROLE_ADMIN".equals(role)) {
            User mainAdmin = userRepository.findByEmail("admin@theoryofbloom.com").orElse(null);
            if (mainAdmin == null || !passwordEncoder.matches(adminPassword, mainAdmin.getPassword())) {
                ra.addFlashAttribute("error", "Invalid Main Admin Password. Cannot create a new admin.");
                return "redirect:/admin#users";
            }
        }

        User u = new User(fullName, email, passwordEncoder.encode(password));
        u.setPhone(phone);
        u.setRole(role);
        userRepository.save(u);
        ra.addFlashAttribute("success", "User created successfully.");
        return "redirect:/admin#users";
    }

    @PostMapping("/users/delete-admin")
    public String deleteAdmin(@RequestParam Long id, @RequestParam String adminPassword, Principal principal,
            RedirectAttributes ra) {
        if (!isAdmin(principal))
            return "redirect:/admin-login";

        User mainAdmin = userRepository.findByEmail("admin@theoryofbloom.com").orElse(null);
        if (mainAdmin == null || !passwordEncoder.matches(adminPassword, mainAdmin.getPassword())) {
            ra.addFlashAttribute("error", "Invalid Main Admin Password. Action denied.");
            return "redirect:/admin#users";
        }

        User u = userRepository.findById(id).orElse(null);
        if (u != null && "ROLE_ADMIN".equals(u.getRole())) {
            if ("admin@theoryofbloom.com".equals(u.getEmail())) {
                ra.addFlashAttribute("error", "Cannot delete the main admin account.");
            } else {
                userRepository.deleteById(id);
                ra.addFlashAttribute("success", "Admin user deleted successfully.");
            }
        } else {
            ra.addFlashAttribute("error", "Invalid operation.");
        }
        return "redirect:/admin#users";
    }

    @PostMapping("/users/{id}/block")
    public String toggleBlockUser(@PathVariable Long id, @RequestParam boolean blocked, Principal principal,
            RedirectAttributes ra) {
        if (!isAdmin(principal))
            return "redirect:/admin-login";
        userRepository.findById(id).ifPresent(u -> {
            if (!u.getEmail().equals(principal.getName()) && !"ROLE_ADMIN".equals(u.getRole())) {
                u.setBlocked(blocked);
                userRepository.save(u);
            }
        });
        ra.addFlashAttribute("success", "User block status updated.");
        return "redirect:/admin#users";
    }


}
