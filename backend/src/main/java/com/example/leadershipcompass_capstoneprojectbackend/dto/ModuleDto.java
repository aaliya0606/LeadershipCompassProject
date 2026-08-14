package com.example.leadershipcompass_capstoneprojectbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object for learning module API requests and responses.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModuleDto {

    private Long id;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Book is required")
    private String book;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Display order is required")
    private Integer displayOrder;

    @NotNull(message = "Active flag is required")
    private Boolean active;

    private List<String> sourceChapters = new ArrayList<>();
    private List<String> checklist = new ArrayList<>();
    private List<String> quotesAndConcepts = new ArrayList<>();
    private List<String> activities = new ArrayList<>();
}
