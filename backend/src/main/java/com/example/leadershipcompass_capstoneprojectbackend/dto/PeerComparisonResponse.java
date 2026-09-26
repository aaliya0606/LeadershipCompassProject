package com.example.leadershipcompass_capstoneprojectbackend.dto;

public class PeerComparisonResponse {

    private int yourCaringTimeScore;
    private int caringTimePercentile;

    private int yourReceivingValueScore;
    private int receivingValuePercentile;

    private int yourActsOfSupportScore;
    private int actsOfSupportPercentile;

    private int yourWordsOfRecognitionScore;
    private int wordsOfRecognitionPercentile;

    private int yourPsychologicalTouchScore;
    private int psychologicalTouchPercentile;

    public int getYourCaringTimeScore() { return yourCaringTimeScore; }
    public void setYourCaringTimeScore(int yourCaringTimeScore) { this.yourCaringTimeScore = yourCaringTimeScore; }

    public int getCaringTimePercentile() { return caringTimePercentile; }
    public void setCaringTimePercentile(int caringTimePercentile) { this.caringTimePercentile = caringTimePercentile; }

    public int getYourReceivingValueScore() { return yourReceivingValueScore; }
    public void setYourReceivingValueScore(int yourReceivingValueScore) { this.yourReceivingValueScore = yourReceivingValueScore; }

    public int getReceivingValuePercentile() { return receivingValuePercentile; }
    public void setReceivingValuePercentile(int receivingValuePercentile) { this.receivingValuePercentile = receivingValuePercentile; }

    public int getYourActsOfSupportScore() { return yourActsOfSupportScore; }
    public void setYourActsOfSupportScore(int yourActsOfSupportScore) { this.yourActsOfSupportScore = yourActsOfSupportScore; }

    public int getActsOfSupportPercentile() { return actsOfSupportPercentile; }
    public void setActsOfSupportPercentile(int actsOfSupportPercentile) { this.actsOfSupportPercentile = actsOfSupportPercentile; }

    public int getYourWordsOfRecognitionScore() { return yourWordsOfRecognitionScore; }
    public void setYourWordsOfRecognitionScore(int yourWordsOfRecognitionScore) { this.yourWordsOfRecognitionScore = yourWordsOfRecognitionScore; }

    public int getWordsOfRecognitionPercentile() { return wordsOfRecognitionPercentile; }
    public void setWordsOfRecognitionPercentile(int wordsOfRecognitionPercentile) { this.wordsOfRecognitionPercentile = wordsOfRecognitionPercentile; }

    public int getYourPsychologicalTouchScore() { return yourPsychologicalTouchScore; }
    public void setYourPsychologicalTouchScore(int yourPsychologicalTouchScore) { this.yourPsychologicalTouchScore = yourPsychologicalTouchScore; }

    public int getPsychologicalTouchPercentile() { return psychologicalTouchPercentile; }
    public void setPsychologicalTouchPercentile(int psychologicalTouchPercentile) { this.psychologicalTouchPercentile = psychologicalTouchPercentile; }
}