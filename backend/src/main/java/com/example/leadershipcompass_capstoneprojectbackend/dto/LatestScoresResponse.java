package com.example.leadershipcompass_capstoneprojectbackend.dto;

/**NEW: 28/09/26
 * Most recent survey scores for the dashboard radar chart.
 * All scores are null when the user has not completed a survey yet.
 */
public class LatestScoresResponse {
    private Integer caringTimeScore;
    private Integer receivingValueScore;
    private Integer actsOfSupportScore;
    private Integer wordsOfRecognitionScore;
    private Integer psychologicalTouchScore;

    public LatestScoresResponse(Integer caringTimeScore, Integer receivingValueScore, Integer actsOfSupportScore,
                                 Integer wordsOfRecognitionScore, Integer psychologicalTouchScore) {
        this.caringTimeScore = caringTimeScore;
        this.receivingValueScore = receivingValueScore;
        this.actsOfSupportScore = actsOfSupportScore;
        this.wordsOfRecognitionScore = wordsOfRecognitionScore;
        this.psychologicalTouchScore = psychologicalTouchScore;
    }

    public Integer getCaringTimeScore() { return caringTimeScore; }
    public Integer getReceivingValueScore() { return receivingValueScore; }
    public Integer getActsOfSupportScore() { return actsOfSupportScore; }
    public Integer getWordsOfRecognitionScore() { return wordsOfRecognitionScore; }
    public Integer getPsychologicalTouchScore() { return psychologicalTouchScore; }
}