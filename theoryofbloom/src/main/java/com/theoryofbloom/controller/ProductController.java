package com.theoryofbloom.controller;

import com.theoryofbloom.model.Product;
import com.theoryofbloom.model.User;
import com.theoryofbloom.repository.UserRepository;
import com.theoryofbloom.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/shop")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String shopPage(@RequestParam(required = false) String category,
                           @RequestParam(required = false) String query,
                           @RequestParam(required = false) String sort,
                           Model model, Authentication auth) {
        
        List<Product> products = productService.searchProducts(query, category, sort);
        
        model.addAttribute("products", products);
        model.addAttribute("searchKeyword", query);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("currentSort", sort);

        if (auth != null && auth.isAuthenticated()) {
            User user = userRepository.findByEmail(auth.getName()).orElse(null);
            if (user != null) model.addAttribute("user", user);
        }

        return "shop";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model, Authentication auth) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        model.addAttribute("product", product);

        if (auth != null && auth.isAuthenticated()) {
            User user = userRepository.findByEmail(auth.getName()).orElse(null);
            if (user != null) model.addAttribute("user", user);
        }

        return "product-detail";
    }

    // Aliases for accessibility
    @GetMapping("-blends")
    public String shopBlendsAlias() {
        return "redirect:/shop";
    }
}