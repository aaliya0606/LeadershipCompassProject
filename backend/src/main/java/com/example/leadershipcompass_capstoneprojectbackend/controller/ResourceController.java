package com.example.leadershipcompass_capstoneprojectbackend.controller;

import com.example.leadershipcompass_capstoneprojectbackend.model.Resource;
import com.example.leadershipcompass_capstoneprojectbackend.service.ResourceImportService;
import com.example.leadershipcompass_capstoneprojectbackend.service.ResourceService;
import com.example.leadershipcompass_capstoneprojectbackend.service.ResourceStorageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * REST controller for managing Resource Library content.
 *
 * <p>Provides endpoints for retrieving, viewing, uploading,
 * updating, deleting, and importing resources.</p>
 */

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;
    private final ResourceStorageService resourceStorageService;
    private final ResourceImportService resourceImportService;

    /**
     * Returns all active Resource Library resources.
     *
     * @return active resources ordered for display
     */
    @GetMapping
    public ResponseEntity<List<Resource>> getResources() {
        return ResponseEntity.ok(resourceService.getActiveResources());
    }

    /**
     * Returns a resource by its ID.
     *
     * @param id resource identifier
     * @return matching resource
     */

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getResourceById(@PathVariable Long id) {
        return ResponseEntity.ok(resourceService.getResourceById(id));
    }
    
    /**
     * Returns the physical file associated with a resource for inline viewing.
     *
     * @param id resource identifier
     * @return stored resource file
     */
    @GetMapping("/{id}/file")
    public ResponseEntity<org.springframework.core.io.Resource> viewResourceFile(
        @PathVariable Long id
    ) {
        Resource resource = resourceService.getResourceById(id);

        org.springframework.core.io.Resource file =
                resourceStorageService.loadFile(resource.getStorageKey());

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + resource.getOriginalFileName() + "\""
                )
                .contentType(MediaType.parseMediaType(resource.getContentType()))
                .body(file);
    }

    /**
     * Creates a resource metadata record without uploading a file.
     *
     * @param resource resource metadata
     * @return saved resource
     */
    @PostMapping
    public ResponseEntity<Resource> createResource(@RequestBody Resource resource) {
        return ResponseEntity.ok(resourceService.createResource(resource));
    }

    /**
     * Uploads a resource file and stores its metadata.
     *
     * @return newly created resource
     */
    @PostMapping(
        value = "/upload",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Resource> uploadResource(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("leadershipLanguage") String leadershipLanguage,
            @RequestParam("resourceType") String resourceType,
            @RequestParam(value = "displayOrder", defaultValue = "0") Integer displayOrder,
            @RequestParam(value = "active", defaultValue = "true") Boolean active
    ) {

        Resource savedResource = resourceStorageService.storeResource(
                file,
                title,
                description,
                leadershipLanguage,
                resourceType,
                displayOrder,
                active
        );

        return ResponseEntity.ok(savedResource);
    }

    /**
     * Updates metadata for an existing resource.
     *
     * @param id resource identifier
     * @param resource updated metadata
     * @return updated resource
     */
    @PutMapping("/{id}")
    public ResponseEntity<Resource> updateResource(
            @PathVariable Long id,
            @RequestBody Resource resource
    ) {
        return ResponseEntity.ok(resourceService.updateResource(id, resource));
    }

    /**
     * Removes a resource record from the Resource Library.
     *
     * @param id resource identifier
     * @return empty response when deletion succeeds
     */

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
        resourceService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }

/**
     * Imports files that already exist in local resource storage.
     *
     * <p>This is an administrative migration utility. Normal uploads
     * should use the {@code /upload} endpoint.</p>
     *
     * @return confirmation message
     */
    //ADMIN ONLY MIGRATION

    @PostMapping("/import-local")
    public ResponseEntity<String> importLocalResources() {
        resourceImportService.importLocalResources();
        return ResponseEntity.ok("Local resources imported successfully.");
    }
}
