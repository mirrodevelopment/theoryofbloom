package com.theoryofbloom.controller;

import com.theoryofbloom.model.Order;
import com.theoryofbloom.model.User;
import com.theoryofbloom.service.OrderService;
import com.theoryofbloom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private com.theoryofbloom.repository.ProductRepository productRepository;

    public String viewOrders(Model model, Principal principal) {
        User user = userService.findByEmail(principal.getName());
        model.addAttribute("orders", orderService.getUserOrders(user));
        model.addAttribute("allProducts", productRepository.findAll());
        return "order-history";
    }

    /** Cancel order — only allowed before SHIPPED status */
    @PostMapping("/cancel/{id}")
    public String cancelOrder(@PathVariable Long id,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(principal.getName());
        List<Order> orders = orderService.getUserOrders(user);
        Order target = orders.stream()
                .filter(o -> o.getId().equals(id))
                .findFirst().orElse(null);

        if (target == null) {
            redirectAttributes.addFlashAttribute("error", "Order not found.");
        } else {
            String status = target.getStatus();
            if ("SHIPPED".equals(status) || "DELIVERED".equals(status) || "CANCELLED".equals(status)) {
                redirectAttributes.addFlashAttribute("error",
                        "Order #" + id + " cannot be cancelled at this stage.");
            } else {
                target.setStatus("CANCELLED");
                orderService.save(target);
                redirectAttributes.addFlashAttribute("message",
                        "Order #" + id + " has been cancelled.");
            }
        }
        return "redirect:/order-history";
    }

    /** Return order — only allowed when DELIVERED */
    @PostMapping("/return/{id}")
    public String returnOrder(@PathVariable Long id,
                              @RequestParam String reason,
                              @RequestParam(required = false) String comments,
                              @RequestParam(required = false, defaultValue = "REFUND") String action,
                              @RequestParam(required = false) String replacementProduct,
                              @RequestParam(value = "returnImage", required = false) org.springframework.web.multipart.MultipartFile returnImage,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(principal.getName());
        List<Order> orders = orderService.getUserOrders(user);
        Order target = orders.stream()
                .filter(o -> o.getId().equals(id))
                .findFirst().orElse(null);

        if (target == null) {
            redirectAttributes.addFlashAttribute("error", "Order not found.");
        } else {
            String status = target.getStatus();
            if (!"DELIVERED".equals(status)) {
                redirectAttributes.addFlashAttribute("error", "Only delivered orders can be returned.");
            } else {
                target.setStatus("RETURN_REQUESTED");
                target.setReturnStatus("PENDING");
                target.setReturnAction(action);
                if ("REPLACEMENT".equals(action) && replacementProduct != null) {
                    target.setReplacementProduct(replacementProduct);
                }
                target.setReturnReason(reason);
                target.setReturnComments(comments);
                if (returnImage != null && !returnImage.isEmpty()) {
                    target.setReturnImageUrl(saveImage(returnImage));
                }
                orderService.save(target);
                redirectAttributes.addFlashAttribute("message", "Return requested for Order #" + id + ". Our team will verify and contact you shortly.");
            }
        }
        return "redirect:/order-history";
    }

    private String saveImage(org.springframework.web.multipart.MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            String originalName = file.getOriginalFilename();
            String fileName = java.util.UUID.randomUUID().toString() + "_" + (originalName != null ? originalName.replaceAll("[^a-zA-Z0-9.-]", "_") : "unnamed");
            java.nio.file.Path path = java.nio.file.Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "static", "images", "returns");
            if (!java.nio.file.Files.exists(path)) {
                java.nio.file.Files.createDirectories(path);
            }
            java.nio.file.Path filePath = path.resolve(fileName);
            java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return "/images/returns/" + fileName;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
