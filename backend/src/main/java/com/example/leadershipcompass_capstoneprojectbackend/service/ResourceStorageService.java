package com.example.leadershipcompass_capstoneprojectbackend.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.example.leadershipcompass_capstoneprojectbackend.repository.ResourceRepository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;
import com.example.leadershipcompass_capstoneprojectbackend.model.Resource;
import java.io.IOException;

/**
 * Handles local file storage for Resource Library content.
 *
 * <p>Uploaded files are stored beneath the configured
 * resource-storage directory and a corresponding Resource
 * metadata record is persisted to PostgreSQL.</p>
 *
 * <p>Frontend clients should never access storage paths
 * directly. Files should be retrieved through the Resource API.</p>
 */

@Service
@RequiredArgsConstructor
public class ResourceStorageService {

    private final ResourceRepository resourceRepository;

    private final Path storageLocation = resolveStorageLocation();

    /**
     * Resolves the Resource Library storage directory consistently whether
     * the backend is launched from the repository root or the backend folder.
     *
     * @return absolute path to backend/resource-storage
     */
    private static Path resolveStorageLocation() {
        Path workingDirectory =
                Paths.get("").toAbsolutePath().normalize();

        // Backend started from the backend folder
        if (Files.exists(workingDirectory.resolve("pom.xml"))) {
            return workingDirectory
                    .resolve("resource-storage")
                    .normalize();
        }

        // Backend started from the repository root / IDE
        Path backendDirectory = workingDirectory.resolve("backend");

        if (Files.exists(backendDirectory.resolve("pom.xml"))) {
            return backendDirectory
                    .resolve("resource-storage")
                    .normalize();
        }

        throw new IllegalStateException(
                "Could not locate backend Resource Library storage directory"
        );
    }
    //method that safely resolves a filename inside resource-storage

    public Path getFilePath(String fileName) {
        Path filePath = storageLocation.resolve(fileName).normalize();

        if (!filePath.startsWith(storageLocation)) {
            throw new IllegalArgumentException("Invalid file path");
        }

        return filePath;
    }

    //method that checks if a file exists in the storage location
    //safely resolves and confirms the file exists, throwing an exception if it does not
    public Path getExistingFilePath(String fileName) {
    Path filePath = getFilePath(fileName);

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new RuntimeException("Resource file not found: " + fileName);
        }

        return filePath;
    }

    //return the file as Spring Resource so controllers can send it to the browser
    /**
     * Loads a stored Resource Library file as a Spring {@link UrlResource}.
     *
     * <p>The returned resource can be passed to a controller so that the file
     * can be securely served through the Resource API.</p>
     *
     * @param fileName storage key identifying the file to load
     * @return readable Spring resource representing the stored file
     * @throws RuntimeException if the file cannot be found, read, or loaded
     */

    public org.springframework.core.io.Resource loadFile(String fileName) {
        try {
            Path filePath = getExistingFilePath(fileName);
            org.springframework.core.io.Resource resource =
                    new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("Resource file is not readable: " + fileName);
            }

            return resource;
        } catch (Exception e) {
            throw new RuntimeException("Could not load resource file: " + fileName, e);
        }
    }

    /**
     * Stores an uploaded Resource Library file and creates its database metadata.
     *
     * <p>The destination folder is selected from the supplied resource type.
     * For example, PDF resources are stored under {@code books}, videos under
     * {@code videos}, and audio resources under {@code audios}.</p>
     *
     * <p>The method creates the destination directory when necessary, validates
     * that the final path remains within the Resource Library storage directory,
     * and rejects an upload when a file with the same name already exists.</p>
     *
     * <p>After the physical file is successfully stored, a {@link Resource}
     * record containing its metadata and relative storage key is persisted to
     * the database.</p>
     *
     * @param file uploaded physical file
     * @param title title displayed in the Resource Library
     * @param description optional description of the resource
     * @param leadershipLanguage Leadership Compass category assigned to the resource
     * @param resourceType type used to classify and determine storage of the resource
     * @param displayOrder preferred ordering value in the Resource Library
     * @param active whether the resource should be available to users
     * @return persisted Resource metadata for the uploaded file
     * @throws RuntimeException if the file has no name, has an invalid storage path,
     *                          already exists, or cannot be stored
     */

    public Resource storeResource(
        MultipartFile file,
        String title,
        String description,
        String leadershipLanguage,
        String resourceType,
        Integer displayOrder,
        Boolean active) {

        try {
            String originalFileName = file.getOriginalFilename();

            if (originalFileName == null || originalFileName.isBlank()) {
                throw new RuntimeException("Uploaded file has no filename");
            }

            String folderName = switch (resourceType.toUpperCase()) {
                case "PDF" -> "books";
                case "VIDEO" -> "videos";
                case "AUDIO" -> "audios";
                case "EBOOK" -> "ebooks";
                case "DOCUMENT" -> "documents";
                case "IMAGE" -> "images";
                default -> "other";
            };

            Path targetFolder = storageLocation
                    .resolve(folderName)
                    .normalize();

            Files.createDirectories(targetFolder);

            Path targetFile = targetFolder
                    .resolve(originalFileName)
                    .normalize();

            if (!targetFile.startsWith(storageLocation)) {
                throw new RuntimeException("Invalid storage path");
            }

            if (Files.exists(targetFile)) {
                throw new RuntimeException(
                    "A resource file with this name already exists: " + originalFileName
                );
            }

            Files.copy(
                file.getInputStream(),
                targetFile
            );

            String storageKey = storageLocation
                    .relativize(targetFile)
                    .toString()
                    .replace("\\", "/");

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
                    .storageProvider("LOCAL")
                    .storageKey(storageKey)
                    .build();

            return resourceRepository.save(resource);

        } catch (IOException e) {
            throw new RuntimeException("Could not store uploaded resource", e);
        }
    }

    /**
     * Deletes a locally stored Resource Library file.
     *
     * @param storageKey relative storage path of the file
     * @throws RuntimeException if the file cannot be deleted
     */
    public void deleteFile(String storageKey) {
        try {
            Path filePath = getFilePath(storageKey);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not delete resource file: " + storageKey,
                    e
            );
        }
    }
}

