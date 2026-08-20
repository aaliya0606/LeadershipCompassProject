package com.example.leadershipcompass_capstoneprojectbackend.service;

import com.example.leadershipcompass_capstoneprojectbackend.model.Resource;
import com.example.leadershipcompass_capstoneprojectbackend.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Provides business logic for Resource Library metadata.
 *
 * <p>This service sits between the Resource API controller and
 * {@link ResourceRepository}. It is responsible for retrieving resources,
 * applying default values during creation, updating editable metadata,
 * and removing resource records from the database.</p>
 *
 * <p>Physical file storage is handled separately by
 * {@link ResourceStorageService}. Deleting a resource through this service
 * removes only the database record and intentionally leaves the stored file
 * untouched.</p>
 */

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;

    /**
     * Returns all resource records, including inactive resources.
     *
     * @return all resources stored in the database
     */
    public List<Resource> getAllResources() {
        return resourceRepository.findAll();
    }

    /**
     * Returns all active resources ordered by their display order in ascending order.
     *
     * @return active resources sorted from lowest to highest display order
     */
    public List<Resource> getActiveResources() {
        return resourceRepository.findByActiveTrueOrderByDisplayOrderAsc();
    }

    /**
     * Returns the resource with the specified ID, or throws an exception if not found.
     *
     * @param id the ID of the resource to retrieve
     * @return the resource with the specified ID
     * @throws RuntimeException if the resource is not found
     */
    public Resource getResourceById(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource not found"));
    }

    /**
     * Creates a new resource record in the database.
     *
     * @param resource the resource to create
     * @return the created resource
     */
    public Resource createResource(Resource resource) {
        if (resource.getActive() == null) {
            resource.setActive(true);
        }

        return resourceRepository.save(resource);
    }

    /**
     * Updates editable metadata for an existing resource.
     *
     * <p>Storage-specific fields such as storage key, original filename,
     * content type, file size, and storage provider are intentionally preserved.
     * Replacing the physical file is not part of this operation.</p>
     *
     * @param id identifier of the resource to update
     * @param updatedResource new metadata values
     * @return the updated resource
     * @throws RuntimeException if the resource does not exist
     */

    public Resource updateResource(Long id, Resource updatedResource) {
        Resource existingResource = getResourceById(id);

        existingResource.setTitle(updatedResource.getTitle());
        existingResource.setDescription(updatedResource.getDescription());
        existingResource.setLeadershipLanguage(updatedResource.getLeadershipLanguage());
        existingResource.setResourceType(updatedResource.getResourceType());
        existingResource.setResourceUrl(updatedResource.getResourceUrl());
        existingResource.setActive(updatedResource.getActive());
        existingResource.setDisplayOrder(updatedResource.getDisplayOrder());

        return resourceRepository.save(existingResource);
    }

    /**
     * Removes a resource record from the application library.
     *
     * <p>The physical file is intentionally preserved in storage to reduce the
     * risk of permanently deleting confidential source material by accident.</p>
     *
     * @param id identifier of the resource record to remove
     * @throws RuntimeException if the resource does not exist
     */

    public void deleteResource(Long id) {
        if (!resourceRepository.existsById(id)) {
            throw new RuntimeException("Resource not found");
        }
        resourceRepository.deleteById(id);
    }
}
