package com.example.leadershipcompass_capstoneprojectbackend.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Stores Resource Library files on the local filesystem.
 *
 * <p>This provider is retained for backwards compatibility and
 * local development. Hosted deployments should use a persistent
 * external provider such as Supabase or SharePoint.</p>
 */

@Component
public class LocalStorageProvider implements ResourceStorageProvider {

    private final Path storageLocation = resolveStorageLocation();

    @Override
    public String getProviderName() {
        return "LOCAL";
    }

    @Override
    public void store(MultipartFile file, String storageKey) {
        try {
            Path targetFile = getFilePath(storageKey);

            if (Files.exists(targetFile)) {
                throw new RuntimeException(
                        "A resource file with this name already exists: "
                                + file.getOriginalFilename()
                );
            }

            if (targetFile.getParent() != null) {
                Files.createDirectories(targetFile.getParent());
            }

            Files.copy(file.getInputStream(), targetFile);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not store uploaded resource locally",
                    e
            );
        }
    }

    @Override
    public Resource load(String storageKey) {
        try {
            Path filePath = getFilePath(storageKey);

            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                throw new RuntimeException(
                        "Resource file not found: " + storageKey
                );
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException(
                        "Resource file is not readable: " + storageKey
                );
            }

            return resource;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not load local resource file: " + storageKey,
                    e
            );
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            Files.deleteIfExists(getFilePath(storageKey));
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not delete local resource file: " + storageKey,
                    e
            );
        }
    }

    private Path getFilePath(String storageKey) {
        Path filePath = storageLocation.resolve(storageKey).normalize();

        if (!filePath.startsWith(storageLocation)) {
            throw new IllegalArgumentException("Invalid file path");
        }

        return filePath;
    }

    private static Path resolveStorageLocation() {
        Path workingDirectory =
                Paths.get("").toAbsolutePath().normalize();

        if (Files.exists(workingDirectory.resolve("pom.xml"))) {
            Path storagePath =
                    workingDirectory.resolve("resource-storage").normalize();
            createStorageDirectory(storagePath);
            return storagePath;
        }

        Path backendDirectory = workingDirectory.resolve("backend");

        if (Files.exists(backendDirectory.resolve("pom.xml"))) {
            Path storagePath =
                    backendDirectory.resolve("resource-storage").normalize();
            createStorageDirectory(storagePath);
            return storagePath;
        }

        Path storagePath =
                workingDirectory.resolve("resource-storage").normalize();
        createStorageDirectory(storagePath);
        return storagePath;
    }

    private static void createStorageDirectory(Path storagePath) {
        try {
            Files.createDirectories(storagePath);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not create Resource Library storage directory: "
                            + storagePath,
                    e
            );
        }
    }
}