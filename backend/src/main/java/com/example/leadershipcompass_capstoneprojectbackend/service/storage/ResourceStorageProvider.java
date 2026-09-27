package com.example.leadershipcompass_capstoneprojectbackend.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Common contract for Resource Library file storage providers.
 *
 * <p>Implementations may store files locally, in Supabase Storage,
 * SharePoint, or another external storage service without changing
 * the Resource Library API.</p>
 */
public interface ResourceStorageProvider {

    /**
     * Returns the provider name stored in the Resource database record.
     *
     * @return provider identifier such as LOCAL or SUPABASE
     */
    String getProviderName();

    /**
     * Stores a file using the supplied storage key.
     *
     * @param file uploaded file
     * @param storageKey provider-independent relative storage key
     */
    void store(MultipartFile file, String storageKey);

    /**
     * Loads a file from storage.
     *
     * @param storageKey relative storage key
     * @return file as a Spring Resource
     */
    Resource load(String storageKey);

    /**
     * Deletes a file from storage.
     *
     * @param storageKey relative storage key
     */
    void delete(String storageKey);
}