package com.theoryofbloom.controller;

import com.theoryofbloom.model.Order;
import com.theoryofbloom.model.User;
import com.theoryofbloom.service.CartService;
import com.theoryofbloom.service.OrderService;
import com.theoryofbloom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

@Controller
public class CheckoutController {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @GetMapping("/checkout")
    public String showCheckoutPage(Model model, Principal principal) {
        if (cartService.getItems().isEmpty()) {
            return "redirect:/cart";
        }
        
        if (principal != null) {
            User user = userService.findByEmail(principal.getName());
            model.addAttribute("user", user);
        }
        model.addAttribute("cartItems", cartService.getItems());
        model.addAttribute("totalPrice", cartService.getTotalPrice());
        return "checkout";
    }

    @PostMapping("/checkout/process")
    public String processCheckout(
            @RequestParam String contactName,
            @RequestParam String shippingAddress,
            @RequestParam String city,
            @RequestParam String state,
            @RequestParam String pincode,
            @RequestParam String contactPhone,
            @RequestParam String contactEmail,
            @RequestParam(defaultValue = "false") boolean isCod,
            @RequestParam(defaultValue = "false") boolean saveAddress,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (contactName == null || contactName.trim().isEmpty() ||
            shippingAddress == null || shippingAddress.trim().isEmpty() ||
            contactPhone == null || contactPhone.trim().isEmpty() ||
            city == null || city.trim().isEmpty() ||
            state == null || state.trim().isEmpty() ||
            pincode == null || pincode.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "All shipping address fields and contact details are strictly required.");
            return "redirect:/checkout";
        }

        try {
            User user = null;
            if (principal != null) {
                user = userService.findByEmail(principal.getName());
                if (user != null && saveAddress) {
                    user.setAddress(shippingAddress);
                    user.setCity(city);
                    user.setState(state);
                    user.setPincode(pincode);
                    user.setPhone(contactPhone);
                    userService.updateProfile(user);
                }
            }
            
            String fullAddress = shippingAddress + ", " + city + ", " + state + " - " + pincode;
            Order order = orderService.createOrder(user, contactName, fullAddress, contactPhone, contactEmail, isCod);

            if (isCod) {
                cartService.clearCart();
                return "redirect:/order-thankyou?orderId=" + order.getId();
            }

            // For Razorpay, return a payment confirmation page that auto-launches the gateway
            model.addAttribute("order", order);
            model.addAttribute("razorpayKeyId", razorpayKeyId);
            return "payment";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to process order: " + e.getMessage());
            return "redirect:/checkout";
        }
    }

    @PostMapping("/checkout/verify-payment")
    public String verifyPayment(
            @RequestParam("razorpay_payment_id") String paymentId,
            @RequestParam("razorpay_order_id") String orderId,
            @RequestParam("razorpay_signature") String signature,
            RedirectAttributes redirectAttributes) {
        
        try {
            orderService.verifyAndConfirmPayment(orderId, paymentId, signature);
            // Find the internal order to get its DB id
            return "redirect:/order-thankyou?orderId=" + orderService.findByRazorpayOrderId(orderId).map(o -> o.getId()).orElse(0L);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Payment verification failed.");
            return "redirect:/checkout";
        }
    }

    @GetMapping("/order-thankyou")
    public String showThankYouPage(@RequestParam("orderId") Long orderId, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        User user = userService.findByEmail(principal.getName());
        model.addAttribute("user", user);

        Optional<com.theoryofbloom.model.Order> orderOpt = orderService.findById(orderId);
        if (orderOpt.isPresent() && orderOpt.get().getUser() != null && orderOpt.get().getUser().getId().equals(user.getId())) {
            model.addAttribute("order", orderOpt.get());
        }
        return "order-thankyou";
    }
}
