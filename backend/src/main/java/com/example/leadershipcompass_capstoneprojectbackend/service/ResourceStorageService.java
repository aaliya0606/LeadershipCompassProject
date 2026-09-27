package com.example.leadershipcompass_capstoneprojectbackend.service;

import com.example.leadershipcompass_capstoneprojectbackend.model.Resource;
import com.example.leadershipcompass_capstoneprojectbackend.repository.ResourceRepository;
import com.example.leadershipcompass_capstoneprojectbackend.service.storage.ResourceStorageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Coordinates Resource Library file storage and metadata persistence.
 *
 * <p>The actual physical storage mechanism is delegated to a
 * {@link ResourceStorageProvider}. This allows the Resource Library
 * to use local storage, Supabase Storage, or a future provider such
 * as SharePoint without changing the frontend API.</p>
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ResourceStorageService {

    private final ResourceRepository resourceRepository;
    private final List<ResourceStorageProvider> storageProviders;

    @Value("${app.resource-storage.provider:LOCAL}")
    private String configuredProvider;

    /**
     * Stores an uploaded Resource Library file using the configured
     * storage provider and persists its metadata.
     *
     * @param file uploaded physical file
     * @param title title displayed in the Resource Library
     * @param description optional resource description
     * @param leadershipLanguage Leadership Compass category
     * @param resourceType resource classification
     * @param displayOrder preferred display order
     * @param active whether the resource is active
     * @return persisted Resource metadata
     */
    public Resource storeResource(
            MultipartFile file,
            String title,
            String description,
            String leadershipLanguage,
            String resourceType,
            Integer displayOrder,
            Boolean active
    ) {
        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank()) {
            throw new RuntimeException("Uploaded file has no filename");
        }

        String folderName = determineFolder(resourceType);

        String safeFileName = sanitizeFileName(originalFileName);

        String storageKey = folderName + "/" + safeFileName;

        ResourceStorageProvider provider =
                getProvider(configuredProvider);

        provider.store(file, storageKey);

        Resource resource = Resource.builder()
                .title(title)
                .description(description)
                .leadershipLanguage(leadershipLanguage)
                .resourceType(resourceType)
                .resourceUrl(null)
                .active(active)
                .displayOrder(displayOrder)
                .originalFileName(originalFileName)
                .contentType(
                        file.getContentType() != null
                                ? file.getContentType()
                                : "application/octet-stream"
                )
                .fileSize(file.getSize())
                .storageProvider(provider.getProviderName())
                .storageKey(storageKey)
                .build();

        try {
            return resourceRepository.save(resource);
        } catch (RuntimeException e) {
            // Avoid leaving an orphaned uploaded file if database persistence fails.
            try {
                provider.delete(storageKey);
            } catch (Exception cleanupException) {
                e.addSuppressed(cleanupException);
            }

            throw e;
        }
    }

    /**
     * Loads the physical file belonging to a Resource.
     *
     * <p>The provider recorded on the Resource determines where the
     * file is retrieved from.</p>
     *
     * @param resource resource metadata containing provider and storage key
     * @return stored file as a Spring Resource
     */
    public org.springframework.core.io.Resource loadFile(Resource resource) {
        validateStoredResource(resource);

        return getProvider(resource.getStorageProvider())
                .load(resource.getStorageKey());
    }

    /**
     * Deletes the physical file belonging to a Resource.
     *
     * @param resource resource metadata containing provider and storage key
     */
    public void deleteFile(Resource resource) {
        validateStoredResource(resource);

        getProvider(resource.getStorageProvider())
                .delete(resource.getStorageKey());
    }

    /**
     * Finds the configured implementation for a provider name.
     *
     * @param providerName provider identifier such as LOCAL or SUPABASE
     * @return matching storage provider
     */
    private ResourceStorageProvider getProvider(String providerName) {
        if (providerName == null || providerName.isBlank()) {
            throw new IllegalArgumentException(
                    "Resource storage provider is missing"
            );
        }

        return storageProviders.stream()
                .filter(provider ->
                        provider.getProviderName()
                                .equalsIgnoreCase(providerName))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unsupported resource storage provider: "
                                        + providerName
                        )
                );
    }

    /**
     * Determines the logical storage folder for a resource type.
     */
    private String determineFolder(String resourceType) {
        if (resourceType == null) {
            return "other";
        }

        return switch (resourceType.toUpperCase()) {
            case "PDF" -> "books";
            case "VIDEO" -> "videos";
            case "AUDIO" -> "audios";
            case "EBOOK" -> "ebooks";
            case "DOCUMENT" -> "documents";
            case "IMAGE" -> "images";
            default -> "other";
        };
    }

    /**
     * Prevents an uploaded filename from introducing storage path segments.
     */
    private String sanitizeFileName(String originalFileName) {
        String normalized = originalFileName.replace("\\", "/");

        String fileName = normalized.substring(
                normalized.lastIndexOf('/') + 1
        );

        if (fileName.isBlank()
                || ".".equals(fileName)
                || "..".equals(fileName)) {
            throw new IllegalArgumentException(
                    "Uploaded file has an invalid filename"
            );
        }

        return fileName;
    }

    /**
     * Validates metadata required to locate a stored physical file.
     */
    private void validateStoredResource(Resource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("Resource is required");
        }

        if (resource.getStorageKey() == null
                || resource.getStorageKey().isBlank()) {
            throw new IllegalArgumentException(
                    "Resource storage key is missing"
            );
        }

        if (resource.getStorageProvider() == null
                || resource.getStorageProvider().isBlank()) {
            throw new IllegalArgumentException(
                    "Resource storage provider is missing"
            );
        }
    }
}