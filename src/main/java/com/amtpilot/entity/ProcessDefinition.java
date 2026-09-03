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
@Table(name = "process_definition")
public class ProcessDefinition {

    public ProcessDefinition() {
    }

    public ProcessDefinition(
            Authority authority,
            String code,
            String title,
            String city,
            String domain) {
        this.authority = authority;
        this.code = code;
        this.title = title;
        this.city = city;
        this.domain = domain;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authority_id")
    private Authority authority;

    @Column(nullable = false, unique = true, length = 120)
    private String code;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 120)
    private String city;

    @Column(nullable = false, length = 80)
    private String domain;

    @Column(nullable = false)
    private int version = 1;

    @Column(nullable = false)
    private boolean active = true;

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

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getCity() {
        return city;
    }

    public String getDomain() {
        return domain;
    }

    public int getVersion() {
        return version;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}