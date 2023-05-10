package com.example.examscoring;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.examscoring.ViewModel.SharedViewModel;
import com.example.examscoring.model.ExamData;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ScoringActivity extends AppCompatActivity {

    private SharedViewModel sharedViewModel;
    private List<String> markedAnswers;
    private ExamData examData;
    private int score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoring);
        Log.d("ScoringActivity", "onCreate: ScoringActivity started");

        // Receive the merged data from the Intent
        if (getIntent() != null) {
            markedAnswers = getIntent().getStringArrayListExtra("markedAnswers");
            examData = (ExamData) getIntent().getSerializableExtra("examData");
        }

        if (markedAnswers != null && examData != null) {
            score = calculateScore(markedAnswers, examData.getAnswers());
            processScoring(score);
        } else {
            if (markedAnswers == null) {
                Log.e("ScoringActivity", "Error: markedAnswers is null. Cannot calculate score.");
            }
            if (examData == null) {
                Log.e("ScoringActivity", "Error: examData is null. Cannot calculate score.");
            }
        }

        Button buttonSaveResult = findViewById(R.id.button_save_result);
        buttonSaveResult.setOnClickListener(v -> saveResultsAsCsv(markedAnswers, examData.getAnswers(), score));

    }


    private int calculateScore(List<String> markedAnswers, List<String> extractedAnswers) {
        Log.d("ScoringActivity", "calculateScore(): Marked answers: " + markedAnswers);
        Log.d("ScoringActivity", "calculateScore(): Extracted answers: " + extractedAnswers);
        int score = 0;
        int numberOfQuestions = markedAnswers.size();

        for (int i = 0; i < numberOfQuestions; i++) {
            if (markedAnswers.get(i).equals(extractedAnswers.get(i))) {
                score++;
            }
        }
        Log.d("ScoringActivity", "calculateScore(): Calculated score: " + score);
        return score;
    }

    private void processScoring(int score) {
        // Display the number of hits and the total number of questions
        TextView tvScore = findViewById(R.id.textView_score);
        TextView textViewStudentName = findViewById(R.id.textView_student_name);
        TextView textViewStudentID = findViewById(R.id.textView_student_id);

        int totalQuestions = examData.getAnswers().size();
        tvScore.setText("Score: " + score + " / " + totalQuestions);

        // Display the student's name and ID
        textViewStudentName.setText("Student Name: " + examData.getStudentName());
        textViewStudentID.setText("Student ID: " + examData.getStudentID());

        // Display question details
        LinearLayout questionDetailsContainer = findViewById(R.id.linear_layout_question_details);
        LayoutInflater inflater = getLayoutInflater();

        // Limit the number of displayed questions to 20
        int maxQuestions = Math.min(totalQuestions, 20);

        for (int i = 0; i < maxQuestions; i++) {
            View questionDetailView = inflater.inflate(R.layout.question_detail_item, null);

            TextView tvQuestionNumber = questionDetailView.findViewById(R.id.tv_question_number);
            TextView tvAnswer = questionDetailView.findViewById(R.id.tv_answer);
            TextView tvSelectedOption = questionDetailView.findViewById(R.id.tv_selected_option);
            TextView tvCorrectAnswer = questionDetailView.findViewById(R.id.tv_correct_answer);

            tvQuestionNumber.setText("Question " + (i + 1) + ":");
            tvAnswer.setText("Answer: " + examData.getAnswers().get(i));
            tvSelectedOption.setText("Selected: " + markedAnswers.get(i));
            tvCorrectAnswer.setText("Correct: " + (examData.getAnswers().get(i).equals(markedAnswers.get(i)) ? "Yes" : "No"));

            questionDetailsContainer.addView(questionDetailView);
        }
    }


    private void saveResultsAsCsv(List<String> markedAnswers, List<String> extractedAnswers, int score) {
        // Save the results as a CSV file
        String fileName = examData.getStudentID() + "_" + examData.getStudentName() + ".csv";
        File outputFile = new File(getExternalFilesDir(null), fileName);

        try (CSVWriter writer = new CSVWriter(new FileWriter(outputFile))) {
            // Header
            String[] header = {"Student ID", "Student Name", "Q1", "Q2", "Q3", "Q4", "Q5", "Q6", "Q7", "Q8", "Q9", "Q10", "Q11", "Q12", "Q13", "Q14", "Q15", "Q16", "Q17", "Q18", "Q19", "Q20", "Score"};
            writer.writeNext(header);

            // Data
            String[] data = new String[23];
            data[0] = examData.getStudentID();
            data[1] = examData.getStudentName();
            for (int i = 0; i < markedAnswers.size(); i++) {
                data[i + 2] = (examData.getAnswers().get(i).equals(markedAnswers.get(i)) ? "Yes" : "No");
            }
            data[22] = String.valueOf(score + " / 20");
            writer.writeNext(data);

            Toast.makeText(this, "Saved result to: " + outputFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("ScoringActivity", "Error saving result as CSV", e);
            Toast.makeText(this, "Error saving result as CSV", Toast.LENGTH_SHORT).show();
        }
    }

}
