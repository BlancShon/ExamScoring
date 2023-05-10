package com.example.examscoring.ViewModel;

import android.util.Log;
import android.util.Pair;

import androidx.annotation.MainThread;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.example.examscoring.model.ExamData;
import com.example.examscoring.model.MergedExamData;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SharedViewModel extends ViewModel {
    private MutableLiveData<List<String>> markedAnswers;
    private MutableLiveData<ExamData> examData;
    private MediatorLiveData<Pair<List<String>, ExamData>> combinedData;

    public SharedViewModel() {
        markedAnswers = new MutableLiveData<>();
        examData = new MutableLiveData<>();
        combinedData = new MediatorLiveData<>();

        // Observe changes in markedAnswers and examData LiveData objects
        combinedData.addSource(markedAnswers, value -> combinedData.setValue(new Pair<>(value, examData.getValue())));
        combinedData.addSource(examData, value -> combinedData.setValue(new Pair<>(markedAnswers.getValue(), value)));
    }

    public MutableLiveData<List<String>> getMarkedAnswers() {
        return markedAnswers;
    }

    public void setMarkedAnswers(List<String> answers) {
        markedAnswers.postValue(answers);
        Log.d("SharedViewModel", "Marked answers set: " + answers);
    }

    public MutableLiveData<ExamData> getExamData() {
        return examData;
    }

    public void setExamData(ExamData examData) {
        ExamData data = new ExamData(
                examData.getStudentName(),
                examData.getStudentID(),
                examData.getAnswers());
        this.examData.postValue(data);
        Log.d("SharedViewModel", "Exam data set: " + data);
    }

    // Add the getCombinedData() method
    public LiveData<Pair<List<String>, ExamData>> getCombinedData() {
        return combinedData;
    }

    @Override
    public String toString() {
        return "SharedViewModel {" +
                "markedAnswers=" + markedAnswers.getValue() +
                ", examData=" + examData.getValue() +
                '}';
    }

    private MutableLiveData<MergedExamData> mergedExamData = new MutableLiveData<>();

    public void setMergedExamData(MergedExamData data) {
        mergedExamData.setValue(data);
    }

    public LiveData<MergedExamData> getMergedExamData() {
        return mergedExamData;
    }

    public static class BooleanSingleLiveEvent extends MutableLiveData<Boolean> {
        private final AtomicBoolean pending = new AtomicBoolean(false);

        @MainThread
        public void observe(LifecycleOwner owner, final Observer<? super Boolean> observer) {
            if (hasActiveObservers()) {
                Log.w("SharedViewModel", "Multiple observers registered but only one will be notified of changes.");
            }

            super.observe(owner, t -> {
                if (pending.compareAndSet(true, false)) {
                    observer.onChanged(t);
                }
            });
        }

        @MainThread
        public void setValue(Boolean value) {
            pending.set(true);
            super.setValue(value);
        }

        public void call() {
            setValue(null);
        }
    }

    private BooleanSingleLiveEvent dataReadyEvent = new BooleanSingleLiveEvent();

    public BooleanSingleLiveEvent getDataReadyEvent() {
        return dataReadyEvent;
    }
}





