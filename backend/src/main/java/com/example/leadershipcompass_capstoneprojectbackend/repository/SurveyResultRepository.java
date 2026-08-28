package com.example.leadershipcompass_capstoneprojectbackend.repository;

import com.example.leadershipcompass_capstoneprojectbackend.model.SurveyResult;
import com.example.leadershipcompass_capstoneprojectbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyResultRepository extends JpaRepository<SurveyResult, Long> {

    List<SurveyResult> findByUserOrderByGenerateDateDesc(User user);

    List<SurveyResult> findByUserOrderByGenerateDateAsc(User user);

    // NEW: gets the current user's most recent result, for peer comparison
    Optional<SurveyResult> findFirstByUserOrderByGenerateDateDesc(User user);

    // NEW: raw score lists, needed to calculate percentile rank per category
    @Query("SELECT r.caringTimeScore FROM SurveyResult r")
    List<Integer> findAllCaringTimeScores();

    @Query("SELECT r.receivingValueScore FROM SurveyResult r")
    List<Integer> findAllReceivingValueScores();

    @Query("SELECT r.actsOfSupportScore FROM SurveyResult r")
    List<Integer> findAllActsOfSupportScores();

    @Query("SELECT r.wordsOfRecognitionScore FROM SurveyResult r")
    List<Integer> findAllWordsOfRecognitionScores();

    @Query("SELECT r.psychologicalTouchScore FROM SurveyResult r")
    List<Integer> findAllPsychologicalTouchScores();
}
