package com.example.leadershipcompass_capstoneprojectbackend.config;

import com.example.leadershipcompass_capstoneprojectbackend.model.Feedback360Question;
import com.example.leadershipcompass_capstoneprojectbackend.model.Feedback360QuestionOption;
import com.example.leadershipcompass_capstoneprojectbackend.model.Feedback360QuestionType;
import com.example.leadershipcompass_capstoneprojectbackend.repository.Feedback360QuestionOptionRepository;
import com.example.leadershipcompass_capstoneprojectbackend.repository.Feedback360QuestionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final Feedback360QuestionRepository questionRepository;
    private final Feedback360QuestionOptionRepository questionOptionRepository;

    @Override
    public void run(String... args) {

        // Only initialise the questions if they don't already exist.
        if (questionRepository.count() > 0) {
            return;
        }

        // ============================================================
        // 360 DEGREE SURVEY QUESTIONS
        // ============================================================

        List<Feedback360Question> questions = List.of(

                // Q1
                Feedback360Question.builder()
                        .questionNumber(1)
                        .questionText(
                                "In general terms, I would rate [Name] overall communication effectiveness as:")
                        .questionType(Feedback360QuestionType.RATING)
                        .category("Communication")
                        .build(),

                // Q2
                Feedback360Question.builder()
                        .questionNumber(2)
                        .questionText(
                                "In general terms, I would rate [Name] overall people skills and engagement skills as:")
                        .questionType(Feedback360QuestionType.RATING)
                        .category("People Skills")
                        .build(),

                // Q3
                Feedback360Question.builder()
                        .questionNumber(3)
                        .questionText(
                                "In general terms, I would rate [Name] leadership/people leadership effectiveness as:")
                        .questionType(Feedback360QuestionType.RATING)
                        .category("Leadership")
                        .build(),

                // Q4
                Feedback360Question.builder()
                        .questionNumber(4)
                        .questionText(
                                "In general terms, I would rate [Name] ability to set direction and align others (clarity of communication, expectations, priorities) as:")
                        .questionType(Feedback360QuestionType.RATING)
                        .category("Direction & Alignment")
                        .build(),

                // Q5
                Feedback360Question.builder()
                        .questionNumber(5)
                        .questionText(
                                "In general terms, I would rate [Name] decision making (timeliness, sound judgment) as:")
                        .questionType(Feedback360QuestionType.RATING)
                        .category("Decision Making")
                        .build(),

                // Q6
                Feedback360Question.builder()
                        .questionNumber(6)
                        .questionText(
                                "In general terms, I would rate [Name] collaboration across teams/stakeholders (partnering, influence, responsiveness) as:")
                        .questionType(Feedback360QuestionType.RATING)
                        .category("Collaboration")
                        .build(),

                // Q7
                Feedback360Question.builder()
                        .questionNumber(7)
                        .questionText(
                                "In general terms, I would rate [Name] results/impact (ownership, follow through, delivering outcomes) as:")
                        .questionType(Feedback360QuestionType.RATING)
                        .category("Results & Impact")
                        .build(),

                // Q8
                Feedback360Question.builder()
                        .questionNumber(8)
                        .questionText(
                                "In general terms, I would rate [Name] execution strength (planning, prioritisation, meeting deadlines) as:")
                        .questionType(Feedback360QuestionType.RATING)
                        .category("Execution")
                        .build(),

                // Q9
                Feedback360Question.builder()
                        .questionNumber(9)
                        .questionText(
                                "What is 1 example of a time [Name] delivered strong results or made a positive impact?")
                        .questionType(Feedback360QuestionType.TEXT)
                        .category("Results & Impact")
                        .build(),

                // Q10
                Feedback360Question.builder()
                        .questionNumber(10)
                        .questionText(
                                "[Name]'s top strengths are: (Select up to 3)")
                        .questionType(Feedback360QuestionType.MULTI_SELECT)
                        .category("Strengths")
                        .build(),

                // Q11
                Feedback360Question.builder()
                        .questionNumber(11)
                        .questionText(
                                "When working with [Name], I experience [Name] as being: (Select up to 3)")
                        .questionType(Feedback360QuestionType.MULTI_SELECT)
                        .category("Working Style")
                        .build(),

                // Q12
                Feedback360Question.builder()
                        .questionNumber(12)
                        .questionText(
                                "What is the one thing [Name] does really well?")
                        .questionType(Feedback360QuestionType.TEXT)
                        .category("Strengths")
                        .build(),

                // Q13
                Feedback360Question.builder()
                        .questionNumber(13)
                        .questionText(
                                "If [Name] could \"do more of one thing\" to be even more effective, what should that be?")
                        .questionType(Feedback360QuestionType.TEXT)
                        .category("Development")
                        .build(),

                // Q14
                Feedback360Question.builder()
                        .questionNumber(14)
                        .questionText(
                                "The one thing I am willing to do to support [Name] to be an even better leader is:")
                        .questionType(Feedback360QuestionType.TEXT)
                        .category("Support")
                        .build()
        );

        // Save Q1-Q14
        questionRepository.saveAll(questions);


        // ============================================================
        // FIND Q10 AND Q11
        // ============================================================

        Feedback360Question q10 = questionRepository
                .findByQuestionNumber(10)
                .orElseThrow();

        Feedback360Question q11 = questionRepository
                .findByQuestionNumber(11)
                .orElseThrow();


        // ============================================================
        // Q10 OPTIONS - TOP STRENGTHS
        // ============================================================

        List<Feedback360QuestionOption> q10Options = List.of(

                Feedback360QuestionOption.builder()
                        .question(q10)
                        .optionText("Communication")
                        .displayOrder(1)
                        .build(),

                Feedback360QuestionOption.builder()
                        .question(q10)
                        .optionText("Collaboration")
                        .displayOrder(2)
                        .build(),

                Feedback360QuestionOption.builder()
                        .question(q10)
                        .optionText("Problem Solving")
                        .displayOrder(3)
                        .build(),

                Feedback360QuestionOption.builder()
                        .question(q10)
                        .optionText("Ownership")
                        .displayOrder(4)
                        .build(),

                Feedback360QuestionOption.builder()
                        .question(q10)
                        .optionText("Reliability")
                        .displayOrder(5)
                        .build(),

                Feedback360QuestionOption.builder()
                        .question(q10)
                        .optionText("Strategic Thinking")
                        .displayOrder(6)
                        .build(),

                Feedback360QuestionOption.builder()
                        .question(q10)
                        .optionText("Customer Focus")
                        .displayOrder(7)
                        .build(),

                Feedback360QuestionOption.builder()
                        .question(q10)
                        .optionText("Leadership")
                        .displayOrder(8)
                        .build(),

                Feedback360QuestionOption.builder()
                        .question(q10)
                        .optionText("Execution")
                        .displayOrder(9)
                        .build(),

                Feedback360QuestionOption.builder()
                        .question(q10)
                        .optionText("Calm Under Pressure")
                        .displayOrder(10)
                        .build(),

                Feedback360QuestionOption.builder()
                        .question(q10)
                        .optionText("Other")
                        .displayOrder(11)
                        .build()
        );


        // ============================================================
        // Q11 OPTIONS - WORKING STYLE
        // ============================================================

        List<Feedback360QuestionOption> q11Options = List.of(

                Feedback360QuestionOption.builder()
                        .question(q11)
                        .optionText("Caring")
                        .displayOrder(1)
                        .build(),

                Feedback360QuestionOption.builder()
                        .question(q11)
                        .optionText("Proactive")
                        .displayOrder(2)
                        .build(),

                Feedback360QuestionOption.builder()
                        .question(q11)
                        .optionText("Easy to Approach")
                        .displayOrder(3)
                        .build(),

                Feedback360QuestionOption.builder()
                        .question(q11)
                        .optionText("Clear and Structured")
                        .displayOrder(4)
                        .build(),

                Feedback360QuestionOption.builder()
                        .question(q11)
                        .optionText("Open to Feedback")
                        .displayOrder(5)
                        .build(),

                Feedback360QuestionOption.builder()
                        .question(q11)
                        .optionText("Supportive")
                        .displayOrder(6)
                        .build(),

                Feedback360QuestionOption.builder()
                        .question(q11)
                        .optionText("Sometimes Defensive")
                        .displayOrder(7)
                        .build(),

                Feedback360QuestionOption.builder()
                        .question(q11)
                        .optionText("Hard to Get Alignment With")
                        .displayOrder(8)
                        .build(),

                Feedback360QuestionOption.builder()
                        .question(q11)
                        .optionText("Slow to Respond")
                        .displayOrder(9)
                        .build(),

                Feedback360QuestionOption.builder()
                        .question(q11)
                        .optionText("Other")
                        .displayOrder(10)
                        .build()
        );


        // Save Q10 and Q11 options
        questionOptionRepository.saveAll(q10Options);
        questionOptionRepository.saveAll(q11Options);
    }
}