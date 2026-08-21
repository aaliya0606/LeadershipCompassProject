package com.example.leadershipcompass_capstoneprojectbackend.repository;

import com.example.leadershipcompass_capstoneprojectbackend.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for accessing and querying {@link Resource} records.
 *
 * <p>Spring Data JPA generates the implementations for the query methods
 * declared here based on their method names.</p>
 *
 * <p>The Resource Library uses this repository to retrieve active resources,
 * check whether a stored file has already been imported, and filter resources
 * by leadership language, resource type, or title.</p>
 */

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    /**
     * Checks whether a resource already exists for the given storage key.
     *
     * <p>This is used by the local import process to avoid creating duplicate
     * database records for files that have already been imported.</p>
     *
     * @param storageKey path of the file relative to the Resource Library storage root
     * @return true if a matching resource record already exists
     */

    boolean existsByStorageKey(String storageKey);

    /**
     * Returns all active resources ordered by their display order in ascending order.
     *
     * <p>This is the default query used by the user-facing Resource Library.</p>
     *
     * @return active resources sorted from lowest to highest display order
     */

    List<Resource> findByActiveTrueOrderByDisplayOrderAsc();

    /**
     * Finds resources matching a leadership language, ignoring case.
     */

    List<Resource> findByLeadershipLanguageIgnoreCase(String leadershipLanguage);

    /**
     * Finds resources matching a resource type, ignoring case.
     */

    List<Resource> findByResourceTypeIgnoreCase(String resourceType);

    /**
     * Finds resources whose titles contain the supplied text, ignoring case.
     */

    List<Resource> findByTitleContainingIgnoreCase(String title);
    
}