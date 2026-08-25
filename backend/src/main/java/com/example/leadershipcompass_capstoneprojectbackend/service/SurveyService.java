package com.example.leadershipcompass_capstoneprojectbackend.service;

import com.example.leadershipcompass_capstoneprojectbackend.dto.SurveyResultResponse;
import com.example.leadershipcompass_capstoneprojectbackend.dto.SurveySubmissionRequest;
import com.example.leadershipcompass_capstoneprojectbackend.model.SurveyQuestions;
import com.example.leadershipcompass_capstoneprojectbackend.model.SurveyResult;
import com.example.leadershipcompass_capstoneprojectbackend.repository.SurveyQuestionsRepository;
import com.example.leadershipcompass_capstoneprojectbackend.repository.SurveyResultRepository;
import com.example.leadershipcompass_capstoneprojectbackend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.leadershipcompass_capstoneprojectbackend.model.User;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.leadershipcompass_capstoneprojectbackend.dto.ProgressEntryResponse;
import com.example.leadershipcompass_capstoneprojectbackend.dto.PeerComparisonResponse;
import com.example.leadershipcompass_capstoneprojectbackend.model.Resource;

@Service
@RequiredArgsConstructor
//SurveyManager in UML Class Diagram
public class SurveyService{

    //5 leadership categories
    private static final String PREFIX_CT = "CT_";
    private static final String PREFIX_RV = "RV_";
    private static final String PREFIX_AS = "AS_";
    private static final String PREFIX_WR = "WR_";
    private static final String PREFIX_PT = "PT_";
    
    private static final int QUESTIONS_PER_CATEGORY = 10;
    private static final int MIN_SCORE = 1;
    private static final int MAX_SCORE = 5;

    private final SurveyResultRepository surveyResultRepository;
    private final SurveyQuestionsRepository surveyQuestionsRepository;
    private final UserRepository userRepository;
    private final InsightGenerator insightGenerator;

    private final ResourceService resourceService;
    private static final int NEEDS_ATTENTION_THRESHOLD = 30;


    @Transactional
    public SurveyResultResponse submitSurvey(SurveySubmissionRequest request, String email){

        Map<String, Integer> answers = request.getAnswers();
        validateAnswers(answers);

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("User not found:" + email));
        
        int ctScore = sumCategory(answers, PREFIX_CT);
        int rvScore = sumCategory(answers, PREFIX_RV);
        int asScore = sumCategory(answers, PREFIX_AS);
        int wrScore = sumCategory(answers, PREFIX_WR);
        int ptScore = sumCategory(answers, PREFIX_PT);

        SurveyResult result = SurveyResult.builder()
            .user(user)
            .caringTimeScore(ctScore)
            .receivingValueScore(rvScore)
            .actsOfSupportScore(asScore)
            .wordsOfRecognitionScore(wrScore)
            .psychologicalTouchScore(ptScore)  
            .build();

        result.generateResult();
        result.setSummary(insightGenerator.generateInsights(result));

        SurveyResult saved = surveyResultRepository.save(result);

        return buildResponse(saved, ctScore, rvScore, asScore, wrScore, ptScore);

    }

    @Transactional(readOnly = true)
    public List<SurveyResult> getHistoryForUser(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));
        return surveyResultRepository.findByUserOrderByGenerateDateDesc(user);

    }

    @Transactional
    public SurveyQuestions addQuestion(String questionText, String category){
        validateCategory(category);
        return surveyQuestionsRepository.save(new SurveyQuestions(questionText, category, 1));
    }

    @Transactional
    public SurveyQuestions editQuestion(int questionId, String newQuestionText){
        SurveyQuestions question = surveyQuestionsRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Question not found:" + questionId));
        question.setQuestionText(newQuestionText);
        return surveyQuestionsRepository.save(question);
            
    }

    @Transactional
    public void removeQuestion(int questionId){
        if (surveyQuestionsRepository.findById(questionId).isEmpty()){
            throw new EntityNotFoundException("Question not found:" + questionId);
        }
        surveyQuestionsRepository.deleteById(questionId);
    }

    @Transactional(readOnly = true)
    public List<SurveyQuestions> loadQuestions(){
        return surveyQuestionsRepository.findAllByOrderByCategoryAscQuestionIdAsc();

    }

    private int sumCategory(Map<String, Integer> answers, String prefix){
        int total = 0;
        int count = 0;
        for (Map.Entry<String, Integer> entry : answers.entrySet()){
            if (entry.getKey().startsWith(prefix)) {
                total += entry.getValue();
                count++;
            }
        }
        if (count != QUESTIONS_PER_CATEGORY){
            throw new IllegalArgumentException(
                "Expeted " + QUESTIONS_PER_CATEGORY + " questions for prefix"
                + prefix + "', but found " + count + ".");
        }
        return total;
    }

    public String band(int score){
        if (score >= 40) return "High";
        if (score >= 30) return "Strong intent";
        if (score >= 20) return "Needs attention";
        return "Blind spot";
    }

    /// Only have placeholder values for feedback :
    // this can be refactored to: message() + band()
    public String message(String categoryName, int score){
    switch (categoryName) {
        case "Caring Time":
            if (score >= 40) return "You consistently practice high-quality Caring Time, deeply investing in trust and team connection.";
            if (score >= 30) return "You show strong intent but can improve presence and prioritisation of Caring Time.";
            if (score >= 20) return "Your Caring Time habits need attention to avoid disconnection and erosion of psychological safety.";
            return "Caring Time may be a blind spot; boosting it can dramatically improve your leadership impact.";

        case "Receiving Value":
            if (score >= 40) return "You consistently practice Receiving Value and create a culture of psychological safety and trust.";
            if (score >= 30) return "You show good intent but have opportunities to deepen active listening and follow-up habits.";
            if (score >= 20) return "Your practice needs strengthening; risks exist for team disengagement and mistrust.";
            return "Receiving Value may be a leadership blind spot; focus urgently on building authentic listening skills.";

        case "Acts of Support":
            if (score >= 40) return "You consistently demonstrate Acts of Support, enabling your team to thrive and grow.";
            if (score >= 30) return "You show good intent, with opportunities to increase proactive support and clarity.";
            if (score >= 20) return "Your support may be inconsistent, risking team frustration and disengagement.";
            return "Acts of Support is an urgent development area to avoid team burnout and underperformance.";

        case "Words of Recognition":
            if (score >= 40) return "You consistently deliver meaningful Words of Recognition that motivate and build loyalty.";
            if (score >= 30) return "You recognise well but have room to increase specificity, timeliness, and variety.";
            if (score >= 20) return "Your recognition habits may be inconsistent or generic, risking diminished motivation.";
            return "Words of Recognition require urgent development to unlock team engagement and trust.";

        case "Psychological Touch":
            if (score >= 40) return "You consistently embody Psychological Touch, cultivating deep trust, safety, and authentic connection in your team.";
            if (score >= 30) return "Strong awareness with areas for growth in emotional courage, empathy, or tailored communication.";
            if (score >= 20) return "Psychological Touch needs deliberate development to avoid ambient fear, silence, or disengagement.";
            return "Psychological safety may be significantly compromised; urgent focus on emotional connection and safety is required.";

        default:
            return "No feedback available for this category.";
    }
    }

    private void validateAnswers(Map<String, Integer> answers){
        if (answers == null || answers.isEmpty()){
            throw new IllegalArgumentException("Answer map must not be null or empty.");
        }
        for (Map.Entry<String, Integer> entry : answers.entrySet()) {
            int score = entry.getValue();
            if (score < MIN_SCORE || score > MAX_SCORE){
                throw new IllegalArgumentException(
                    "Score for" + entry.getKey() + "is" + score
                    + ". Allowed range: " + MIN_SCORE + "-" + MAX_SCORE + ".");
                
            }
        }

    }

    private void validateCategory(String category){
        if (!List.of("CT", "RV", "AS", "WR", "PT").contains(category)){
            throw new IllegalArgumentException(
                "Unrecognised ctargory'" + category + "'. Must be one of: CT, RV, AS, WR, PT");
            
        }
    }

    private SurveyResultResponse buildResponse(SurveyResult saved, int ct, int rv, int as_, int wr, int pt){

        SurveyResultResponse response = new SurveyResultResponse();
        response.setResultId(saved.getResultId()); 
        response.setOverallScore(saved.getOverallScore());
        response.setOverallBand(saved.getScoreBand());
        response.setSummary(saved.getSummary());

        response.setCaringTimeScore(ct);
        response.setCaringTimeBand(band(ct));
        response.setCaringTimeMessage(message("Caring Time", ct));

        response.setReceivingValueScore(rv);
        response.setReceivingValueBand(band(rv));
        response.setReceivingValueMessage(message("Receiving Value", rv));

        response.setActsOfSupportScore(as_);
        response.setActsOfSupportBand(band(as_));
        response.setActsOfSupportMessage(message("Acts of Support", as_));

        response.setWordsOfRecognitionScore(wr);
        response.setWordsOfRecognitionBand(band(wr));
        response.setWordsOfRecognitionMessage(message("Words of Recognition", wr));

        response.setPsychologicalTouchScore(pt);
        response.setPsychologicalTouchBand(band(pt));
        response.setPsychologicalTouchMessage(message("Psychological Touch", pt));

        return response;

    }

    //Progress over time
    @Transactional(readOnly = true)
    public List<ProgressEntryResponse> getProgressOverTime(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));

        List<SurveyResult> history = surveyResultRepository.findByUserOrderByGenerateDateAsc(user);

        List<ProgressEntryResponse> progress = new ArrayList<>();
        for (SurveyResult result : history) {
            progress.add(new ProgressEntryResponse(
                result.getGenerateDate(),
                result.getOverallScore(),
                result.getScoreBand(),
                result.getCaringTimeScore(),
                result.getReceivingValueScore(),
                result.getActsOfSupportScore(),
                result.getWordsOfRecognitionScore(),
                result.getPsychologicalTouchScore()
            ));
        }
        return progress;
    }

    //Peer comaprisons by percentile
    @Transactional(readOnly = true)
    public PeerComparisonResponse getPeerComparison(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));

        SurveyResult latest = surveyResultRepository.findFirstByUserOrderByGenerateDateDesc(user)
            .orElseThrow(() -> new EntityNotFoundException("No survey result found for user: " + email));

        PeerComparisonResponse response = new PeerComparisonResponse();

        response.setYourCaringTimeScore(latest.getCaringTimeScore());
        response.setCaringTimePercentile(calculatePercentile(latest.getCaringTimeScore(),
            surveyResultRepository.findAllCaringTimeScores()));

        response.setYourReceivingValueScore(latest.getReceivingValueScore());
        response.setReceivingValuePercentile(calculatePercentile(latest.getReceivingValueScore(),
            surveyResultRepository.findAllReceivingValueScores()));

        response.setYourActsOfSupportScore(latest.getActsOfSupportScore());
        response.setActsOfSupportPercentile(calculatePercentile(latest.getActsOfSupportScore(),
            surveyResultRepository.findAllActsOfSupportScores()));

        response.setYourWordsOfRecognitionScore(latest.getWordsOfRecognitionScore());
        response.setWordsOfRecognitionPercentile(calculatePercentile(latest.getWordsOfRecognitionScore(),
            surveyResultRepository.findAllWordsOfRecognitionScores()));

        response.setYourPsychologicalTouchScore(latest.getPsychologicalTouchScore());
        response.setPsychologicalTouchPercentile(calculatePercentile(latest.getPsychologicalTouchScore(),
            surveyResultRepository.findAllPsychologicalTouchScores()));

        return response;
    }

    // NEW: calculates what % of all scores are at or below the given score
    private int calculatePercentile(int userScore, List<Integer> allScores) {
        if (allScores.isEmpty()) return 0;
        long countAtOrBelow = allScores.stream().filter(s -> s <= userScore).count();
        return (int) Math.round((countAtOrBelow / (double) allScores.size()) * 100);
    }

    @Transactional(readOnly = true)
    public List<Resource> getSuggestedLearningPath(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));

        SurveyResult latest = surveyResultRepository.findFirstByUserOrderByGenerateDateDesc(user)
            .orElseThrow(() -> new EntityNotFoundException("No survey result found for user: " + email));

        List<String> weakestCategories = determineWeakestCategories(
            latest.getCaringTimeScore(),
            latest.getReceivingValueScore(),
            latest.getActsOfSupportScore(),
            latest.getWordsOfRecognitionScore(),
            latest.getPsychologicalTouchScore()
        );

        List<Resource> suggested = new ArrayList<>();
        for (String language : weakestCategories) {
            suggested.addAll(resourceService.getActiveResourcesByLeadershipLanguage(language));
        }
        return suggested;
    }

    @Transactional(readOnly = true)
    public List<Resource> getSuggestedLearningPath(SurveyResult result) {
        List<String> weakestCategories = determineWeakestCategories(
            result.getCaringTimeScore(),
            result.getReceivingValueScore(),
            result.getActsOfSupportScore(),
            result.getWordsOfRecognitionScore(),
            result.getPsychologicalTouchScore()
        );

        List<Resource> suggested = new ArrayList<>();
        for (String language : weakestCategories) {
            suggested.addAll(resourceService.getActiveResourcesByLeadershipLanguage(language));
        }
        return suggested;
    }

    private List<String> determineWeakestCategories(int ct, int rv, int as_, int wr, int pt) {
        Map<String, Integer> scores = new LinkedHashMap<>();
        scores.put("Caring Time", ct);
        scores.put("Receiving Value", rv);
        scores.put("Acts of Support", as_);
        scores.put("Words of Recognition", wr);
        scores.put("Psychological Touch", pt);

        List<String> belowThreshold = scores.entrySet().stream()
            .filter(e -> e.getValue() < NEEDS_ATTENTION_THRESHOLD)
            .sorted(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .toList();

        if (!belowThreshold.isEmpty()) {
            return belowThreshold;
        }

        int minScore = scores.values().stream().min(Integer::compareTo).orElse(0);

        return scores.entrySet().stream()
            .filter(e -> e.getValue() == minScore)
            .map(Map.Entry::getKey)
            .toList();
    }

    @Transactional(readOnly = true)
    public SurveyResult getResultById(Long resultId) {
        SurveyResult result = surveyResultRepository.findById(resultId)
            .orElseThrow(() -> new EntityNotFoundException("Result not found: " + resultId));

        result.getUser().getFullName(); // ensures user is loaded

        return result;
    }
}