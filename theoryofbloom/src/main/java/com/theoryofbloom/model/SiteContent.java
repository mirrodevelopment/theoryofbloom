package com.theoryofbloom.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;


/**
 * Root POJO that holds all editable site content.
 * Persisted as a single JSON file: content/site-content.json
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SiteContent {

    @Valid
    @JsonProperty("home")
    private HomeContent home = new HomeContent();
    @Valid
    @JsonProperty("shop")
    private ShopContent shop = new ShopContent();
    @Valid
    @JsonProperty("contact")
    private ContactContent contact = new ContactContent();
    @Valid
    @JsonProperty("order")
    private OrderContent order = new OrderContent();
    @Valid
    @JsonProperty("profile")
    private ProfileContent profile = new ProfileContent();
    @Valid
    @JsonProperty("footer")
    private FooterContent footer = new FooterContent();

    // ── Getters / Setters (null-safe: return fresh defaults if Jackson set field to null) ────

    public HomeContent getHome() { return home != null ? home : (home = new HomeContent()); }
    public void setHome(HomeContent home) { this.home = home; }

    public ShopContent getShop() { return shop != null ? shop : (shop = new ShopContent()); }
    public void setShop(ShopContent shop) { this.shop = shop; }

    public ContactContent getContact() { return contact != null ? contact : (contact = new ContactContent()); }
    public void setContact(ContactContent contact) { this.contact = contact; }

    public OrderContent getOrder() { return order != null ? order : (order = new OrderContent()); }
    public void setOrder(OrderContent order) { this.order = order; }

    public ProfileContent getProfile() { return profile != null ? profile : (profile = new ProfileContent()); }
    public void setProfile(ProfileContent profile) { this.profile = profile; }

    public FooterContent getFooter() { return footer != null ? footer : (footer = new FooterContent()); }
    public void setFooter(FooterContent footer) { this.footer = footer; }

    // ═══════════════════════════════════════════════════════════════════
    // HOME PAGE
    // ═══════════════════════════════════════════════════════════════════
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HomeContent {
        @NotBlank(message = "Hero eyebrow cannot be empty")
        @JsonProperty("heroEyebrow")
        private String heroEyebrow    = "Botanical Wellness";
        @NotBlank(message = "Hero heading cannot be empty")
        @JsonProperty("heroHeading")
        private String heroHeading    = "A new theory of blooming from within";
        @JsonProperty("heroSubtext")
        private String heroSubtext    = "Crafted by nature. Perfected by science.";
        @JsonProperty("heroCtaText")
        private String heroCtaText    = "Shop Blends";
        @JsonProperty("heroImageUrl")
        private String heroImageUrl   = "";
        @JsonProperty("spinnerCategories")
        private String spinnerCategories = "Sleep & Calm,Skin & Glow,Focus & Clarity,Immunity,Masculine,Morning";

        @JsonProperty("philosophyQuote")
        private String philosophyQuote = "At Theory of Bloom, wellness is not a quick fix — it is a quiet, daily unfolding.";
        @JsonProperty("philosophyBody")
        private String philosophyBody  = "Rooted in nature and refined through thoughtful formulation, our blends are created to support the body gently, yet effectively.";

        @JsonProperty("testimonialsTitle")
        private String testimonialsTitle = "What Our Community Feels";
        @JsonProperty("testimonials")
        private List<Testimonial> testimonials = new ArrayList<>();

        @JsonProperty("ourStoryEyebrow")
        private String ourStoryEyebrow  = "Est. 2024 · Botanical Wellness";
        @JsonProperty("ourStoryTitle")
        private String ourStoryTitle    = "Our Story";
        @JsonProperty("ourStoryP1")
        private String ourStoryP1       = "Theory of Bloom was created from a simple belief that wellness should feel natural, not complicated.";
        @JsonProperty("ourStoryP2")
        private String ourStoryP2       = "In a world of quick fixes, we choose to slow down, return to nature, and create blends that support the body gently yet meaningfully.";
        @JsonProperty("ourStoryHighlight")
        private String ourStoryHighlight= "This is our theory — that when given the right care, the body knows how to bloom.";

        @JsonProperty("ritualEyebrow")
        private String ritualEyebrow = "Your Daily Practice";
        @JsonProperty("ritualHeading")
        private String ritualHeading = "A Daily Ritual, Not Just a Drink.";
        @JsonProperty("ritualBody")
        private String ritualBody    = "Slow down. Sip intentionally. Let your body find its natural rhythm.";
        @JsonProperty("ritualImageUrl")
        private String ritualImageUrl= "/images/ritual-tea.jpg";

        // getters/setters
        public String getHeroEyebrow() { return heroEyebrow; }
        public void setHeroEyebrow(String v) { this.heroEyebrow = v; }

        public String getHeroHeading() { return heroHeading; }
        public void setHeroHeading(String v) { this.heroHeading = v; }

        public String getHeroSubtext() { return heroSubtext; }
        public void setHeroSubtext(String v) { this.heroSubtext = v; }

        public String getHeroCtaText() { return heroCtaText; }
        public void setHeroCtaText(String v) { this.heroCtaText = v; }

        public String getHeroImageUrl() { return heroImageUrl; }
        public void setHeroImageUrl(String v) { this.heroImageUrl = v; }

        public String getSpinnerCategories() { return spinnerCategories != null ? spinnerCategories : "Sleep & Calm,Skin & Glow,Focus & Clarity,Immunity,Masculine,Morning"; }
        public void setSpinnerCategories(String v) { this.spinnerCategories = v; }

        public String getPhilosophyQuote() { return philosophyQuote; }
        public void setPhilosophyQuote(String v) { this.philosophyQuote = v; }

        public String getPhilosophyBody() { return philosophyBody; }
        public void setPhilosophyBody(String v) { this.philosophyBody = v; }

        public String getTestimonialsTitle() { return testimonialsTitle; }
        public void setTestimonialsTitle(String v) { this.testimonialsTitle = v; }

        public List<Testimonial> getTestimonials() { return testimonials; }
        public void setTestimonials(List<Testimonial> v) { this.testimonials = v; }

        public String getOurStoryEyebrow() { return ourStoryEyebrow; }
        public void setOurStoryEyebrow(String v) { this.ourStoryEyebrow = v; }

        public String getOurStoryTitle() { return ourStoryTitle; }
        public void setOurStoryTitle(String v) { this.ourStoryTitle = v; }

        public String getOurStoryP1() { return ourStoryP1; }
        public void setOurStoryP1(String v) { this.ourStoryP1 = v; }

        public String getOurStoryP2() { return ourStoryP2; }
        public void setOurStoryP2(String v) { this.ourStoryP2 = v; }

        public String getOurStoryHighlight() { return ourStoryHighlight; }
        public void setOurStoryHighlight(String v) { this.ourStoryHighlight = v; }

        public String getRitualEyebrow() { return ritualEyebrow; }
        public void setRitualEyebrow(String v) { this.ritualEyebrow = v; }

        public String getRitualHeading() { return ritualHeading; }
        public void setRitualHeading(String v) { this.ritualHeading = v; }

        public String getRitualBody() { return ritualBody; }
        public void setRitualBody(String v) { this.ritualBody = v; }

        public String getRitualImageUrl() { return ritualImageUrl; }
        public void setRitualImageUrl(String v) { this.ritualImageUrl = v; }
    }

    // ═══════════════════════════════════════════════════════════════════
    // TESTIMONIAL (nested)
    // ═══════════════════════════════════════════════════════════════════
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Testimonial {
        @JsonProperty("name")
        private String name     = "";
        @JsonProperty("location")
        private String location = "";
        @JsonProperty("stars")
        private String stars    = "★★★★★";
        @JsonProperty("quote")
        private String quote    = "";

        public String getName() { return name; }
        public void setName(String v) { this.name = v; }

        public String getLocation() { return location; }
        public void setLocation(String v) { this.location = v; }

        public String getStars() { return stars; }
        public void setStars(String v) { this.stars = v; }

        public String getQuote() { return quote; }
        public void setQuote(String v) { this.quote = v; }
    }

    // ═══════════════════════════════════════════════════════════════════
    // SHOP PAGE
    // ═══════════════════════════════════════════════════════════════════
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ShopContent {
        @NotBlank(message = "Hero banner title cannot be empty")
        @JsonProperty("heroBannerTitle")
        private String heroBannerTitle    = "The Master Curations";
        @JsonProperty("heroBannerSubtitle")
        private String heroBannerSubtitle = "Discover botanical alchemy designed for your well-being. Formulated with precision, elevated by nature.";
        @JsonProperty("filterLabel")
        private String filterLabel        = "Shop by Category";
        @JsonProperty("filterCategories")
        private String filterCategories   = "Sleep & Calm,Skin & Glow,Focus & Clarity,Immunity,Masculine,Morning";
        @JsonProperty("footerTagline")
        private String footerTagline      = "Crafted by Nature. Perfected by Science.";

        public String getHeroBannerTitle() { return heroBannerTitle != null ? heroBannerTitle : "The Master Curations"; }
        public void setHeroBannerTitle(String v) { this.heroBannerTitle = v; }

        public String getHeroBannerSubtitle() { return heroBannerSubtitle != null ? heroBannerSubtitle : "Discover botanical alchemy designed for your well-being."; }
        public void setHeroBannerSubtitle(String v) { this.heroBannerSubtitle = v; }

        public String getFilterLabel() { return filterLabel != null ? filterLabel : "Shop by Category"; }
        public void setFilterLabel(String v) { this.filterLabel = v; }

        public String getFilterCategories() { return filterCategories != null ? filterCategories : "Sleep & Calm,Skin & Glow,Focus & Clarity,Immunity,Masculine,Morning"; }
        public void setFilterCategories(String v) { this.filterCategories = v; }

        public String getFooterTagline() { return footerTagline != null ? footerTagline : "Crafted by Nature. Perfected by Science."; }
        public void setFooterTagline(String v) { this.footerTagline = v; }
    }

    // ═══════════════════════════════════════════════════════════════════
    // CONTACT PAGE
    // ═══════════════════════════════════════════════════════════════════
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContactContent {
        @JsonProperty("bannerEyebrow")
        private String bannerEyebrow = "We're Here to Help";
        @NotBlank(message = "Banner title cannot be empty")
        @JsonProperty("bannerTitle")
        private String bannerTitle   = "Get in Touch";
        @JsonProperty("bannerSubtitle")
        private String bannerSubtitle= "Questions, feedback or wholesale enquiries — we'd love to hear from you.";
        @NotBlank(message = "Email cannot be empty")
        @JsonProperty("email")
        private String email         = "hello@theoryofbloom.com";
        @JsonProperty("address")
        private String address       = "Coimbatore, Tamil Nadu";
        @JsonProperty("hours")
        private String hours         = "Mon – Sat: 9am – 6pm IST";
        @JsonProperty("tagline")
        private String tagline       = "We usually respond within 24 hours.";
        @JsonProperty("mapsUrl")
        private String mapsUrl       = "";

        public String getBannerEyebrow() { return bannerEyebrow; }
        public void setBannerEyebrow(String v) { this.bannerEyebrow = v; }

        public String getBannerTitle() { return bannerTitle; }
        public void setBannerTitle(String v) { this.bannerTitle = v; }

        public String getBannerSubtitle() { return bannerSubtitle; }
        public void setBannerSubtitle(String v) { this.bannerSubtitle = v; }

        public String getEmail() { return email; }
        public void setEmail(String v) { this.email = v; }

        public String getAddress() { return address; }
        public void setAddress(String v) { this.address = v; }

        public String getHours() { return hours; }
        public void setHours(String v) { this.hours = v; }

        public String getTagline() { return tagline; }
        public void setTagline(String v) { this.tagline = v; }

        public String getMapsUrl() { return mapsUrl; }
        public void setMapsUrl(String v) { this.mapsUrl = v; }
    }

    // ═══════════════════════════════════════════════════════════════════
    // ORDER PAGE
    // ═══════════════════════════════════════════════════════════════════
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderContent {
        @JsonProperty("pageTitle")
        private String pageTitle          = "Your Orders";
        @JsonProperty("pageSubtitle")
        private String pageSubtitle       = "Track and manage your botanical ritual orders.";
        @JsonProperty("emptyStateMessage")
        private String emptyStateMessage  = "You haven't placed any orders yet.";
        @JsonProperty("emptyStateCtaText")
        private String emptyStateCtaText  = "Explore Our Blends";

        public String getPageTitle() { return pageTitle; }
        public void setPageTitle(String v) { this.pageTitle = v; }

        public String getPageSubtitle() { return pageSubtitle; }
        public void setPageSubtitle(String v) { this.pageSubtitle = v; }

        public String getEmptyStateMessage() { return emptyStateMessage; }
        public void setEmptyStateMessage(String v) { this.emptyStateMessage = v; }

        public String getEmptyStateCtaText() { return emptyStateCtaText; }
        public void setEmptyStateCtaText(String v) { this.emptyStateCtaText = v; }
    }

    // ═══════════════════════════════════════════════════════════════════
    // PROFILE PAGE
    // ═══════════════════════════════════════════════════════════════════
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProfileContent {
        @JsonProperty("pageTitle")
        private String pageTitle              = "My Profile";
        @JsonProperty("pageSubtitle")
        private String pageSubtitle           = "Manage your account, orders and preferences.";
        @JsonProperty("membershipBannerTitle")
        private String membershipBannerTitle  = "Upgrade to Premium";
        @JsonProperty("membershipBannerBody")
        private String membershipBannerBody   = "Unlock exclusive blends, early access and free delivery.";
        @JsonProperty("pointsLabel")
        private String pointsLabel            = "Bloom Points";
        @JsonProperty("pointsDescription")
        private String pointsDescription      = "Earn points with every purchase and redeem for rewards.";

        public String getPageTitle() { return pageTitle; }
        public void setPageTitle(String v) { this.pageTitle = v; }

        public String getPageSubtitle() { return pageSubtitle; }
        public void setPageSubtitle(String v) { this.pageSubtitle = v; }

        public String getMembershipBannerTitle() { return membershipBannerTitle; }
        public void setMembershipBannerTitle(String v) { this.membershipBannerTitle = v; }

        public String getMembershipBannerBody() { return membershipBannerBody; }
        public void setMembershipBannerBody(String v) { this.membershipBannerBody = v; }

        public String getPointsLabel() { return pointsLabel; }
        public void setPointsLabel(String v) { this.pointsLabel = v; }

        public String getPointsDescription() { return pointsDescription; }
        public void setPointsDescription(String v) { this.pointsDescription = v; }
    }

    // ═══════════════════════════════════════════════════════════════════
    // FOOTER
    // ═══════════════════════════════════════════════════════════════════
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FooterContent {
        @JsonProperty("tagline")
        private String tagline       = "Botanical wellness blends crafted by nature, perfected by science.";
        @JsonProperty("copyrightText")
        private String copyrightText = "© 2026 Theory of Bloom. All rights reserved.";
        @JsonProperty("bottomTagline")
        private String bottomTagline = "Crafted by Nature. Perfected by Science.";
        @JsonProperty("contactEmail")
        private String contactEmail  = "hello@theoryofbloom.com";

        public String getTagline() { return tagline != null ? tagline : "Botanical wellness blends crafted by nature, perfected by science."; }
        public void setTagline(String v) { this.tagline = v; }

        public String getCopyrightText() { return copyrightText != null ? copyrightText : "\u00a9 2026 Theory of Bloom. All rights reserved."; }
        public void setCopyrightText(String v) { this.copyrightText = v; }

        public String getBottomTagline() { return bottomTagline != null ? bottomTagline : "Crafted by Nature. Perfected by Science."; }
        public void setBottomTagline(String v) { this.bottomTagline = v; }

        public String getContactEmail() { return contactEmail != null ? contactEmail : "hello@theoryofbloom.com"; }
        public void setContactEmail(String v) { this.contactEmail = v; }
    }
}
