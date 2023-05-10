package com.example.examscoring.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ExamData implements Serializable {
    private String studentName;
    private String studentID;
    private List<String> answers;

    public ExamData(String studentName, String studentID, List<String> answers) {
        this.studentName = studentName;
        this.studentID = studentID;
        this.answers = answers;
    }
    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }

    @Override
    public String toString() {
        return "ExamData{" +
                "studentName='" + studentName + '\'' +
                ", studentID='" + studentID + '\'' +
                ", answerResults=" + answers +
                '}';
    }
}
