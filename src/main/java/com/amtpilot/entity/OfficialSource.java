package com.amtpilot.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "official_source")
public class OfficialSource {

    public OfficialSource() {
    }

    public OfficialSource(
            Authority authority,
            String url,
            String title,
            String city,
            String language) {
        this.authority = authority;
        this.url = url;
        this.title = title;
        this.city = city;
        this.language = language;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authority_id")
    private Authority authority;

    @Column(nullable = false, unique = true, length = 2048)
    private String url;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, length = 120)
    private String city;

    @Column(nullable = false, length = 10)
    private String language;

    @Column(name = "fetched_at")
    private Instant fetchedAt;

    @Column(length = 64)
    private String checksum;

    @Column(nullable = false, length = 30)
    private String status = "NEEDS_REVIEW";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getId() {
        return id;
    }

    public Authority getAuthority() {
        return authority;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getCity() {
        return city;
    }

    public String getLanguage() {
        return language;
    }

    public Instant getFetchedAt() {
        return fetchedAt;
    }

    public String getChecksum() {
        return checksum;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}