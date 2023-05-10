package com.example.examscoring.model;

import java.util.List;

public class MergedExamData {
    private List<String> markedAnswers;
    private ExamData examData;

    public MergedExamData(List<String> markedAnswers, ExamData examData) {
        this.markedAnswers = markedAnswers;
        this.examData = examData;
    }

    public List<String> getMarkedAnswers() {
        return markedAnswers;
    }

    public ExamData getExamData() {
        return examData;
    }
}
