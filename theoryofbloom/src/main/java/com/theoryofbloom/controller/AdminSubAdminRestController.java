package com.theoryofbloom.controller;

import com.theoryofbloom.model.User;
import com.theoryofbloom.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

/**
 * Handles Sub-Admin promotion with secure main-admin password verification.
 * ROLE_SUB_ADMIN: can view orders/products but cannot delete or promote others.
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminSubAdminRestController {

    private static final String MAIN_ADMIN_EMAIL = "admin@theoryofbloom.com";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private boolean isMainAdmin(Principal principal) {
        if (principal == null) return false;
        return MAIN_ADMIN_EMAIL.equals(principal.getName());
    }

    /** Promote a ROLE_USER to ROLE_SUB_ADMIN after verifying main-admin password */
    @PostMapping("/{id}/promote-sub-admin")
    public ResponseEntity<?> promoteToSubAdmin(
            @PathVariable Long id,
            @RequestParam String adminPassword,
            Principal principal) {

        if (!isMainAdmin(principal)) {
            return ResponseEntity.status(403)
                    .body(Map.of("success", false, "message", "Only the Main Admin can promote users to Sub-Admin."));
        }

        // Verify main-admin password
        User mainAdmin = userRepository.findByEmail(MAIN_ADMIN_EMAIL).orElse(null);
        if (mainAdmin == null || !passwordEncoder.matches(adminPassword, mainAdmin.getPassword())) {
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "Invalid Main Admin password. Promotion denied."));
        }

        User target = userRepository.findById(id).orElse(null);
        if (target == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "User not found."));
        }
        if ("ROLE_ADMIN".equals(target.getRole())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "User is already a full Admin."));
        }
        if (MAIN_ADMIN_EMAIL.equals(target.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Cannot modify the Main Admin account."));
        }

        target.setRole("ROLE_SUB_ADMIN");
        userRepository.save(target);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "'" + target.getFullName() + "' has been promoted to Sub-Admin.",
            "newRole", "ROLE_SUB_ADMIN"
        ));
    }

    /** Demote a Sub-Admin back to regular user (also requires main-admin password) */
    @PostMapping("/{id}/demote-sub-admin")
    public ResponseEntity<?> demoteSubAdmin(
            @PathVariable Long id,
            @RequestParam String adminPassword,
            Principal principal) {

        if (!isMainAdmin(principal)) {
            return ResponseEntity.status(403)
                    .body(Map.of("success", false, "message", "Only the Main Admin can demote Sub-Admins."));
        }

        User mainAdmin = userRepository.findByEmail(MAIN_ADMIN_EMAIL).orElse(null);
        if (mainAdmin == null || !passwordEncoder.matches(adminPassword, mainAdmin.getPassword())) {
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "Invalid Main Admin password."));
        }

        User target = userRepository.findById(id).orElse(null);
        if (target == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not found."));
        }
        if (!"ROLE_SUB_ADMIN".equals(target.getRole())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User is not a Sub-Admin."));
        }

        target.setRole("ROLE_USER");
        userRepository.save(target);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "'" + target.getFullName() + "' demoted to regular User.",
            "newRole", "ROLE_USER"
        ));
    }
}
