package com.example.leadershipcompass_capstoneprojectbackend.service;

import com.example.leadershipcompass_capstoneprojectbackend.dto.ModuleDto;
import com.example.leadershipcompass_capstoneprojectbackend.model.Modules;
import com.example.leadershipcompass_capstoneprojectbackend.repository.ModulesRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Business logic for admin learning module management.
 */
@Service
@RequiredArgsConstructor
public class ModulesService {

    private final ModulesRepository modulesRepository;

    /**
     * Returns all modules sorted by the requested field.
     *
     * @param sortBy field name: category, id, or book
     * @param direction asc or desc
     * @return sorted module DTO list
     */
    @Transactional(readOnly = true)
    public List<ModuleDto> findAll(String sortBy, String direction) {
        Sort sort = Sort.by(resolveDirection(direction), resolveSortProperty(sortBy));
        return modulesRepository.findAll(sort).stream().map(this::toDto).toList();
    }

    /**
     * Returns a single module by id.
     *
     * @param id module primary key
     * @return module DTO
     */
    @Transactional(readOnly = true)
    public ModuleDto findById(Long id) {
        return toDto(getModuleOrThrow(id));
    }

    /**
     * Creates a new learning module.
     *
     * @param dto module payload
     * @return persisted module DTO
     */
    @Transactional
    public ModuleDto create(ModuleDto dto) {
        if (modulesRepository.findByBookAndTitle(dto.getBook(), dto.getTitle()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A module with this book and title already exists.");
        }

        Modules module = new Modules();
        applyDto(module, dto);
        return toDto(modulesRepository.save(module));
    }

    /**
     * Updates an existing learning module.
     *
     * @param id module primary key
     * @param dto updated payload
     * @return updated module DTO
     */
    @Transactional
    public ModuleDto update(Long id, ModuleDto dto) {
        Modules module = getModuleOrThrow(id);

        modulesRepository
                .findByBookAndTitle(dto.getBook(), dto.getTitle())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "A module with this book and title already exists.");
                });

        applyDto(module, dto);
        return toDto(modulesRepository.save(module));
    }

    /**
     * Deletes a learning module by id.
     *
     * @param id module primary key
     */
    @Transactional
    public void delete(Long id) {
        Modules module = getModuleOrThrow(id);
        modulesRepository.delete(module);
    }

    private Modules getModuleOrThrow(Long id) {
        return modulesRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found."));
    }

    private void applyDto(Modules module, ModuleDto dto) {
        module.setCategory(dto.getCategory().trim());
        module.setBook(dto.getBook().trim());
        module.setTitle(dto.getTitle().trim());
        module.setDescription(dto.getDescription() == null ? "" : dto.getDescription().trim());
        module.setDisplayOrder(dto.getDisplayOrder());
        module.setActive(dto.getActive());
        module.setSourceChapters(copyList(dto.getSourceChapters()));
        module.setChecklist(copyList(dto.getChecklist()));
        module.setQuotesAndConcepts(copyList(dto.getQuotesAndConcepts()));
        module.setActivities(copyList(dto.getActivities()));
    }

    private List<String> copyList(List<String> values) {
        if (values == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(values);
    }

    private ModuleDto toDto(Modules module) {
        ModuleDto dto = new ModuleDto();
        dto.setId(module.getId());
        dto.setCategory(module.getCategory());
        dto.setBook(module.getBook());
        dto.setTitle(module.getTitle());
        dto.setDescription(module.getDescription());
        dto.setDisplayOrder(module.getDisplayOrder());
        dto.setActive(module.getActive());
        dto.setSourceChapters(copyList(module.getSourceChapters()));
        dto.setChecklist(copyList(module.getChecklist()));
        dto.setQuotesAndConcepts(copyList(module.getQuotesAndConcepts()));
        dto.setActivities(copyList(module.getActivities()));
        return dto;
    }

    private String resolveSortProperty(String sortBy) {
        if (sortBy == null) {
            return "id";
        }
        return switch (sortBy.toLowerCase(Locale.ROOT)) {
            case "category" -> "category";
            case "book" -> "book";
            case "id", "moduleid", "module_id" -> "id";
            default -> "id";
        };
    }

    private Sort.Direction resolveDirection(String direction) {
        if (direction != null && direction.equalsIgnoreCase("desc")) {
            return Sort.Direction.DESC;
        }
        return Sort.Direction.ASC;
    }
}
