package com.example.examscoring.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examscoring.MainActivity;
import com.example.examscoring.R;
import com.example.examscoring.ScoringActivity;
import com.example.examscoring.ViewModel.SharedViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SetAnswersFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private AnswersAdapter mAdapter;
    private List<String> mAnswers;
    private SharedViewModel sharedViewModel;

    public SetAnswersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        List<String> savedAnswers = sharedViewModel.getMarkedAnswers().getValue();

        if (savedAnswers != null) {
            mAnswers = new ArrayList<>(savedAnswers);
            Log.d("SetAnswersFragment", "onCreate: Saved answers found");
        } else {
            mAnswers = new ArrayList<>(Collections.nCopies(20, "A"));
            Log.d("SetAnswersFragment", "onCreate: No saved answers found, using default");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_set_answers, container, false);

        Log.d("SetAnswersFragment", "onCreateView: Fragment created");

        // Initialize RecyclerView
        mRecyclerView = view.findViewById(R.id.recyclerView_answers);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set the adapter for the RecyclerView
        mAdapter = new AnswersAdapter(mAnswers);
        mRecyclerView.setAdapter(mAdapter);

        // Initialize sharedViewModel
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Set the onClickListener for the Confirm Answers button
        view.findViewById(R.id.button_confirm_answers).setOnClickListener(v -> {
            sharedViewModel.getMarkedAnswers().postValue(mAnswers);
            Log.d("SetAnswersFragment", "Marked answers set: " + mAnswers);

            // Display a toast message
            Toast.makeText(getContext(), "Answers successfully set!", Toast.LENGTH_SHORT).show();

            // Navigate back to MainActivity
            requireActivity().onBackPressed();
            Log.d("SetAnswersFragment", "Navigating back to MainActivity");
        });
        return view;
    }

    // Define the adapter for the RecyclerView
    public class AnswersAdapter extends RecyclerView.Adapter<AnswersAdapter.AnswerViewHolder> {

        private List<String> mAnswers;
        private String[] mOptions = {"A", "B", "C", "D"};

        public AnswersAdapter(List<String> answers) {
            mAnswers = answers;
        }

        @NonNull
        @Override
        public AnswerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_answer, parent, false);
            return new AnswerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AnswerViewHolder holder, int position) {
            String answer = mAnswers.get(position);
            holder.mTextViewQuestionNumber.setText(String.format(Locale.getDefault(), "Question %d", position + 1));

            // Set the checked RadioButton based on the saved answer
            int selectedOptionIndex = Arrays.asList(mOptions).indexOf(answer);
            for (int i = 0; i < mOptions.length; i++) {
                int finalI = i; // Add this line
                holder.mRadioButtons[i].setChecked(i == selectedOptionIndex);
                holder.mRadioButtons[i].setOnClickListener(v -> mAnswers.set(position, mOptions[finalI])); // Modify this line
            }
        }


        @Override
        public int getItemCount() {
            return mAnswers.size();
        }

        public class AnswerViewHolder extends RecyclerView.ViewHolder {
            TextView mTextViewQuestionNumber;
            RadioButton[] mRadioButtons;

            public AnswerViewHolder(@NonNull View itemView) {
                super(itemView);
                mTextViewQuestionNumber = itemView.findViewById(R.id.textView_question_number);
                mRadioButtons = new RadioButton[]{
                        itemView.findViewById(R.id.radioButton_A),
                        itemView.findViewById(R.id.radioButton_B),
                        itemView.findViewById(R.id.radioButton_C),
                        itemView.findViewById(R.id.radioButton_D)
                };
            }
        }
    }

}



