package com.example.leadershipcompass_capstoneprojectbackend.service;

import com.example.leadershipcompass_capstoneprojectbackend.model.Resource;
import com.example.leadershipcompass_capstoneprojectbackend.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Imports existing files from the local Resource Library storage directory
 * into the application database.
 *
 * <p>This service is intended primarily as a migration or administrative
 * utility for files that already exist inside {@code resource-storage}.
 * It is not the normal upload path for new resources. New file uploads
 * should use {@link ResourceStorageService} through the Resource API.</p>
 *
 * <p>The importer recursively scans the local storage directory and creates
 * a {@link Resource} metadata record for each physical file that does not
 * already have a matching storage key in the database.</p>
 *
 * <p>Imported resources are initially assigned the leadership language
 * {@code UNCLASSIFIED} so that an administrator can classify them later.</p>
 */

@Service
@RequiredArgsConstructor
public class ResourceImportService {

    private final ResourceRepository resourceRepository;

    /**
     * Root directory scanned for existing Resource Library files.
     */
    private final Path storageLocation =
            Paths.get("resource-storage").toAbsolutePath().normalize();

    
    /**
     * Scans the local Resource Library storage directory and imports files
     * that are not already represented in the database.
     *
     * <p>The directory walk is recursive, so files inside nested folders are
     * also discovered. Existing resources are identified by their relative
     * storage key and are skipped to prevent duplicate database records.</p>
     *
     * @throws RuntimeException if the storage directory cannot be scanned
     */

    public void importLocalResources() {
        try (var paths = Files.walk(storageLocation)) {

            paths
                    .filter(Files::isRegularFile)
                    .forEach(this::importFileIfMissing);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not scan resource-storage folder",
                    e
            );
        }
    }

    /**
     * Creates a Resource metadata record for a physical file when no resource
     * with the same storage key already exists.
     *
     * <p>Metadata such as filename, MIME type, file size, resource type, and
     * relative storage key is derived from the physical file. Newly imported
     * resources are active by default and initially classified as
     * {@code UNCLASSIFIED}.</p>
     *
     * @param filePath absolute path of the physical file being considered
     * @throws RuntimeException if file metadata cannot be read
     */

    private void importFileIfMissing(Path filePath) {
        try {
            String storageKey = storageLocation
                    .relativize(filePath)
                    .toString()
                    .replace("\\", "/");

            if (resourceRepository.existsByStorageKey(storageKey)) {
                return;
            }

            String originalFileName = filePath.getFileName().toString();
            String contentType = Files.probeContentType(filePath);

            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            Resource resource = Resource.builder()
                    .title(createTitle(originalFileName))
                    .description(null)
                    .leadershipLanguage("UNCLASSIFIED")
                    .resourceType(determineResourceType(contentType, originalFileName))
                    .resourceUrl(null)
                    .active(true)
                    .displayOrder(null)
                    .originalFileName(originalFileName)
                    .contentType(contentType)
                    .fileSize(Files.size(filePath))
                    .storageProvider("LOCAL")
                    .storageKey(storageKey)
                    .build();

            resourceRepository.save(resource);

            System.out.println("Imported resource: " + storageKey);

        } catch (IOException e) {
            throw new RuntimeException("Could not import file: " + filePath, e);
        }
    }

    /**
     * Determines a resource type from the file's MIME type and extension.
     *
     * @param contentType MIME type of the file
     * @param fileName original filename
     * @return detected resource type, or {@code OTHER} if unknown
     */

    private String determineResourceType(String contentType, String fileName) {

        String lowerFileName = fileName.toLowerCase();

        if (contentType.equals("application/pdf") || lowerFileName.endsWith(".pdf")) {
            return "PDF";
        }

        if (contentType.startsWith("video/")) {
            return "VIDEO";
        }

        if (contentType.startsWith("audio/")) {
            return "AUDIO";
        }

        if (lowerFileName.endsWith(".epub")) {
            return "EBOOK";
        }

        if (lowerFileName.endsWith(".doc") || lowerFileName.endsWith(".docx")) {
            return "DOCUMENT";
        }

        if (contentType.startsWith("image/")) {
            return "IMAGE";
        }

        return "OTHER";
    }

    /**
     * Creates a readable resource title from a filename.
     *
     * @param fileName original filename
     * @return filename converted into a resource title
     */
    private String createTitle(String fileName) {

        int dotIndex = fileName.lastIndexOf(".");

        String title = dotIndex > 0
                ? fileName.substring(0, dotIndex)
                : fileName;

        title = title
                .replace("-", " ")
                .replace("_", " ")
                .trim();

        return title;
    }
}