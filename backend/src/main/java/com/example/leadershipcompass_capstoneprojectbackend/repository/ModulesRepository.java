package com.example.leadershipcompass_capstoneprojectbackend.repository;

import com.example.leadershipcompass_capstoneprojectbackend.model.Modules;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Data access repository for {@link Modules} records.
 *
 * <p>Provides standard CRUD operations plus lookup methods used by the
 * module seed loader and API/service layers.</p>
 */
public interface ModulesRepository extends JpaRepository<Modules, Long> {
    /**
     * Finds a module by its source book and module title.
     *
     * <p>This pair is used as the natural identity for idempotent JSON seeding.</p>
     *
     * @param book source book title
     * @param title module title
     * @return matching module when present
     */
    Optional<Modules> findByBookAndTitle(String book, String title);

    List<Modules> findByCategoryAndActiveTrueOrderByDisplayOrderAsc(String category);
}
