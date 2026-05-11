package com.theoryofbloom.controller;

import com.theoryofbloom.model.SiteContent;
import com.theoryofbloom.model.User;
import com.theoryofbloom.repository.UserRepository;
import com.theoryofbloom.service.SiteContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/content")
public class AdminContentRestController {

    @Autowired
    private SiteContentService siteContentService;

    @Autowired
    private UserRepository userRepository;

    private boolean isAdmin(Principal principal) {
        if (principal == null) return false;
        User u = userRepository.findByEmail(principal.getName()).orElse(null);
        return u != null && "ROLE_ADMIN".equals(u.getRole());
    }

    private String saveImage(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path path = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "static", "images", "products");
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

    @PostMapping("/home")
    public ResponseEntity<?> saveHomeContent(
            @RequestParam(required = false) String heroEyebrow,
            @RequestParam(required = false) String heroHeading,
            @RequestParam(required = false) String heroSubtext,
            @RequestParam(required = false) String heroCtaText,
            @RequestParam(required = false) String spinnerCategories,
            @RequestParam(required = false) String philosophyQuote,
            @RequestParam(required = false) String philosophyBody,
            @RequestParam(required = false) String testimonialsTitle,
            @RequestParam(required = false) String ourStoryEyebrow,
            @RequestParam(required = false) String ourStoryTitle,
            @RequestParam(required = false) String ourStoryP1,
            @RequestParam(required = false) String ourStoryP2,
            @RequestParam(required = false) String ourStoryHighlight,
            @RequestParam(required = false) String ritualEyebrow,
            @RequestParam(required = false) String ritualHeading,
            @RequestParam(required = false) String ritualBody,
            @RequestParam(required = false) String ritualImageUrl,
            @RequestParam(value = "testiName", required = false) List<String> testiName,
            @RequestParam(value = "testiLocation", required = false) List<String> testiLocation,
            @RequestParam(value = "testiStars", required = false) List<String> testiStars,
            @RequestParam(value = "testiQuote", required = false) List<String> testiQuote,
            @RequestParam(required = false) MultipartFile heroImageFile,
            @RequestParam(required = false) MultipartFile ritualImageFile,
            Principal principal) {
        
        if (!isAdmin(principal)) return ResponseEntity.status(403).body(Map.of("success", false, "message", "Unauthorized"));
        
        try {
            SiteContent sc = siteContentService.load();
            SiteContent.HomeContent h = sc.getHome();
            if (heroEyebrow != null) h.setHeroEyebrow(heroEyebrow);
            if (heroHeading != null) h.setHeroHeading(heroHeading);
            if (heroSubtext != null) h.setHeroSubtext(heroSubtext);
            if (heroCtaText != null) h.setHeroCtaText(heroCtaText);
            if (spinnerCategories != null) h.setSpinnerCategories(spinnerCategories);
            if (philosophyQuote != null) h.setPhilosophyQuote(philosophyQuote);
            if (philosophyBody != null) h.setPhilosophyBody(philosophyBody);
            if (testimonialsTitle != null) h.setTestimonialsTitle(testimonialsTitle);
            if (ourStoryEyebrow != null) h.setOurStoryEyebrow(ourStoryEyebrow);
            if (ourStoryTitle != null) h.setOurStoryTitle(ourStoryTitle);
            if (ourStoryP1 != null) h.setOurStoryP1(ourStoryP1);
            if (ourStoryP2 != null) h.setOurStoryP2(ourStoryP2);
            if (ourStoryHighlight != null) h.setOurStoryHighlight(ourStoryHighlight);
            if (ritualEyebrow != null) h.setRitualEyebrow(ritualEyebrow);
            if (ritualHeading != null) h.setRitualHeading(ritualHeading);
            if (ritualBody != null) h.setRitualBody(ritualBody);
            
            if (ritualImageUrl != null && !ritualImageUrl.isEmpty()) {
                h.setRitualImageUrl(ritualImageUrl);
            }
            if (ritualImageFile != null && !ritualImageFile.isEmpty()) {
                String savedImageUrl = saveImage(ritualImageFile);
                if (savedImageUrl != null) h.setRitualImageUrl(savedImageUrl);
            }

            if (heroImageFile != null && !heroImageFile.isEmpty()) {
                String savedImageUrl = saveImage(heroImageFile);
                if (savedImageUrl != null) h.setHeroImageUrl(savedImageUrl);
            }
            
            if (testiName != null && !testiName.isEmpty()) {
                List<SiteContent.Testimonial> testimonials = new ArrayList<>();
                for (int i = 0; i < testiName.size(); i++) {
                    SiteContent.Testimonial t = new SiteContent.Testimonial();
                    t.setName(testiName.get(i));
                    t.setLocation(i < testiLocation.size() ? testiLocation.get(i) : "");
                    t.setStars(i < testiStars.size() ? testiStars.get(i) : "★★★★★");
                    t.setQuote(i < testiQuote.size() ? testiQuote.get(i) : "");
                    if (!t.getName().isBlank()) testimonials.add(t);
                }
                if (!testimonials.isEmpty()) h.setTestimonials(testimonials);
            }
            
            sc.setHome(h);
            siteContentService.save(sc);
            return ResponseEntity.ok(Map.of("success", true, "message", "Home page content updated instantly"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/shop")
    public ResponseEntity<?> saveShopContent(
            @RequestParam(required = false) String heroBannerTitle,
            @RequestParam(required = false) String heroBannerSubtitle,
            @RequestParam(required = false) String filterLabel,
            @RequestParam(required = false) String filterCategories,
            @RequestParam(required = false) String footerTagline,
            Principal principal) {
        
        if (!isAdmin(principal)) return ResponseEntity.status(403).body(Map.of("success", false, "message", "Unauthorized"));
        
        try {
            SiteContent sc = siteContentService.load();
            SiteContent.ShopContent s = sc.getShop();
            if (heroBannerTitle != null) s.setHeroBannerTitle(heroBannerTitle);
            if (heroBannerSubtitle != null) s.setHeroBannerSubtitle(heroBannerSubtitle);
            if (filterLabel != null) s.setFilterLabel(filterLabel);
            if (filterCategories != null) s.setFilterCategories(filterCategories);
            if (footerTagline != null) s.setFooterTagline(footerTagline);
            sc.setShop(s);
            siteContentService.save(sc);
            return ResponseEntity.ok(Map.of("success", true, "message", "Shop content updated instantly"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/contact")
    public ResponseEntity<?> saveContactContent(
            @RequestParam(required = false) String bannerEyebrow,
            @RequestParam(required = false) String bannerTitle,
            @RequestParam(required = false) String bannerSubtitle,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String hours,
            @RequestParam(required = false) String tagline,
            @RequestParam(required = false) String mapsUrl,
            Principal principal) {
        
        if (!isAdmin(principal)) return ResponseEntity.status(403).body(Map.of("success", false, "message", "Unauthorized"));
        
        try {
            SiteContent sc = siteContentService.load();
            SiteContent.ContactContent c = sc.getContact();
            if (bannerEyebrow != null) c.setBannerEyebrow(bannerEyebrow);
            if (bannerTitle != null) c.setBannerTitle(bannerTitle);
            if (bannerSubtitle != null) c.setBannerSubtitle(bannerSubtitle);
            if (email != null) c.setEmail(email);
            if (address != null) c.setAddress(address);
            if (hours != null) c.setHours(hours);
            if (tagline != null) c.setTagline(tagline);
            if (mapsUrl != null) c.setMapsUrl(mapsUrl);
            sc.setContact(c);
            siteContentService.save(sc);
            return ResponseEntity.ok(Map.of("success", true, "message", "Contact content updated instantly"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/order")
    public ResponseEntity<?> saveOrderContent(
            @RequestParam(required = false) String pageTitle,
            @RequestParam(required = false) String pageSubtitle,
            @RequestParam(required = false) String emptyStateMessage,
            @RequestParam(required = false) String emptyStateCtaText,
            Principal principal) {
        
        if (!isAdmin(principal)) return ResponseEntity.status(403).body(Map.of("success", false, "message", "Unauthorized"));
        
        try {
            SiteContent sc = siteContentService.load();
            SiteContent.OrderContent o = sc.getOrder();
            if (pageTitle != null) o.setPageTitle(pageTitle);
            if (pageSubtitle != null) o.setPageSubtitle(pageSubtitle);
            if (emptyStateMessage != null) o.setEmptyStateMessage(emptyStateMessage);
            if (emptyStateCtaText != null) o.setEmptyStateCtaText(emptyStateCtaText);
            sc.setOrder(o);
            siteContentService.save(sc);
            return ResponseEntity.ok(Map.of("success", true, "message", "Order content updated instantly"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/profile")
    public ResponseEntity<?> saveProfileContent(
            @RequestParam(required = false) String pageTitle,
            @RequestParam(required = false) String pageSubtitle,
            @RequestParam(required = false) String membershipBannerTitle,
            @RequestParam(required = false) String membershipBannerBody,
            @RequestParam(required = false) String pointsLabel,
            @RequestParam(required = false) String pointsDescription,
            Principal principal) {
        
        if (!isAdmin(principal)) return ResponseEntity.status(403).body(Map.of("success", false, "message", "Unauthorized"));
        
        try {
            SiteContent sc = siteContentService.load();
            SiteContent.ProfileContent p = sc.getProfile();
            if (pageTitle != null) p.setPageTitle(pageTitle);
            if (pageSubtitle != null) p.setPageSubtitle(pageSubtitle);
            if (membershipBannerTitle != null) p.setMembershipBannerTitle(membershipBannerTitle);
            if (membershipBannerBody != null) p.setMembershipBannerBody(membershipBannerBody);
            if (pointsLabel != null) p.setPointsLabel(pointsLabel);
            if (pointsDescription != null) p.setPointsDescription(pointsDescription);
            sc.setProfile(p);
            siteContentService.save(sc);
            return ResponseEntity.ok(Map.of("success", true, "message", "Profile content updated instantly"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/footer")
    public ResponseEntity<?> saveFooterContent(
            @RequestParam(required = false) String tagline,
            @RequestParam(required = false) String copyrightText,
            @RequestParam(required = false) String bottomTagline,
            @RequestParam(required = false) String contactEmail,
            Principal principal) {
        
        if (!isAdmin(principal)) return ResponseEntity.status(403).body(Map.of("success", false, "message", "Unauthorized"));
        
        try {
            SiteContent sc = siteContentService.load();
            SiteContent.FooterContent f = sc.getFooter();
            if (tagline != null) f.setTagline(tagline);
            if (copyrightText != null) f.setCopyrightText(copyrightText);
            if (bottomTagline != null) f.setBottomTagline(bottomTagline);
            if (contactEmail != null) f.setContactEmail(contactEmail);
            sc.setFooter(f);
            siteContentService.save(sc);
            return ResponseEntity.ok(Map.of("success", true, "message", "Footer content updated instantly"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
