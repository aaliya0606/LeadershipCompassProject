package com.example.leadershipcompass_capstoneprojectbackend.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Stores Resource Library files in a private Supabase Storage bucket.
 *
 * <p>The Supabase secret key is used only by the backend and must
 * never be exposed to frontend code or committed to source control.</p>
 */
@Component
public class SupabaseStorageProvider implements ResourceStorageProvider {

    private final RestClient restClient;
    private final String bucket;

    public SupabaseStorageProvider(
            @Value("${app.resource-storage.supabase-url}") String supabaseUrl,
            @Value("${app.resource-storage.supabase-bucket}") String bucket,
            @Value("${app.resource-storage.supabase-secret-key}") String secretKey
    ) {
        this.bucket = bucket;

        this.restClient = RestClient.builder()
                .baseUrl(supabaseUrl + "/storage/v1")
                .defaultHeader("apikey", secretKey)
                .build();
    }

    @Override
    public String getProviderName() {
        return "SUPABASE";
    }

    @Override
    public void store(MultipartFile file, String storageKey) {
        try {
            byte[] fileBytes = file.getBytes();

            MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;

            if (file.getContentType() != null) {
                contentType = MediaType.parseMediaType(file.getContentType());
            }

            restClient.post()
                    .uri("/object/{bucket}/{storageKey}", bucket, storageKey)
                    .contentType(contentType)
                    .header("x-upsert", "false")
                    .body(fileBytes)
                    .retrieve()
                    .toBodilessEntity();

        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not read uploaded resource file",
                    e
            );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not store resource in Supabase: " + storageKey,
                    e
            );
        }
    }

    @Override
    public Resource load(String storageKey) {
        try {
            byte[] fileBytes = restClient.get()
                    .uri(
                            "/object/authenticated/{bucket}/{storageKey}",
                            bucket,
                            storageKey
                    )
                    .retrieve()
                    .body(byte[].class);

            if (fileBytes == null) {
                throw new RuntimeException(
                        "Supabase returned an empty resource: " + storageKey
                );
            }

            return new ByteArrayResource(fileBytes);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not load resource from Supabase: " + storageKey,
                    e
            );
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            restClient.method(org.springframework.http.HttpMethod.DELETE)
                    .uri("/object/{bucket}", bucket)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("prefixes", new String[]{storageKey}))
                    .retrieve()
                    .toBodilessEntity();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not delete resource from Supabase: " + storageKey,
                    e
            );
        }
    }
}