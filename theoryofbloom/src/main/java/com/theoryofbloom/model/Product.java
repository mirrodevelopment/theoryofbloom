package com.theoryofbloom.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String shortDescription;

    @Column(length = 2000)
    private String longDescription;

    @Column(nullable = false)
    private BigDecimal price;

    private String category;  // Sleep & Calm, Skin & Glow, Focus & Clarity, Immunity

    private String imageUrl;

    private String ingredients;

    private String benefits;

    private String howItWorks;

    private String usageInstructions;

    private String whoShouldUse;

    private String directions;

    private String warnings;

    private String storageInstructions;

    private String netWeight;

    private String faq;

    @Column(length = 1000)
    private String subImages; // Comma separated URLs

    private boolean featured;    // for homepage Featured section

    private boolean bestseller;  // for Bestseller section

    private boolean newArrival;  // for New Arrivals section

    private boolean topRated;    // for Top Rated section

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer stockQuantity = 0;


    // Constructors
    public Product() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }

    public String getLongDescription() { return longDescription; }
    public void setLongDescription(String longDescription) { this.longDescription = longDescription; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public String getBenefits() { return benefits; }
    public void setBenefits(String benefits) { this.benefits = benefits; }

    public String getHowItWorks() { return howItWorks; }
    public void setHowItWorks(String howItWorks) { this.howItWorks = howItWorks; }

    public String getUsageInstructions() { return usageInstructions; }
    public void setUsageInstructions(String usageInstructions) { this.usageInstructions = usageInstructions; }

    public String getWhoShouldUse() { return whoShouldUse; }
    public void setWhoShouldUse(String whoShouldUse) { this.whoShouldUse = whoShouldUse; }

    public String getDirections() { return directions; }
    public void setDirections(String directions) { this.directions = directions; }

    public String getWarnings() { return warnings; }
    public void setWarnings(String warnings) { this.warnings = warnings; }

    public String getStorageInstructions() { return storageInstructions; }
    public void setStorageInstructions(String storageInstructions) { this.storageInstructions = storageInstructions; }

    public String getNetWeight() { return netWeight; }
    public void setNetWeight(String netWeight) { this.netWeight = netWeight; }

    public String getFaq() { return faq; }
    public void setFaq(String faq) { this.faq = faq; }

    public boolean isFeatured() { return featured; }
    public void setFeatured(boolean featured) { this.featured = featured; }

    public boolean isBestseller() { return bestseller; }
    public void setBestseller(boolean bestseller) { this.bestseller = bestseller; }

    public boolean isNewArrival() { return newArrival; }
    public void setNewArrival(boolean newArrival) { this.newArrival = newArrival; }

    public boolean isTopRated() { return topRated; }
    public void setTopRated(boolean topRated) { this.topRated = topRated; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public String getSubImages() { return subImages; }
    public void setSubImages(String subImages) { this.subImages = subImages; }
}