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

import java.util.List;
import java.util.Map;

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

    private String band(int score){
        if (score >= 40) return "High";
        if (score >= 30) return "Strong intent";
        if (score >= 20) return "Needs attention";
        return "Blind spot";
    }

    /// Only have placeholder values for feedback :
    private String message(String categoryName, int score){
        if (score >= 40){
            return "placeholder: feedback on high score";
        }
        if (score >= 30){
             return "placeholder: feedback on mid score";
        }
        if (score >= 20){
             return "placeholder: feedback on low score";
        }
        return categoryName + "may be a leadership blind spot"
        + "prioritise this action plan....";

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
        response.setPsychologicalTouchMessage(message("Psychological", pt));

        return response;

    }
}
