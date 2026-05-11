// Nikki, can you please add your implementation here for your survey implementation,
// and if you can, can we please also document our codes Javadoc stlye for our documentation.
// Thank you, Nikki. Gkenda


package com.example.leadershipcompass_capstoneprojectbackend.controller;

import com.example.leadershipcompass_capstoneprojectbackend.dto.SurveyResultResponse;
import com.example.leadershipcompass_capstoneprojectbackend.dto.SurveySubmissionRequest;

import lombok.RequiredArgsConstructor;

import com.example.leadershipcompass_capstoneprojectbackend.dto.SurveyQuestions;
import com.example.leadershipcompass_capstoneprojectbackend.dto.SurveyResult;
import com.example.leadershipcompass_capstoneprojectbackend.dto.SurveyService;
import com.example.leadershipcompass_capstoneprojectbackend.dto.SurveyResultResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/survey")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    @PostMapping("/submit")
    public ResponseEntity<SurveyResultResponse> submitSurvey(
            @RequestBody SurveySubmissionRequest request,
            @AuthenticationPrincipal UserDetials userDetails){

        try {
            SurveyResultResponse response = 
                    surveyService.submitSurvey(request, userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<SurveyResult>> getSurveyHistory(
            @AuthenticationPrincipal UserDetails userDetails){
                
                return ResponseEntity.ok(
                    surveyService.getSurveyHistoryForUser(userDetails.getUsername()));
    

    }  

    @GetMapping("/questions")
    public ResponseEntity<List<SurveyQuestions>> getQuestions() {
        return ResponseEntity.ok(surveyService.loadQuestions());
    }

    @PostMapping("/admin/questions")
    public ResponseEntity<SurveyQuestions> addQuestion(
        @RequestBody Map<String, String> body) {
        
        try{
            SurveyQuestions created = surveyService.addQuestion(
                body.get("questionText"), body.get("category"));
                return ResponseEntity.status(HttpStatus.CREATED).body(created);
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().build();
            }
    }

    
    @PutMapping("/admin/questions/{id}")
    public ResponseEntity<SurveyQuestions> addQuestion(
        @PathVariable in id,
        @RequestBody Map<String, String> body) {
        
        try{
            return ResponseEntity.ok(
                surveyService.editQuestion(id, body.get("questionText"))
            } catch (jakarta.persistence.EntityNotFoundException ex) {
                return ResponseEntity.notFound().build()
            }
    }

    @DeleteMapping("/admin/questions/{id}")
    public ResponseEntity<Void> removeQuestion(@PathVariable int id){
        try{
            surveyService.removeQuestion(id);
            return ResponseEntity.noContent().build();
        } catch (jakarta.persistence.EntityNotFoundException ex){
            return ResponseEntity.notFound().build();
        }
    }
}
