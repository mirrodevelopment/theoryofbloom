package com.theoryofbloom.controller;

import com.theoryofbloom.model.SiteContent;
import com.theoryofbloom.model.User;
import com.theoryofbloom.repository.UserRepository;
import com.theoryofbloom.service.SiteContentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(GlobalControllerAdvice.class);

    @Autowired
    private SiteContentService siteContentService;

    @Autowired
    private UserRepository userRepository;

    @ModelAttribute("siteContent")
    public SiteContent globalSiteContent() {
        try {
            SiteContent sc = siteContentService.load();
            // Ensure no top-level section is null (guard against Jackson null-injection)
            if (sc == null) return new SiteContent();
            return sc;
        } catch (Exception e) {
            logger.error("GlobalControllerAdvice: failed to load SiteContent, using defaults. Reason: {}", e.getMessage());
            return new SiteContent();
        }
    }

    @ModelAttribute("user")
    public User globalUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
                return userRepository.findByEmail(auth.getName()).orElse(null);
            }
        } catch (Exception e) {
            logger.warn("GlobalControllerAdvice: could not resolve current user. Reason: {}", e.getMessage());
        }
        return null;
    }
}
