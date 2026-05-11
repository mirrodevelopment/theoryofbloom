package com.theoryofbloom.controller;

import com.theoryofbloom.model.User;
import com.theoryofbloom.service.UserService;
import com.theoryofbloom.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/admin-login")
    public String adminLoginPage() {
        return "admin-login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            Model model) {
        boolean success = userService.registerUser(fullName, email, password);
        if (!success) {
            model.addAttribute("error", "Email already exists");
            return "register";
        }
        return "redirect:/login?registered";
    }

    @GetMapping("/order-history")
    public String orderHistoryPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email);
        model.addAttribute("user", user);
        model.addAttribute("orders", orderService.getUserOrders(user));
        return "order-history";
    }

    @GetMapping("/profile")
    public String profilePage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email);
        model.addAttribute("user", user);
        model.addAttribute("orders", orderService.getUserOrders(user));
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam String fullName,
            @RequestParam(required = false) String dob,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String altPhone) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email);
        
        if (user != null) {
            user.setFullName(fullName);
            user.setDob(dob);
            user.setGender(gender);
            user.setPhone(phone);
            user.setAltPhone(altPhone);
            userService.updateProfile(user);
        }
        return "redirect:/profile?updated=profile";
    }

    @PostMapping("/profile/address/update")
    public String updateAddress(
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String pincode) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email);
        
        if (user != null) {
            user.setAddress(address);
            user.setCity(city);
            user.setState(state);
            user.setPincode(pincode);
            userService.updateProfile(user);
        }
        return "redirect:/profile?updated=address";
    }

    @PostMapping("/profile/security/update")
    public String updateSecurity(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            RedirectAttributes redirectAttributes) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email);
        
        if (user != null) {
            if (passwordEncoder.matches(currentPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                userService.updateProfile(user);
                return "redirect:/profile?updated=security";
            } else {
                redirectAttributes.addFlashAttribute("error", "Current password incorrect");
                return "redirect:/profile#security";
            }
        }
        return "redirect:/profile";
    }

    @GetMapping("/privacy-policy")
    public String privacyPolicyPage() {
        return "privacy-policy";
    }

    @GetMapping("/terms")
    public String termsPage() {
        return "privacy-policy";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String sendResetLink(@RequestParam String email, Model model) {
        String token = userService.createPasswordResetToken(email);
        if (token != null) {
            // In real implementation send email with link: /reset-password?token=...
            model.addAttribute("message", "Reset link sent to your email");
        } else {
            model.addAttribute("error", "Email not found");
        }
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
            @RequestParam String password,
            Model model) {
        boolean success = userService.resetPassword(token, password);
        if (success) {
            return "redirect:/login?resetSuccess";
        } else {
            model.addAttribute("error", "Invalid or expired token");
            return "reset-password";
        }
    }

    @PostMapping("/subscribe")
    @ResponseBody
    public String subscribe(@RequestParam String email) {
        //
        System.out.println("Newsletter subscription: " + email);
        return "OK";
    }
}