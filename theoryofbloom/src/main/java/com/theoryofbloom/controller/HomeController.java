package com.theoryofbloom.controller;

import com.theoryofbloom.model.User;
import com.theoryofbloom.repository.UserRepository;
import com.theoryofbloom.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.time.LocalDateTime;

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String home(Model model, Authentication auth) {
        model.addAttribute("featuredProducts", productService.getFeaturedProducts());

        if (auth != null && auth.isAuthenticated()) {
            User user = userRepository.findByEmail(auth.getName()).orElse(null);
            if (user != null) {
                model.addAttribute("user", user);
            }
        }

        return "index";
    }

    @GetMapping("/home")
    public String homePage(Model model, Authentication auth) {
        return home(model, auth);
    }

    @GetMapping("/saved-blends")
    public String savedBlends(Model model, Authentication auth) {
        model.addAttribute("products", productService.getAllProducts());
        if (auth != null && auth.isAuthenticated()) {
            User user = userRepository.findByEmail(auth.getName()).orElse(null);
            if (user != null) model.addAttribute("user", user);
        }
        return "saved-blends";
    }

    @GetMapping("/product-11")
    public String product11(Model model, Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            User user = userRepository.findByEmail(auth.getName()).orElse(null);
            if (user != null) model.addAttribute("user", user);
        }
        return "product-11";
    }

    @GetMapping("/my-reviews")
    public String myReviews(Model model, Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            User user = userRepository.findByEmail(auth.getName()).orElse(null);
            if (user != null) model.addAttribute("user", user);
        }
        return "my-reviews";
    }

    @GetMapping("/membership")
    public String membership(Model model, Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            User user = userRepository.findByEmail(auth.getName()).orElse(null);
            if (user != null) model.addAttribute("user", user);
        }
        return "membership";
    }

    @PostMapping("/membership/upgrade/ajax")
    @ResponseBody
    public String upgradeMembership(Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            User user = userRepository.findByEmail(auth.getName()).orElse(null);
            if (user != null) {
                user.setPremium(true);
                user.setPremiumExpiryDate(LocalDateTime.now().plusYears(1));
                userRepository.save(user);
                return "{\"status\":\"success\"}";
            }
        }
        return "{\"status\":\"error\"}";
    }
}