package com.amtpilot.entity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import com.amtpilot.ai.dto.AiApplicationAdviceResponse;
import com.amtpilot.ai.dto.AiRequirementAssessmentResponse;

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
@Table(name = "application_advice")
public class ApplicationAdvice {

    public ApplicationAdvice() {
    }

    public ApplicationAdvice(
            Application application,
            AiApplicationAdviceResponse advice) {

        this.application = application;
        update(advice);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private Application application;

    @Column(nullable = false, length = 40)
    private String readiness;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "requirement_assessments", nullable = false, columnDefinition = "jsonb")
    private List<AiRequirementAssessmentResponse> requirementAssessments;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<String> inconsistencies;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "next_steps", nullable = false, columnDefinition = "jsonb")
    private List<String> nextSteps;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "questions_for_user", nullable = false, columnDefinition = "jsonb")
    private List<String> questionsForUser;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String disclaimer;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void update(AiApplicationAdviceResponse advice) {
        this.readiness = advice.readiness();
        this.summary = advice.summary();
        this.requirementAssessments = List.copyOf(
                advice.requirementAssessments());
        this.inconsistencies = List.copyOf(
                advice.inconsistencies());
        this.nextSteps = List.copyOf(
                advice.nextSteps());
        this.questionsForUser = List.copyOf(
                advice.questionsForUser());
        this.disclaimer = advice.disclaimer();
    }

    public UUID getId() {
        return id;
    }

    public Application getApplication() {
        return application;
    }

    public String getReadiness() {
        return readiness;
    }

    public String getSummary() {
        return summary;
    }

    public List<AiRequirementAssessmentResponse> getRequirementAssessments() {
        return requirementAssessments;
    }

    public List<String> getInconsistencies() {
        return inconsistencies;
    }

    public List<String> getNextSteps() {
        return nextSteps;
    }

    public List<String> getQuestionsForUser() {
        return questionsForUser;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
