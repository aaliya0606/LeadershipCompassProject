package com.example.leadershipcompass_capstoneprojectbackend.dto;

public class SurveyResultResponse {

    private Long resultId;
    private int overallScore;
    private String overallBand;
    private String summary;

    private int caringTimeScore;
    private String caringTimeBand;
    private String caringTimeMessage;

    private int receivingValueScore;
    private String receivingValueBand;
    private String receivingValueMessage;

    private int actsOfSupportScore;
    private String actsOfSupportBand;
    private String actsOfSupportMessage;

    private int wordsOfRecognitionScore;
    private String wordsOfRecognitionBand;
    private String wordsOfRecognitionMessage;

    private int psychologicalTouchScore;
    private String psychologicalTouchBand;
    private String psychologicalTouchMessage;

    public SurveyResultResponse() {}

    public Long getResultId() { return resultId; }
    public void setResultId(Long resultId) { this.resultId = resultId; }

    public int getOverallScore() { return overallScore; }
    public void setOverallScore(int overallScore) { this.overallScore = overallScore; }

    public String getOverallBand() { return overallBand; }
    public void setOverallBand(String overallBand) { this.overallBand = overallBand; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public int getCaringTimeScore() { return caringTimeScore; }
    public void setCaringTimeScore(int caringTimeScore) { this.caringTimeScore = caringTimeScore; }

    public String getCaringTimeBand() { return caringTimeBand; }
    public void setCaringTimeBand(String caringTimeBand) { this.caringTimeBand = caringTimeBand; }

    public String getCaringTimeMessage() { return caringTimeMessage; }
    public void setCaringTimeMessage(String caringTimeMessage) { this.caringTimeMessage = caringTimeMessage; }

    public int getReceivingValueScore() { return receivingValueScore; }
    public void setReceivingValueScore(int receivingValueScore) { this.receivingValueScore = receivingValueScore; }

    public String getReceivingValueBand() { return receivingValueBand; }
    public void setReceivingValueBand(String receivingValueBand) { this.receivingValueBand = receivingValueBand; }

    public String getReceivingValueMessage() { return receivingValueMessage; }
    public void setReceivingValueMessage(String receivingValueMessage) { this.receivingValueMessage = receivingValueMessage; }

    public int getActsOfSupportScore() { return actsOfSupportScore; }
    public void setActsOfSupportScore(int actsOfSupportScore) { this.actsOfSupportScore = actsOfSupportScore; }

    public String getActsOfSupportBand() { return actsOfSupportBand; }
    public void setActsOfSupportBand(String actsOfSupportBand) { this.actsOfSupportBand = actsOfSupportBand; }

    public String getActsOfSupportMessage() { return actsOfSupportMessage; }
    public void setActsOfSupportMessage(String actsOfSupportMessage) { this.actsOfSupportMessage = actsOfSupportMessage; }

    public int getWordsOfRecognitionScore() { return wordsOfRecognitionScore; }
    public void setWordsOfRecognitionScore(int wordsOfRecognitionScore) { this.wordsOfRecognitionScore = wordsOfRecognitionScore; }

    public String getWordsOfRecognitionBand() { return wordsOfRecognitionBand; }
    public void setWordsOfRecognitionBand(String wordsOfRecognitionBand) { this.wordsOfRecognitionBand = wordsOfRecognitionBand; }

    public String getWordsOfRecognitionMessage() { return wordsOfRecognitionMessage; }
    public void setWordsOfRecognitionMessage(String wordsOfRecognitionMessage) { this.wordsOfRecognitionMessage = wordsOfRecognitionMessage; }

    public int getPsychologicalTouchScore() { return psychologicalTouchScore; }
    public void setPsychologicalTouchScore(int psychologicalTouchScore) { this.psychologicalTouchScore = psychologicalTouchScore; }

    public String getPsychologicalTouchBand() { return psychologicalTouchBand; }
    public void setPsychologicalTouchBand(String psychologicalTouchBand) { this.psychologicalTouchBand = psychologicalTouchBand; }

    public String getPsychologicalTouchMessage() { return psychologicalTouchMessage; }
    public void setPsychologicalTouchMessage(String psychologicalTouchMessage) { this.psychologicalTouchMessage = psychologicalTouchMessage; }
}