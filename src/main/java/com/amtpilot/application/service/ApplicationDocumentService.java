package com.amtpilot.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.amtpilot.application.dto.DocumentResponse;
import com.amtpilot.application.dto.UpdateChecklistItemRequest;
import com.amtpilot.application.exception.ApplicationNotFoundException;
import com.amtpilot.application.exception.ChecklistItemNotFoundException;
import com.amtpilot.application.exception.DocumentNotFoundException;
import com.amtpilot.entity.Application;
import com.amtpilot.entity.ApplicationChecklistItem;
import com.amtpilot.entity.ApplicationDocument;
import com.amtpilot.repository.ApplicationChecklistItemRepository;
import com.amtpilot.repository.ApplicationDocumentRepository;
import com.amtpilot.repository.ApplicationRepository;

@Service
public class ApplicationDocumentService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationChecklistItemRepository checklistRepository;
    private final ApplicationDocumentRepository documentRepository;
    private final DocumentStorageService storageService;
    private final ApplicationService applicationService;

    public ApplicationDocumentService(
            ApplicationRepository applicationRepository,
            ApplicationChecklistItemRepository checklistRepository,
            ApplicationDocumentRepository documentRepository,
            DocumentStorageService storageService,
            ApplicationService applicationService) {

        this.applicationRepository = applicationRepository;
        this.checklistRepository = checklistRepository;
        this.documentRepository = documentRepository;
        this.storageService = storageService;
        this.applicationService = applicationService;
    }

    @Transactional
    public DocumentResponse upload(
            UUID userId,
            UUID applicationId,
            UUID checklistItemId,
            MultipartFile file) {

        Application application = applicationRepository
                .findByIdAndUserId(applicationId, userId)
                .orElseThrow(
                        () -> new ApplicationNotFoundException(
                                applicationId));

        ApplicationChecklistItem checklistItem = findChecklistItem(
                userId,
                applicationId,
                checklistItemId);

        String storagePath = storageService.store(file);

        try {
            String originalFilename = StringUtils.getFilename(
                    StringUtils.cleanPath(
                            file.getOriginalFilename()));

            ApplicationDocument document = new ApplicationDocument(
                    application,
                    checklistItem,
                    originalFilename,
                    file.getContentType(),
                    file.getSize(),
                    storagePath);

            ApplicationDocument savedDocument = documentRepository.saveAndFlush(document);

            if (checklistItemId != null) {
                applicationService.updateChecklistItem(
                        userId,
                        checklistItemId,
                        new UpdateChecklistItemRequest(true));
            }

            return toResponse(savedDocument);

        } catch (RuntimeException exception) {
            try {
                storageService.delete(storagePath);
            } catch (RuntimeException cleanupException) {
                exception.addSuppressed(cleanupException);
            }

            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> list(
            UUID userId,
            UUID applicationId) {

        applicationRepository
                .findByIdAndUserId(applicationId, userId)
                .orElseThrow(
                        () -> new ApplicationNotFoundException(
                                applicationId));

        return documentRepository
                .findByApplicationIdOrderByCreatedAtDesc(
                        applicationId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void delete(
            UUID userId,
            UUID applicationId,
            UUID documentId) {

        applicationRepository
                .findByIdAndUserId(applicationId, userId)
                .orElseThrow(
                        () -> new ApplicationNotFoundException(
                                applicationId));

        ApplicationDocument document = documentRepository
                .findByIdAndApplicationUserId(documentId, userId)
                .filter(item -> item.getApplication()
                        .getId()
                        .equals(applicationId))
                .orElseThrow(
                        () -> new DocumentNotFoundException(
                                documentId));

        ApplicationChecklistItem checklistItem = document.getChecklistItem();

        documentRepository.delete(document);
        documentRepository.flush();
        storageService.delete(document.getStoragePath());

        if (checklistItem != null
                && !documentRepository.existsByChecklistItemId(
                        checklistItem.getId())) {

            applicationService.updateChecklistItem(
                    userId,
                    checklistItem.getId(),
                    new UpdateChecklistItemRequest(false));
        }
    }

    private ApplicationChecklistItem findChecklistItem(
            UUID userId,
            UUID applicationId,
            UUID checklistItemId) {

        if (checklistItemId == null) {
            return null;
        }

        return checklistRepository
                .findByIdAndApplicationUserId(
                        checklistItemId,
                        userId)
                .filter(item -> item.getApplication()
                        .getId()
                        .equals(applicationId))
                .orElseThrow(
                        () -> new ChecklistItemNotFoundException(
                                checklistItemId));
    }

    private DocumentResponse toResponse(
            ApplicationDocument document) {

        UUID checklistItemId = document.getChecklistItem() == null
                ? null
                : document.getChecklistItem().getId();

        return new DocumentResponse(
                document.getId(),
                document.getApplication().getId(),
                checklistItemId,
                document.getOriginalFilename(),
                document.getContentType(),
                document.getSizeBytes(),
                document.getCreatedAt());
    }
}
