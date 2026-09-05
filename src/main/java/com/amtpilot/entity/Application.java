package com.amtpilot.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.amtpilot.enums.ApplicationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "application")
public class Application {

    public Application() {
    }

    public Application(User user, ProcessDefinition process) {
        this.user = user;
        this.process = process;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "process_id", nullable = false)
    private ProcessDefinition process;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ApplicationStatus status = ApplicationStatus.DRAFT;

    @Column(nullable = false)
    private short completeness = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public ProcessDefinition getProcess() {
        return process;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public short getCompleteness() {
        return completeness;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void changeStatus(ApplicationStatus status) {
        this.status = status;
    }

    public void updateCompleteness(int completeness) {
        if (completeness < 0 || completeness > 100) {
            throw new IllegalArgumentException(
                    "Completeness must be between 0 and 100");
        }

        this.completeness = (short) completeness;
    }
}