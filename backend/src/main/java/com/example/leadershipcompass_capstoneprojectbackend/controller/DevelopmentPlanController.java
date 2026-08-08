package com.example.leadershipcompass_capstoneprojectbackend.controller;

import com.example.leadershipcompass_capstoneprojectbackend.dto.DevelopmentPlanDto;
import com.example.leadershipcompass_capstoneprojectbackend.dto.DevelopmentPlanPreviewRequest;
import com.example.leadershipcompass_capstoneprojectbackend.dto.DevelopmentPlanSummaryDto;
import com.example.leadershipcompass_capstoneprojectbackend.service.DevelopmentPlanService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for 5-week development plans.
 * <p>
 * Each successful generate call persists a new plan snapshot. {@code /current}
 * always returns the newest; {@code /} lists history; {@code /{id}} loads one
 * historical plan owned by the caller.
 */
@CrossOrigin(origins = {
        "http://localhost:3000",
        "http://localhost:5173",
        "http://127.0.0.1:5500",
        "http://localhost:5500"
})
@RestController
@RequestMapping("/api/development-plans")
@RequiredArgsConstructor
public class DevelopmentPlanController {

    private final DevelopmentPlanService developmentPlanService;

    /**
     * Returns the authenticated user's most recent saved development plan.
     *
     * @param principal current authenticated user
     * @return saved plan DTO
     */
    @GetMapping("/current")
    public DevelopmentPlanDto getCurrentPlan(Principal principal) {
        return developmentPlanService.getCurrentPlan(principal.getName());
    }

    /**
     * Lists all saved plan snapshots for the user, newest first.
     *
     * @param principal current authenticated user
     * @return plan history summaries
     */
    @GetMapping
    public List<DevelopmentPlanSummaryDto> listPlans(Principal principal) {
        return developmentPlanService.listPlans(principal.getName());
    }

    /**
     * Returns one saved plan owned by the authenticated user.
     *
     * @param principal current authenticated user
     * @param planId    development plan id
     * @return full plan including weeks
     */
    @GetMapping("/{planId}")
    public DevelopmentPlanDto getPlanById(Principal principal, @PathVariable Long planId) {
        return developmentPlanService.getPlanById(principal.getName(), planId);
    }

    /**
     * Generates and persists a new plan from the user's latest survey scores.
     * Older plans remain stored as history.
     *
     * @param principal current authenticated user
     * @return newly created plan
     */
    @PostMapping("/generate")
    public ResponseEntity<DevelopmentPlanDto> generatePlan(Principal principal) {
        DevelopmentPlanDto plan = developmentPlanService.generatePlan(principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(plan);
    }

    /**
     * Returns a non-persisted plan preview using mock survey scores (POC).
     *
     * @param request mock score payload
     * @return preview plan DTO
     */
    @PostMapping("/preview")
    public DevelopmentPlanDto previewPlan(@Valid @RequestBody DevelopmentPlanPreviewRequest request) {
        return developmentPlanService.previewPlan(request);
    }
}
