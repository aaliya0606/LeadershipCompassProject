package com.example.leadershipcompass_capstoneprojectbackend.service;

import com.example.leadershipcompass_capstoneprojectbackend.dto.DevelopmentPlanDto;
import com.example.leadershipcompass_capstoneprojectbackend.dto.DevelopmentPlanPreviewRequest;
import com.example.leadershipcompass_capstoneprojectbackend.dto.DevelopmentPlanSummaryDto;
import com.example.leadershipcompass_capstoneprojectbackend.dto.DevelopmentPlanWeekDto;
import com.example.leadershipcompass_capstoneprojectbackend.model.DevelopmentPlan;
import com.example.leadershipcompass_capstoneprojectbackend.model.DevelopmentPlanWeek;
import com.example.leadershipcompass_capstoneprojectbackend.model.Modules;
import com.example.leadershipcompass_capstoneprojectbackend.model.SurveyResult;
import com.example.leadershipcompass_capstoneprojectbackend.model.User;
import com.example.leadershipcompass_capstoneprojectbackend.repository.DevelopmentPlanRepository;
import com.example.leadershipcompass_capstoneprojectbackend.repository.ModulesRepository;
import com.example.leadershipcompass_capstoneprojectbackend.repository.SurveyResultRepository;
import com.example.leadershipcompass_capstoneprojectbackend.repository.UserRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Generates and stores personalised 5-week development plans.
 * <p>
 * Plan selection uses survey scores and active learning modules. AI-Brain calls
 * are delegated to {@link AiBrainService}; if AI is unavailable or unusable,
 * a score-weighted fallback planner is used.
 */
@Service
public class DevelopmentPlanService {

    private static final Logger log = LoggerFactory.getLogger(DevelopmentPlanService.class);
    private static final int MAX_WEEKS = 5;
    private static final String AI_SOURCE = "AI_BRAIN";
    private static final String FALLBACK_SOURCE = "RULE_BASED_FALLBACK";

    private final DevelopmentPlanRepository developmentPlanRepository;
    private final SurveyResultRepository surveyResultRepository;
    private final ModulesRepository modulesRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final AiBrainService aiBrainService;

    /**
     * @param developmentPlanRepository persistence for generated plans
     * @param surveyResultRepository    source of the user's latest survey scores
     * @param modulesRepository         source of active learning modules
     * @param userRepository            authenticated user lookup
     * @param objectMapper              JSON prompt/response parsing
     * @param aiBrainService            reusable AI-Brain HTTP client
     */
    public DevelopmentPlanService(
            DevelopmentPlanRepository developmentPlanRepository,
            SurveyResultRepository surveyResultRepository,
            ModulesRepository modulesRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper,
            AiBrainService aiBrainService) {
        this.developmentPlanRepository = developmentPlanRepository;
        this.surveyResultRepository = surveyResultRepository;
        this.modulesRepository = modulesRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.aiBrainService = aiBrainService;
    }

    /**
     * Returns the most recently generated plan for the authenticated user.
     *
     * @param userEmail authenticated user email
     * @return saved development plan
     */
    @Transactional(readOnly = true)
    public DevelopmentPlanDto getCurrentPlan(String userEmail) {
        User user = getUserByEmail(userEmail);
        DevelopmentPlan plan = developmentPlanRepository.findFirstByUserIdOrderByGeneratedAtDesc(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No development plan found."));
        return toDto(plan);
    }

    /**
     * Returns all saved plans for the user, newest first (history list).
     *
     * @param userEmail authenticated user email
     * @return plan summaries without week details
     */
    @Transactional(readOnly = true)
    public List<DevelopmentPlanSummaryDto> listPlans(String userEmail) {
        User user = getUserByEmail(userEmail);
        return developmentPlanRepository.findByUserIdOrderByGeneratedAtDesc(user.getId()).stream()
                .map(this::toSummaryDto)
                .toList();
    }

    /**
     * Returns one saved plan owned by the authenticated user.
     *
     * @param userEmail authenticated user email
     * @param planId    development plan id
     * @return full plan including weeks
     */
    @Transactional(readOnly = true)
    public DevelopmentPlanDto getPlanById(String userEmail, Long planId) {
        User user = getUserByEmail(userEmail);
        DevelopmentPlan plan = developmentPlanRepository.findByIdAndUserId(planId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Development plan not found."));
        return toDto(plan);
    }

    /**
     * Generates and persists a new 5-week plan snapshot from the user's latest survey scores.
     * <p>
     * Older plans are kept; this always inserts a new row so history is retained
     * and {@link #getCurrentPlan(String)} returns the newest snapshot.
     *
     * @param userEmail authenticated user email
     * @return newly saved development plan
     */
    @Transactional
    public DevelopmentPlanDto generatePlan(String userEmail) {
        User user = getUserByEmail(userEmail);
        SurveyResult surveyResult = surveyResultRepository.findFirstByUserIdOrderByIdDesc(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No survey result found for user."));
        List<Modules> activeModules = modulesRepository.findByActiveTrueOrderByDisplayOrderAscIdAsc();
        if (activeModules.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No active modules are available.");
        }

        ScoreSnapshot scoreSnapshot = ScoreSnapshot.from(surveyResult);
        List<PlannedWeek> plannedWeeks = generateWeeks(user.getFullName(), "development-plan-" + user.getId(), scoreSnapshot, activeModules);

        DevelopmentPlan plan = new DevelopmentPlan();
        plan.setUser(user);
        plan.setGeneratedAt(Instant.now());
        plan.setGenerationSource(plannedWeeks.stream().anyMatch(PlannedWeek::generatedByAi) ? AI_SOURCE : FALLBACK_SOURCE);
        plan.setCaringTimeScore(scoreSnapshot.caringTimeScore());
        plan.setReceivingValueScore(scoreSnapshot.receivingValueScore());
        plan.setActsOfSupportScore(scoreSnapshot.actsOfSupportScore());
        plan.setWordsOfRecognitionScore(scoreSnapshot.wordsOfRecognitionScore());
        plan.setPsychologicalTouchScore(scoreSnapshot.psychologicalTouchScore());
        plan.replaceWeeks(toEntities(plannedWeeks));

        return toDto(developmentPlanRepository.save(plan));
    }

    /**
     * Builds a non-persisted plan preview from mock scores for POC testing.
     *
     * @param request mock score payload
     * @return preview development plan
     */
    @Transactional(readOnly = true)
    public DevelopmentPlanDto previewPlan(DevelopmentPlanPreviewRequest request) {
        List<Modules> activeModules = modulesRepository.findByActiveTrueOrderByDisplayOrderAscIdAsc();
        if (activeModules.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No active modules are available.");
        }

        ScoreSnapshot scoreSnapshot = ScoreSnapshot.fromRequest(request);
        String fullName = request.getFullName() != null && !request.getFullName().isBlank()
                ? request.getFullName().trim()
                : "POC User";
        List<PlannedWeek> plannedWeeks = generateWeeks(
                fullName,
                buildPreviewConversationId(scoreSnapshot),
                scoreSnapshot,
                activeModules);

        DevelopmentPlanDto dto = new DevelopmentPlanDto();
        dto.setGeneratedAt(Instant.now());
        dto.setGenerationSource(plannedWeeks.stream().anyMatch(PlannedWeek::generatedByAi) ? AI_SOURCE : FALLBACK_SOURCE);
        dto.setCaringTimeScore(scoreSnapshot.caringTimeScore());
        dto.setReceivingValueScore(scoreSnapshot.receivingValueScore());
        dto.setActsOfSupportScore(scoreSnapshot.actsOfSupportScore());
        dto.setWordsOfRecognitionScore(scoreSnapshot.wordsOfRecognitionScore());
        dto.setPsychologicalTouchScore(scoreSnapshot.psychologicalTouchScore());
        dto.setWeeks(toWeekDtos(plannedWeeks));
        return dto;
    }

    private User getUserByEmail(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found."));
    }

    /**
     * Attempts AI generation first, then falls back to the score-weighted rule planner.
     */
    private List<PlannedWeek> generateWeeks(
            String fullName,
            String conversationId,
            ScoreSnapshot scoreSnapshot,
            List<Modules> activeModules) {
        return tryGenerateWithAi(fullName, conversationId, scoreSnapshot, activeModules)
                .orElseGet(() -> buildFallbackPlan(scoreSnapshot, activeModules));
    }

    /**
     * Calls {@link AiBrainService}, parses the JSON plan answer, and reshapes weeks
     * into score-weighted category slots.
     *
     * @return planned weeks when AI succeeds; empty when AI is disabled or unusable
     */
    private Optional<List<PlannedWeek>> tryGenerateWithAi(
            String fullName,
            String conversationId,
            ScoreSnapshot scoreSnapshot,
            List<Modules> activeModules) {
        if (!aiBrainService.isEnabled()) {
            return Optional.empty();
        }

        String aiAnswer = "";
        try {
            AiPromptPayload payload = new AiPromptPayload(
                    fullName,
                    scoreSnapshot,
                    weakestCategories(scoreSnapshot),
                    buildWeightedCategorySlots(scoreSnapshot),
                    toAiModuleSummaries(activeModules));
            String query = buildAiPrompt(payload);

            Optional<String> answer = aiBrainService.chat(query, conversationId);
            if (answer.isEmpty()) {
                return Optional.empty();
            }

            aiAnswer = answer.get();
            AiGeneratedPlan generatedPlan = parseAiPlan(aiAnswer);
            List<PlannedWeek> plannedWeeks = reconcileAiPlan(generatedPlan, scoreSnapshot, activeModules);
            return plannedWeeks.isEmpty() ? Optional.empty() : Optional.of(plannedWeeks);
        } catch (JsonProcessingException ex) {
            log.warn("AI-brain development-plan response could not be parsed. Answer snippet: {}", truncate(aiAnswer, 300), ex);
            return Optional.empty();
        }
    }

    /**
     * Builds the JSON-only planning prompt sent to the AI-Brain.
     */
    private String buildAiPrompt(AiPromptPayload payload) throws JsonProcessingException {
        List<String> weakestCategories = payload.weakestCategories();
        List<String> weekTargets = payload.weekCategoryTargets();
        return """
                Return ONLY valid JSON. Do not include markdown fences or commentary.
                Schema: {"weeks":[{"weekNumber":1,"moduleId":2,"focus":"personalized focus sentence","rationale":"personalized rationale sentence"}]}
                Requirements:
                - Exactly 5 weeks with weekNumber 1 through 5
                - Use only module IDs from availableModules
                - One unique module per week
                - Match each week to the target category for that week (weaker scores get more weeks): %s
                - Prioritize modules in these weaker categories: %s
                - Keep focus and rationale practical and personalized

                Planning data:
                %s
                """.formatted(
                String.join(" > ", weekTargets),
                String.join(", ", weakestCategories),
                objectMapper.writeValueAsString(payload));
    }

    /**
     * Parses an AI answer into a structured plan, stripping markdown fences if present.
     */
    private AiGeneratedPlan parseAiPlan(String answer) throws JsonProcessingException {
        String json = extractJsonBlob(answer);
        return objectMapper.readValue(json, AiGeneratedPlan.class);
    }

    private String extractJsonBlob(String answer) {
        String cleaned = answer == null ? "" : answer.trim();
        cleaned = cleaned.replaceAll("(?s)```json\\s*", "");
        cleaned = cleaned.replaceAll("(?s)```\\s*", "").trim();

        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return cleaned.substring(start, end + 1);
        }
        return cleaned;
    }

    /**
     * Maps AI week suggestions onto real modules, then applies score-weighted slotting.
     */
    private List<PlannedWeek> reconcileAiPlan(
            AiGeneratedPlan generatedPlan,
            ScoreSnapshot scoreSnapshot,
            List<Modules> activeModules) {
        if (generatedPlan == null || generatedPlan.weeks() == null || generatedPlan.weeks().isEmpty()) {
            return reshapeToWeightedSlots(List.of(), scoreSnapshot, activeModules);
        }

        Map<Long, Modules> moduleById = new LinkedHashMap<>();
        for (Modules module : activeModules) {
            moduleById.put(module.getId(), module);
        }

        Set<Long> usedModules = new LinkedHashSet<>();
        List<PlannedWeek> plannedWeeks = new ArrayList<>();
        for (AiGeneratedWeek aiWeek : generatedPlan.weeks()) {
            if (aiWeek == null || aiWeek.moduleId() == null || usedModules.contains(aiWeek.moduleId())) {
                continue;
            }

            Modules module = moduleById.get(aiWeek.moduleId());
            if (module == null) {
                continue;
            }

            usedModules.add(module.getId());
            plannedWeeks.add(new PlannedWeek(
                    plannedWeeks.size() + 1,
                    module,
                    resolveFocus(aiWeek.focus(), module),
                    resolveRationale(aiWeek.rationale(), module, scoreSnapshot),
                    trimActions(aiWeek.actions(), module),
                    true));
            if (plannedWeeks.size() == MAX_WEEKS) {
                break;
            }
        }

        return reshapeToWeightedSlots(plannedWeeks, scoreSnapshot, activeModules);
    }

    private List<String> weakestCategories(ScoreSnapshot scoreSnapshot) {
        List<CategoryScore> rankedCategories = scoreSnapshot.toRankedCategories();
        int lowestScore = rankedCategories.get(0).score();
        List<String> categories = new ArrayList<>();
        for (CategoryScore categoryScore : rankedCategories) {
            if (categoryScore.score() != lowestScore) {
                break;
            }
            categories.add(categoryScore.category());
        }
        return categories;
    }

    private String buildPreviewConversationId(ScoreSnapshot scoreSnapshot) {
        return "development-plan-preview-%d-%d-%d-%d-%d".formatted(
                scoreSnapshot.caringTimeScore(),
                scoreSnapshot.receivingValueScore(),
                scoreSnapshot.actsOfSupportScore(),
                scoreSnapshot.wordsOfRecognitionScore(),
                scoreSnapshot.psychologicalTouchScore());
    }

    /**
     * Builds a plan using only the score-weighted fallback (no AI call).
     */
    private List<PlannedWeek> buildFallbackPlan(ScoreSnapshot scoreSnapshot, List<Modules> activeModules) {
        return reshapeToWeightedSlots(List.of(), scoreSnapshot, activeModules);
    }

    /**
     * Builds a 5-week plan where weaker categories receive more weeks based on score gaps.
     * Reuses AI focus/rationale/actions when a matching module is available.
     */
    private List<PlannedWeek> reshapeToWeightedSlots(
            List<PlannedWeek> candidateWeeks,
            ScoreSnapshot scoreSnapshot,
            List<Modules> activeModules) {
        List<String> categorySlots = buildWeightedCategorySlots(scoreSnapshot);
        Map<String, List<Modules>> modulesByCategory = indexModulesByCategory(activeModules);
        Set<Long> usedModules = new LinkedHashSet<>();
        List<PlannedWeek> reshapedWeeks = new ArrayList<>();

        for (String targetCategory : categorySlots) {
            PlannedWeek aiMatch = takeMatchingAiWeek(candidateWeeks, targetCategory, usedModules);
            Modules module;
            String focus;
            String rationale;
            List<String> actions;
            boolean generatedByAi;

            if (aiMatch != null) {
                module = aiMatch.module();
                focus = aiMatch.focus();
                rationale = aiMatch.rationale();
                actions = aiMatch.actions();
                generatedByAi = aiMatch.generatedByAi();
            } else {
                module = pickModuleForCategory(targetCategory, modulesByCategory, activeModules, usedModules);
                if (module == null) {
                    break;
                }
                focus = defaultFocus(module);
                rationale = buildDefaultRationale(module, scoreSnapshot);
                actions = buildDefaultActions(module);
                generatedByAi = false;
            }

            usedModules.add(module.getId());
            reshapedWeeks.add(new PlannedWeek(
                    reshapedWeeks.size() + 1,
                    module,
                    focus,
                    rationale,
                    actions,
                    generatedByAi));
        }

        return reshapedWeeks;
    }

    private PlannedWeek takeMatchingAiWeek(
            List<PlannedWeek> candidateWeeks,
            String targetCategory,
            Set<Long> usedModules) {
        String normalizedTarget = normalizeCategory(targetCategory);
        for (PlannedWeek candidate : candidateWeeks) {
            if (usedModules.contains(candidate.module().getId())) {
                continue;
            }
            if (normalizeCategory(candidate.module().getCategory()).equals(normalizedTarget)) {
                return candidate;
            }
        }
        return null;
    }

    private Map<String, List<Modules>> indexModulesByCategory(List<Modules> activeModules) {
        Map<String, List<Modules>> modulesByCategory = new LinkedHashMap<>();
        for (Modules module : activeModules) {
            modulesByCategory.computeIfAbsent(normalizeCategory(module.getCategory()), ignored -> new ArrayList<>()).add(module);
        }
        return modulesByCategory;
    }

    private Modules pickModuleForCategory(
            String targetCategory,
            Map<String, List<Modules>> modulesByCategory,
            List<Modules> activeModules,
            Set<Long> usedModules) {
        Modules selected = modulesByCategory.getOrDefault(normalizeCategory(targetCategory), List.of()).stream()
                .filter(module -> !usedModules.contains(module.getId()))
                .findFirst()
                .orElse(null);
        if (selected != null) {
            return selected;
        }
        return activeModules.stream()
                .filter(module -> !usedModules.contains(module.getId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Allocates week slots across categories, giving more weeks to lower scores.
     * When all scores are equal, each category receives one week.
     *
     * @return ordered category names, one entry per week slot
     */
    private List<String> buildWeightedCategorySlots(ScoreSnapshot scoreSnapshot) {
        List<CategoryScore> rankedCategories = scoreSnapshot.toRankedCategories();
        int lowestScore = rankedCategories.get(0).score();
        int highestScore = rankedCategories.get(rankedCategories.size() - 1).score();

        if (lowestScore == highestScore) {
            List<String> balancedSlots = new ArrayList<>();
            for (CategoryScore categoryScore : rankedCategories) {
                balancedSlots.add(categoryScore.category());
            }
            return balancedSlots;
        }

        List<Integer> weights = new ArrayList<>();
        for (CategoryScore categoryScore : rankedCategories) {
            weights.add(51 - categoryScore.score());
        }

        int[] slotCounts = allocateSlotCounts(weights, MAX_WEEKS);
        List<String> slots = new ArrayList<>();
        for (int index = 0; index < rankedCategories.size(); index++) {
            for (int count = 0; count < slotCounts[index]; count++) {
                slots.add(rankedCategories.get(index).category());
            }
        }
        return slots;
    }

    /**
     * Distributes {@code totalSlots} across categories using largest-remainder weighting.
     */
    private int[] allocateSlotCounts(List<Integer> weights, int totalSlots) {
        int weightSum = weights.stream().mapToInt(Integer::intValue).sum();
        int[] slotCounts = new int[weights.size()];
        int assignedSlots = 0;
        List<Double> remainders = new ArrayList<>();

        for (int index = 0; index < weights.size(); index++) {
            double rawSlots = (weights.get(index) * (double) totalSlots) / weightSum;
            int wholeSlots = (int) Math.floor(rawSlots);
            slotCounts[index] = wholeSlots;
            assignedSlots += wholeSlots;
            remainders.add(rawSlots - wholeSlots);
        }

        while (assignedSlots < totalSlots) {
            int bestIndex = 0;
            for (int index = 1; index < remainders.size(); index++) {
                if (remainders.get(index) > remainders.get(bestIndex)) {
                    bestIndex = index;
                }
            }
            slotCounts[bestIndex]++;
            remainders.set(bestIndex, 0.0);
            assignedSlots++;
        }

        return slotCounts;
    }

    private List<DevelopmentPlanWeek> toEntities(List<PlannedWeek> plannedWeeks) {
        List<DevelopmentPlanWeek> weeks = new ArrayList<>();
        for (PlannedWeek plannedWeek : plannedWeeks) {
            DevelopmentPlanWeek week = new DevelopmentPlanWeek();
            week.setWeekNumber(plannedWeek.weekNumber());
            week.setModuleId(plannedWeek.module().getId());
            week.setCategory(plannedWeek.module().getCategory());
            week.setModuleTitle(plannedWeek.module().getTitle());
            week.setFocus(plannedWeek.focus());
            week.setRationale(plannedWeek.rationale());
            week.setActions(new ArrayList<>(plannedWeek.actions()));
            weeks.add(week);
        }
        return weeks;
    }

    private DevelopmentPlanDto toDto(DevelopmentPlan plan) {
        DevelopmentPlanDto dto = new DevelopmentPlanDto();
        dto.setId(plan.getId());
        dto.setGeneratedAt(plan.getGeneratedAt());
        dto.setGenerationSource(plan.getGenerationSource());
        dto.setCaringTimeScore(plan.getCaringTimeScore());
        dto.setReceivingValueScore(plan.getReceivingValueScore());
        dto.setActsOfSupportScore(plan.getActsOfSupportScore());
        dto.setWordsOfRecognitionScore(plan.getWordsOfRecognitionScore());
        dto.setPsychologicalTouchScore(plan.getPsychologicalTouchScore());
        dto.setWeeks(toWeekDtosFromEntities(plan.getWeeks()));
        return dto;
    }

    private DevelopmentPlanSummaryDto toSummaryDto(DevelopmentPlan plan) {
        DevelopmentPlanSummaryDto dto = new DevelopmentPlanSummaryDto();
        dto.setId(plan.getId());
        dto.setGeneratedAt(plan.getGeneratedAt());
        dto.setGenerationSource(plan.getGenerationSource());
        dto.setCaringTimeScore(plan.getCaringTimeScore());
        dto.setReceivingValueScore(plan.getReceivingValueScore());
        dto.setActsOfSupportScore(plan.getActsOfSupportScore());
        dto.setWordsOfRecognitionScore(plan.getWordsOfRecognitionScore());
        dto.setPsychologicalTouchScore(plan.getPsychologicalTouchScore());
        return dto;
    }

    private List<DevelopmentPlanWeekDto> toWeekDtos(List<PlannedWeek> plannedWeeks) {
        List<DevelopmentPlanWeekDto> weeks = new ArrayList<>();
        for (PlannedWeek plannedWeek : plannedWeeks) {
            DevelopmentPlanWeekDto weekDto = new DevelopmentPlanWeekDto();
            weekDto.setWeekNumber(plannedWeek.weekNumber());
            weekDto.setModuleId(plannedWeek.module().getId());
            weekDto.setCategory(plannedWeek.module().getCategory());
            weekDto.setModuleTitle(plannedWeek.module().getTitle());
            weekDto.setFocus(plannedWeek.focus());
            weekDto.setRationale(plannedWeek.rationale());
            weekDto.setActions(new ArrayList<>(plannedWeek.actions()));
            weeks.add(weekDto);
        }
        return weeks;
    }

    private List<DevelopmentPlanWeekDto> toWeekDtosFromEntities(List<DevelopmentPlanWeek> planWeeks) {
        List<DevelopmentPlanWeekDto> weeks = new ArrayList<>();
        for (DevelopmentPlanWeek week : planWeeks) {
            DevelopmentPlanWeekDto weekDto = new DevelopmentPlanWeekDto();
            weekDto.setWeekNumber(week.getWeekNumber());
            weekDto.setModuleId(week.getModuleId());
            weekDto.setCategory(week.getCategory());
            weekDto.setModuleTitle(week.getModuleTitle());
            weekDto.setFocus(week.getFocus());
            weekDto.setRationale(week.getRationale());
            weekDto.setActions(new ArrayList<>(week.getActions()));
            weeks.add(weekDto);
        }
        return weeks;
    }

    private List<AiModuleSummary> toAiModuleSummaries(List<Modules> activeModules) {
        return activeModules.stream()
                .map(module -> new AiModuleSummary(module.getId(), module.getCategory(), module.getTitle()))
                .toList();
    }

    /**
     * Builds a default rationale that reflects whether the category is weak, strong, or balanced.
     */
    private String buildDefaultRationale(Modules module, ScoreSnapshot scoreSnapshot) {
        String category = module.getCategory();
        int score = scoreSnapshot.scoreForCategory(category);
        List<CategoryScore> rankedCategories = scoreSnapshot.toRankedCategories();
        int lowestScore = rankedCategories.get(0).score();
        int highestScore = rankedCategories.get(rankedCategories.size() - 1).score();

        if (lowestScore == highestScore) {
            return "This module strengthens %s (%d/50) as part of a balanced development plan with practical behaviours you can apply immediately."
                    .formatted(category, score);
        }
        if (score == lowestScore) {
            return "This module targets %s, one of your weaker leadership areas (%d/50), with practical behaviours you can apply immediately."
                    .formatted(category, score);
        }
        if (score == highestScore) {
            return "This module builds on %s, a relative strength (%d/50), to deepen practical leadership habits you can apply immediately."
                    .formatted(category, score);
        }
        return "This module develops %s (%d/50), an area with room to grow, through practical behaviours you can apply immediately."
                .formatted(category, score);
    }

    private String resolveFocus(String aiFocus, Modules module) {
        return isMeaningfulAiText(aiFocus) ? aiFocus.trim() : defaultFocus(module);
    }

    private String resolveRationale(String aiRationale, Modules module, ScoreSnapshot scoreSnapshot) {
        if (!isMeaningfulAiText(aiRationale) || isMisleadingScoreRationale(aiRationale, module, scoreSnapshot)) {
            return buildDefaultRationale(module, scoreSnapshot);
        }
        return aiRationale.trim();
    }

    private boolean isMeaningfulAiText(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return !normalized.equals("short focus")
                && !normalized.equals("short rationale")
                && !normalized.equals("personalized focus sentence")
                && !normalized.equals("personalized rationale sentence")
                && !normalized.equals("n/a");
    }

    /**
     * Rejects AI/fallback copy that calls a category "lower/weaker" when it is not
     * among the user's weakest scores (or when all scores are tied).
     */
    private boolean isMisleadingScoreRationale(String rationale, Modules module, ScoreSnapshot scoreSnapshot) {
        String normalized = rationale.toLowerCase(Locale.ROOT);
        boolean claimsWeak = normalized.contains("lower scoring")
                || normalized.contains("weaker leadership")
                || normalized.contains("weakest");
        if (!claimsWeak) {
            return false;
        }

        List<CategoryScore> ranked = scoreSnapshot.toRankedCategories();
        int lowest = ranked.get(0).score();
        int highest = ranked.get(ranked.size() - 1).score();
        int score = scoreSnapshot.scoreForCategory(module.getCategory());
        return lowest == highest || score > lowest;
    }

    private String defaultFocus(Modules module) {
        if (module.getDescription() != null && !module.getDescription().isBlank()) {
            return module.getDescription().length() > 140
                    ? module.getDescription().substring(0, 140).trim() + "..."
                    : module.getDescription().trim();
        }
        return "Build practical capability in " + module.getCategory().toLowerCase(Locale.ROOT) + ".";
    }

    private List<String> buildDefaultActions(Modules module) {
        List<String> actions = new ArrayList<>();
        addTopItems(actions, module.getChecklist());
        addTopItems(actions, module.getActivities());
        if (actions.isEmpty()) {
            actions.add("Read the module and identify one behaviour to practice this week.");
            actions.add("Apply the idea in one real workplace conversation.");
        }
        return actions.stream().limit(4).toList();
    }

    private List<String> trimActions(List<String> aiActions, Modules module) {
        if (aiActions == null || aiActions.stream().noneMatch(action -> action != null && !action.isBlank())) {
            return buildDefaultActions(module);
        }
        return aiActions.stream()
                .filter(action -> action != null && !action.isBlank())
                .map(String::trim)
                .limit(4)
                .toList();
    }

    private void addTopItems(List<String> target, List<String> values) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                target.add(value.trim());
            }
            if (target.size() >= 4) {
                return;
            }
        }
    }

    private String normalizeCategory(String category) {
        return category == null ? "" : category.trim().toLowerCase(Locale.ROOT);
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength) + "...";
    }

    /**
     * Immutable score snapshot used while building a plan.
     */
    private record ScoreSnapshot(
            int caringTimeScore,
            int receivingValueScore,
            int actsOfSupportScore,
            int wordsOfRecognitionScore,
            int psychologicalTouchScore) {

        /** Builds a snapshot from a persisted survey result. */
        static ScoreSnapshot from(SurveyResult surveyResult) {
            return new ScoreSnapshot(
                    defaultScore(surveyResult.getCaringTimeScore()),
                    defaultScore(surveyResult.getReceivingValueScore()),
                    defaultScore(surveyResult.getActsOfSupportScore()),
                    defaultScore(surveyResult.getWordsOfRecognitionScore()),
                    defaultScore(surveyResult.getPsychologicalTouchScore()));
        }

        /** Builds a snapshot from a POC preview request. */
        static ScoreSnapshot fromRequest(DevelopmentPlanPreviewRequest request) {
            return new ScoreSnapshot(
                    defaultScore(request.getCaringTimeScore()),
                    defaultScore(request.getReceivingValueScore()),
                    defaultScore(request.getActsOfSupportScore()),
                    defaultScore(request.getWordsOfRecognitionScore()),
                    defaultScore(request.getPsychologicalTouchScore()));
        }

        /** Returns categories sorted ascending by score (weakest first). */
        List<CategoryScore> toRankedCategories() {
            List<CategoryScore> categories = new ArrayList<>();
            categories.add(new CategoryScore("Caring Time", caringTimeScore));
            categories.add(new CategoryScore("Receiving Value", receivingValueScore));
            categories.add(new CategoryScore("Acts of Support", actsOfSupportScore));
            categories.add(new CategoryScore("Words of Recognition", wordsOfRecognitionScore));
            categories.add(new CategoryScore("Psychological Touch", psychologicalTouchScore));
            categories.sort(Comparator.comparingInt(CategoryScore::score));
            return categories;
        }

        /** Looks up the score for a leadership language category name. */
        int scoreForCategory(String category) {
            return switch (category == null ? "" : category.trim().toLowerCase(Locale.ROOT)) {
                case "caring time" -> caringTimeScore;
                case "receiving value" -> receivingValueScore;
                case "acts of support" -> actsOfSupportScore;
                case "words of recognition" -> wordsOfRecognitionScore;
                case "psychological touch" -> psychologicalTouchScore;
                default -> 0;
            };
        }

        private static int defaultScore(Integer value) {
            return value == null ? 0 : value;
        }
    }

    /** Category name paired with its score for ranking. */
    private record CategoryScore(String category, int score) {
    }

    /** Compact module metadata included in the AI prompt. */
    private record AiModuleSummary(Long id, String category, String title) {
    }

    /** Structured prompt payload serialised into the AI-Brain query. */
    private record AiPromptPayload(
            String fullName,
            ScoreSnapshot scores,
            List<String> weakestCategories,
            List<String> weekCategoryTargets,
            List<AiModuleSummary> availableModules) {
    }

    /** Parsed AI JSON root containing weekly recommendations. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AiGeneratedPlan(List<AiGeneratedWeek> weeks) {
    }

    /** One week object as returned (or partially returned) by the AI-Brain. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AiGeneratedWeek(
            Integer weekNumber,
            Long moduleId,
            String category,
            String moduleTitle,
            String focus,
            String rationale,
            List<String> actions) {
    }

    /** Internal week representation before persistence or DTO mapping. */
    private record PlannedWeek(
            Integer weekNumber,
            Modules module,
            String focus,
            String rationale,
            List<String> actions,
            boolean generatedByAi) {
    }
}
