package com.amtpilot.application.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import com.amtpilot.application.exception.DocumentStorageException;
import com.amtpilot.application.exception.InvalidDocumentException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentStorageServiceTest {

    @TempDir
    Path temporaryDirectory;

    private DocumentStorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new DocumentStorageService(
                temporaryDirectory.toString());
    }

    @Test
    void storesLoadsAndDeletesPdf() throws IOException {
        byte[] content = "%PDF-1.7\nExample PDF"
                .getBytes(StandardCharsets.UTF_8);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "passport.pdf",
                "application/pdf",
                content);

        String storagePath = storageService.store(file);

        assertThat(storagePath)
                .endsWith(".pdf")
                .isNotEqualTo("passport.pdf");

        assertThat(temporaryDirectory.resolve(storagePath))
                .exists();

        Resource storedDocument = storageService.load(storagePath);

        try (InputStream inputStream = storedDocument.getInputStream()) {

            assertThat(inputStream.readAllBytes())
                    .isEqualTo(content);
        }

        storageService.delete(storagePath);

        assertThat(temporaryDirectory.resolve(storagePath))
                .doesNotExist();
    }

    @Test
    void rejectsEmptyDocument() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "passport.pdf",
                "application/pdf",
                new byte[0]);

        assertThatThrownBy(() -> storageService.store(file))
                .isInstanceOf(InvalidDocumentException.class)
                .hasMessage("Document must not be empty");
    }

    @Test
    void rejectsNonPdfDocument() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.txt",
                "text/plain",
                "Example".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> storageService.store(file))
                .isInstanceOf(InvalidDocumentException.class)
                .hasMessage("Only PDF documents are allowed");
    }

    @Test
    void rejectsInvalidPdfContent() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "passport.pdf",
                "application/pdf",
                "Not a PDF".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> storageService.store(file))
                .isInstanceOf(InvalidDocumentException.class)
                .hasMessage(
                        "Document content is not a valid PDF");
    }

    @Test
    void rejectsPathOutsideUploadDirectory() {
        assertThatThrownBy(
                () -> storageService.load("../secret.pdf"))
                .isInstanceOf(DocumentStorageException.class)
                .hasMessage("Invalid document storage path");
    }
}