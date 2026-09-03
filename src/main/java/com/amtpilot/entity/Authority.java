package com.amtpilot.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "authority")
public class Authority {

    public Authority() {
    }

    public Authority(
            String name,
            String authorityType,
            String city,
            String officialUrl,
            String contactUrl) {
        this.name = name;
        this.authorityType = authorityType;
        this.city = city;
        this.officialUrl = officialUrl;
        this.contactUrl = contactUrl;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "authority_type", nullable = false, length = 60)
    private String authorityType;

    @Column(nullable = false, length = 120)
    private String city;

    @Column(name = "official_url", nullable = false, length = 2048)
    private String officialUrl;

    @Column(name = "contact_url", length = 2048)
    private String contactUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAuthorityType() {
        return authorityType;
    }

    public String getCity() {
        return city;
    }

    public String getOfficialUrl() {
        return officialUrl;
    }

    public String getContactUrl() {
        return contactUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}