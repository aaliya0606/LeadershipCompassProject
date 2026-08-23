package com.example.leadershipcompass_capstoneprojectbackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.leadershipcompass_capstoneprojectbackend.model.SurveyResult;
import com.example.leadershipcompass_capstoneprojectbackend.model.User;

@Repository
public interface SurveyResultRepository extends JpaRepository<SurveyResult, Long> {

    List<SurveyResult> findByUserOrderByGenerateDateDesc(User user);

    List<SurveyResult> findByUserDepartment(String department);
}
