package com.example.leadershipcompass_capstoneprojectbackend.controller;

import com.example.leadershipcompass_capstoneprojectbackend.dto.ModuleDto;
import com.example.leadershipcompass_capstoneprojectbackend.service.ModulesService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin REST API for managing learning modules.
 */
@CrossOrigin(origins = {
        "http://localhost:3000",
        "http://localhost:5173",
        "http://127.0.0.1:5500",
        "http://localhost:5500"
})
@RestController
@RequestMapping("/api/admin/modules")
@RequiredArgsConstructor
public class AdminModulesController {

    private final ModulesService modulesService;

    /**
     * Lists all learning modules with optional sorting.
     *
     * @param sortBy category, id, or book
     * @param direction asc or desc
     * @return module list
     */
    @GetMapping
    public List<ModuleDto> listModules(
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        return modulesService.findAll(sortBy, direction);
    }

    /**
     * Retrieves one learning module by id.
     *
     * @param id module primary key
     * @return module details
     */
    @GetMapping("/{id}")
    public ModuleDto getModule(@PathVariable Long id) {
        return modulesService.findById(id);
    }

    /**
     * Creates a new learning module.
     *
     * @param dto module payload
     * @return created module
     */
    @PostMapping
    public ResponseEntity<ModuleDto> createModule(@Valid @RequestBody ModuleDto dto) {
        ModuleDto created = modulesService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Updates an existing learning module.
     *
     * @param id module primary key
     * @param dto updated payload
     * @return updated module
     */
    @PutMapping("/{id}")
    public ModuleDto updateModule(@PathVariable Long id, @Valid @RequestBody ModuleDto dto) {
        return modulesService.update(id, dto);
    }

    /**
     * Deletes a learning module.
     *
     * @param id module primary key
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModule(@PathVariable Long id) {
        modulesService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
