package com.amtpilot.entity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import com.amtpilot.ai.dto.AiDocumentAnalysisResponse;
import com.amtpilot.ai.dto.AiExtractedFieldResponse;

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
@Table(name = "application_document_analysis")
public class ApplicationDocumentAnalysis {

    public ApplicationDocumentAnalysis() {
    }

    public ApplicationDocumentAnalysis(
            ApplicationDocument document,
            AiDocumentAnalysisResponse analysis) {

        this.document = document;
        update(analysis);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false, unique = true)
    private ApplicationDocument document;

    @Column(name = "document_type", nullable = false, length = 255)
    private String documentType;

    @Column(name = "primary_language", nullable = false, length = 50)
    private String primaryLanguage;

    @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
    private String summary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extracted_fields", nullable = false, columnDefinition = "jsonb")
    private List<AiExtractedFieldResponse> extractedFields;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "missing_or_unclear", nullable = false, columnDefinition = "jsonb")
    private List<String> missingOrUnclear;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "warnings", nullable = false, columnDefinition = "jsonb")
    private List<String> warnings;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void update(
            AiDocumentAnalysisResponse analysis) {

        this.documentType = analysis.documentType();
        this.primaryLanguage = analysis.primaryLanguage();
        this.summary = analysis.summary();
        this.extractedFields = List.copyOf(
                analysis.extractedFields());
        this.missingOrUnclear = List.copyOf(
                analysis.missingOrUnclear());
        this.warnings = List.copyOf(
                analysis.warnings());
    }

    public UUID getId() {
        return id;
    }

    public ApplicationDocument getDocument() {
        return document;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getPrimaryLanguage() {
        return primaryLanguage;
    }

    public String getSummary() {
        return summary;
    }

    public List<AiExtractedFieldResponse> getExtractedFields() {
        return extractedFields;
    }

    public List<String> getMissingOrUnclear() {
        return missingOrUnclear;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}