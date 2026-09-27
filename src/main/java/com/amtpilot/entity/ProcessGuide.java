package com.amtpilot.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "process_guide")
public class ProcessGuide {

    public ProcessGuide() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "process_id", nullable = false, unique = true)
    private ProcessDefinition process;

    @Column(name = "overview_en", nullable = false, columnDefinition = "TEXT")
    private String overviewEn;

    @Column(name = "overview_de", nullable = false, columnDefinition = "TEXT")
    private String overviewDe;

    @Column(name = "eligibility_en", nullable = false, columnDefinition = "TEXT")
    private String eligibilityEn;

    @Column(name = "eligibility_de", nullable = false, columnDefinition = "TEXT")
    private String eligibilityDe;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "steps_en", nullable = false, columnDefinition = "jsonb")
    private List<String> stepsEn;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "steps_de", nullable = false, columnDefinition = "jsonb")
    private List<String> stepsDe;

    @Column(name = "deadline_en", nullable = false, columnDefinition = "TEXT")
    private String deadlineEn;

    @Column(name = "deadline_de", nullable = false, columnDefinition = "TEXT")
    private String deadlineDe;

    @Column(name = "fee_en", nullable = false, columnDefinition = "TEXT")
    private String feeEn;

    @Column(name = "fee_de", nullable = false, columnDefinition = "TEXT")
    private String feeDe;

    @Column(name = "appointment_required", nullable = false)
    private boolean appointmentRequired;

    @Column(name = "appointment_information_en", nullable = false, columnDefinition = "TEXT")
    private String appointmentInformationEn;

    @Column(name = "appointment_information_de", nullable = false, columnDefinition = "TEXT")
    private String appointmentInformationDe;

    @Column(name = "appointment_url", length = 2048)
    private String appointmentUrl;

    @Column(name = "source_title", nullable = false, length = 500)
    private String sourceTitle;

    @Column(name = "source_url", nullable = false, length = 2048)
    private String sourceUrl;

    @Column(name = "verified_at", nullable = false)
    private LocalDate verifiedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getId() {
        return id;
    }

    public ProcessDefinition getProcess() {
        return process;
    }

    public String getOverviewEn() {
        return overviewEn;
    }

    public String getOverviewDe() {
        return overviewDe;
    }

    public String getEligibilityEn() {
        return eligibilityEn;
    }

    public String getEligibilityDe() {
        return eligibilityDe;
    }

    public List<String> getStepsEn() {
        return stepsEn;
    }

    public List<String> getStepsDe() {
        return stepsDe;
    }

    public String getDeadlineEn() {
        return deadlineEn;
    }

    public String getDeadlineDe() {
        return deadlineDe;
    }

    public String getFeeEn() {
        return feeEn;
    }

    public String getFeeDe() {
        return feeDe;
    }

    public boolean isAppointmentRequired() {
        return appointmentRequired;
    }

    public String getAppointmentInformationEn() {
        return appointmentInformationEn;
    }

    public String getAppointmentInformationDe() {
        return appointmentInformationDe;
    }

    public String getAppointmentUrl() {
        return appointmentUrl;
    }

    public String getSourceTitle() {
        return sourceTitle;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public LocalDate getVerifiedAt() {
        return verifiedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}