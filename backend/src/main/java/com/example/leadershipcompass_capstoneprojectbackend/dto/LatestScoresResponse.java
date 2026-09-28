package com.example.leadershipcompass_capstoneprojectbackend.dto;

public class LatestScoresResponse {
    private int caringTimeScore;
    private int receivingValueScore;
    private int actsOfSupportScore;
    private int wordsOfRecognitionScore;
    private int psychologicalTouchScore;

    public LatestScoresResponse(int caringTimeScore, int receivingValueScore, int actsOfSupportScore,
                                 int wordsOfRecognitionScore, int psychologicalTouchScore) {
        this.caringTimeScore = caringTimeScore;
        this.receivingValueScore = receivingValueScore;
        this.actsOfSupportScore = actsOfSupportScore;
        this.wordsOfRecognitionScore = wordsOfRecognitionScore;
        this.psychologicalTouchScore = psychologicalTouchScore;
    }

    public int getCaringTimeScore() { return caringTimeScore; }
    public int getReceivingValueScore() { return receivingValueScore; }
    public int getActsOfSupportScore() { return actsOfSupportScore; }
    public int getWordsOfRecognitionScore() { return wordsOfRecognitionScore; }
    public int getPsychologicalTouchScore() { return psychologicalTouchScore; }
}