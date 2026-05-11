package com.theoryofbloom.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String address;
    private String city;
    private String state;
    private String pincode;

    private String phone;
    private String altPhone;

    private String dob;
    private String gender;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "reset_token")
    private String resetToken;

    // Roles: ROLE_USER, ROLE_ADMIN, ROLE_SUB_ADMIN
    private String role = "ROLE_USER";

    @Column(name = "is_blocked")
    private Boolean blocked = false;

    // Premium status
    @Column(name = "is_premium")
    private Boolean premium = false;

    @Column(name = "premium_expiry_date")
    private LocalDateTime premiumExpiryDate;

    // Bloom loyalty points
    @Column(name = "bloom_points")
    private Integer points = 0;

    // Constructors
    public User() {}

    public User(String fullName, String email, String password) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAltPhone() { return altPhone; }
    public void setAltPhone(String altPhone) { this.altPhone = altPhone; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isPremium() { return premium != null ? premium : false; }
    public void setPremium(Boolean premium) { this.premium = premium; }

    public LocalDateTime getPremiumExpiryDate() { return premiumExpiryDate; }
    public void setPremiumExpiryDate(LocalDateTime premiumExpiryDate) { this.premiumExpiryDate = premiumExpiryDate; }

    public Integer getPoints() { return points != null ? points : 0; }
    public void setPoints(Integer points) { this.points = points; }

    public boolean isBlocked() { return blocked != null ? blocked : false; }
    public void setBlocked(Boolean blocked) { this.blocked = blocked; }
}