package com.amtpilot.application.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amtpilot.application.exception.DocumentStorageException;
import com.amtpilot.application.exception.InvalidDocumentException;

@Service
public class DocumentStorageService {

    private static final byte[] PDF_SIGNATURE = { '%', 'P', 'D', 'F', '-' };

    private final Path uploadDirectory;

    public DocumentStorageService(
            @Value("${app.storage.upload-directory}") String uploadDirectory) {

        this.uploadDirectory = Path.of(uploadDirectory)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(this.uploadDirectory);
        } catch (IOException exception) {
            throw new DocumentStorageException(
                    "Could not create the upload directory",
                    exception);
        }
    }

    public String store(MultipartFile file) {
        validate(file);

        String storageFilename = UUID.randomUUID() + ".pdf";

        Path targetPath = resolve(storageFilename);

        try (InputStream inputStream = file.getInputStream()) {

            Files.copy(inputStream, targetPath);
            return storageFilename;

        } catch (IOException exception) {
            throw new DocumentStorageException(
                    "Could not store the document",
                    exception);
        }
    }

    public Resource load(String storagePath) {
        Path filePath = resolve(storagePath);

        if (!Files.isRegularFile(filePath)) {
            throw new DocumentStorageException(
                    "Stored document could not be found",
                    new NoSuchFileException(storagePath));
        }

        return new FileSystemResource(filePath);
    }

    public void delete(String storagePath) {
        Path filePath = resolve(storagePath);

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException exception) {
            throw new DocumentStorageException(
                    "Could not delete the stored document",
                    exception);
        }
    }

    private void validate(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidDocumentException(
                    "Document must not be empty");
        }

        if (!MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(
                file.getContentType())) {

            throw new InvalidDocumentException(
                    "Only PDF documents are allowed");
        }

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null
                || !originalFilename
                        .toLowerCase(Locale.ROOT)
                        .endsWith(".pdf")) {

            throw new InvalidDocumentException(
                    "Document filename must end with .pdf");
        }

        validatePdfSignature(file);
    }

    private void validatePdfSignature(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {

            byte[] signature = inputStream.readNBytes(
                    PDF_SIGNATURE.length);

            if (signature.length != PDF_SIGNATURE.length) {
                throw new InvalidDocumentException(
                        "Document content is not a valid PDF");
            }

            for (int index = 0; index < PDF_SIGNATURE.length; index++) {

                if (signature[index] != PDF_SIGNATURE[index]) {
                    throw new InvalidDocumentException(
                            "Document content is not a valid PDF");
                }
            }

        } catch (IOException exception) {
            throw new DocumentStorageException(
                    "Could not inspect the document",
                    exception);
        }
    }

    private Path resolve(String storagePath) {
        Path resolvedPath = uploadDirectory
                .resolve(storagePath)
                .normalize();

        if (!resolvedPath.startsWith(uploadDirectory)) {
            throw new DocumentStorageException(
                    "Invalid document storage path",
                    new IllegalArgumentException(storagePath));
        }

        return resolvedPath;
    }
}