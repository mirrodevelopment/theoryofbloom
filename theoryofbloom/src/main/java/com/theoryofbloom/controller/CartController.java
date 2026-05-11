package com.theoryofbloom.controller;

import com.theoryofbloom.model.Product;
import com.theoryofbloom.service.CartService;
import com.theoryofbloom.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    @GetMapping
    public String viewCart(Model model) {
        model.addAttribute("cartItems", cartService.getItems());
        model.addAttribute("totalPrice", cartService.getTotalPrice());
        model.addAttribute("totalItems", cartService.getTotalItems());
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            RedirectAttributes redirectAttributes) {
        Optional<Product> productOpt = productService.getProductById(productId);
        if (productOpt.isPresent()) {
            cartService.addItem(productOpt.get(), quantity);
            redirectAttributes.addFlashAttribute("message", "Added to cart!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Product not found.");
        }
        return "redirect:/cart";
    }

    /** AJAX endpoint — no redirect, returns JSON status */
    @PostMapping("/add/ajax")
    @ResponseBody
    public ResponseEntity<?> addToCartAjax(@RequestParam Long productId,
                                            @RequestParam(defaultValue = "1") int quantity) {
        Optional<Product> productOpt = productService.getProductById(productId);
        if (productOpt.isPresent()) {
            cartService.addItem(productOpt.get(), quantity);
            return ResponseEntity.ok(Map.of("status", "ok", "message", "Added to cart!"));
        }
        return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Product not found."));
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam Long productId,
                                 @RequestParam int quantity) {
        cartService.updateQuantity(productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam Long productId) {
        cartService.removeItem(productId);
        return "redirect:/cart";
    }
}