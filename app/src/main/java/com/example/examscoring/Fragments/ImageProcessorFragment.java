package com.example.examscoring.Fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.examscoring.R;
import com.example.examscoring.ScoringActivity;
import com.example.examscoring.ViewModel.SharedViewModel;
import com.example.examscoring.model.ExamData;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageProcessorFragment extends Fragment {
    private String imagePath;
    private Bitmap imageBitmap;
    private SharedViewModel sharedViewModel;
    private List<String> markedAnswers;
    private ExamData examData;

    public ImageProcessorFragment() {
        // Required empty public constructor
    }

    public static ImageProcessorFragment newInstance(String imagePath) {
        ImageProcessorFragment fragment = new ImageProcessorFragment();
        Bundle args = new Bundle();
        args.putString("imagePath", imagePath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imagePath = getArguments().getString("imagePath");
        }
        if (!OpenCVLoader.initDebug()) {
            Log.e("ImageProcessorFragment", "OpenCV library not loaded.");
        } else {
            Log.d("ImageProcessorFragment", "OpenCV library loaded successfully.");
        }
        copyTessDataToInternalStorage();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_processor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("ImageProcessorFragment", "onViewCreated: Loading image from path: " + imagePath);

        // Initialize sharedViewModel
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Observe the marked answers from SharedViewModel
        sharedViewModel.getMarkedAnswers().observe(getViewLifecycleOwner(), markedAnswers -> {
            // Store the marked answers in a field
            this.markedAnswers = markedAnswers;
            Log.d("ImageProcessorFragment", "getMarkedAnswers: " + markedAnswers);

            // Call a method to check if we have all the data needed to proceed
            checkAndProceedToScoring();
        });

        // Load the image from the imagePath
        imageBitmap = BitmapFactory.decodeFile(imagePath);

        // Preprocess the image
        Bitmap preprocessedImage = preprocessImage(imageBitmap);

        if (preprocessedImage != null) {
            // Define the regions for OCR
            Rect studentNameRegion = new Rect(497, 85, 793, 105); // Student Name region
            Rect studentIDRegion = new Rect(497, 271, 793, 105); // Student ID region

            // Extract student info data
            String studentName = performOcrForStudentInfo(preprocessedImage, studentNameRegion, true);
            String studentID = performOcrForStudentInfo(preprocessedImage, studentIDRegion, false);

            // Log student info extraction results
            Log.d("ImageProcessorFragment", "Student Name1: " + studentName);
            Log.d("ImageProcessorFragment", "Student ID1: " + studentID);

            // Define the regions for checking filled answer bubbles
            List<Rect> answerRegions = new ArrayList<>();
            answerRegions.add(new Rect(116, 593, 534, 124));
            answerRegions.add(new Rect(116, 722, 534, 124));
            answerRegions.add(new Rect(116, 851, 534, 124));
            answerRegions.add(new Rect(116, 980, 534, 124));
            answerRegions.add(new Rect(116, 1109, 534, 124));
            answerRegions.add(new Rect(116, 1238, 534, 124));
            answerRegions.add(new Rect(116, 1367, 534, 124));
            answerRegions.add(new Rect(116, 1496, 534, 124));
            answerRegions.add(new Rect(116, 1625, 534, 124));
            answerRegions.add(new Rect(116, 1754, 534, 124));
            answerRegions.add(new Rect(852 , 593, 534, 124));
            answerRegions.add(new Rect(852 , 722, 534, 124));
            answerRegions.add(new Rect(852 , 851, 534, 124));
            answerRegions.add(new Rect(852 , 980, 534, 124));
            answerRegions.add(new Rect(852 , 1109, 534, 124));
            answerRegions.add(new Rect(852 , 1238, 534, 124));
            answerRegions.add(new Rect(852 , 1367, 534, 124));
            answerRegions.add(new Rect(852 , 1496, 534, 124));
            answerRegions.add(new Rect(852 , 1625, 534, 124));
            answerRegions.add(new Rect(852 , 1754, 534, 124));

            // Check filled answer bubbles
            List<String> answerResults = new ArrayList<>();
            int answerIndex = 1;
            for (Rect region : answerRegions) {
                String answer = checkFilledAnswerBubble(preprocessedImage, region);
                answerResults.add(answer);
                answerIndex++;
            }

            // Create ExamData object
            examData = new ExamData(studentName, studentID, answerResults);

            // Set the examData in the SharedViewModel
            sharedViewModel.getExamData().postValue(examData);
            Log.d("ImageProcessorFragment", "Exam data set: " + examData);

            // Call checkAndProceedToScoring() instead of starting ScoringActivity here
            checkAndProceedToScoring();
        } else {
            Log.e("ImageProcessorFragment", "Error: preprocessedImage is null. Skipping OCR.");
        }
    }


    private void checkAndProceedToScoring() {
        if (examData != null && markedAnswers != null) {
            Log.d("ImageProcessorFragment", "Final examData : " + examData);
            Log.d("ImageProcessorFragment", "Final markedAnswers : " + markedAnswers);

            // Start ScoringActivity with merged data
            Intent intent = new Intent(getActivity(), ScoringActivity.class);
            intent.putExtra("markedAnswers", new ArrayList<>(markedAnswers));
            intent.putExtra("examData", examData);
            startActivity(intent);
        } else {
            Log.d("ImageProcessorFragment", "checkAndProceedToScoring: Waiting for markedAnswers and examData.");
        }
    }



    private Bitmap preprocessImage(Bitmap originalImage) {
        Log.d("ImageProcessorFragment", "Preprocessing image...");

        if (originalImage == null) {
            Log.e("ImageProcessorFragment", "Error: originalImage is null.");
            return null;
        }

        // Convert Bitmap to Mat
        Mat mat = new Mat();
        Utils.bitmapToMat(originalImage, mat);

        // Save the original image
        saveMatToExternalStorage(mat, "original.png");

        // Resize the image
        Size newSize = new Size(1500, 2000); // Example size
        Imgproc.resize(mat, mat, newSize);

        // Save the resized image
        saveMatToExternalStorage(mat, "resized.png");

        // Convert to grayscale
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);

        // Save the grayscale image
        saveMatToExternalStorage(mat, "grayscale.png");

        // Apply binary thresholding
        double thresholdValue = 250; // Example threshold value
        double maxValue = 255;
        Imgproc.threshold(mat, mat, thresholdValue, maxValue, Imgproc.THRESH_BINARY);

        // Save the binary image
        saveMatToExternalStorage(mat, "binary.png");

        // Apply Gaussian blur
        Size ksize = new Size(1, 1);
        Imgproc.GaussianBlur(mat, mat, ksize, 0);

        // Save the blurred image
        saveMatToExternalStorage(mat, "blurred.png");

        // Apply morphology operation
        int morphType = Imgproc.MORPH_DILATE; // Example morphology type
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 1));
        Imgproc.morphologyEx(mat, mat, morphType, kernel);

        // Save the morphology processed image
        saveMatToExternalStorage(mat, "morphology.png");

        // Convert Mat back to Bitmap
        Bitmap preprocessedImage = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, preprocessedImage);

        return preprocessedImage;
    }

    private void saveMatToExternalStorage(Mat mat, String filename) {
        Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);

        if (isExternalStorageWritable()) {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File file = new File(path, filename);

            try (FileOutputStream out = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                Log.d("ImageProcessorFragment", "Saved image to: " + file.getAbsolutePath());
            } catch (IOException e) {
                Log.e("ImageProcessorFragment", "Error saving image to external storage: " + e.getMessage());
            }
        } else {
            Log.e("ImageProcessorFragment", "External storage is not writable.");
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private String performOcrForStudentInfo(Bitmap preprocessedImage, Rect region, boolean isStudentName) {
        Log.d("ImageProcessorFragment", "Performing OCR for Student Info...");

        TessBaseAPI tessBaseAPI = new TessBaseAPI();

        // Set the Tesseract data path and language
        String dataPath = requireContext().getFilesDir().getAbsolutePath() + "/tesseract/";
        tessBaseAPI.init(dataPath, "eng");

        if (isStudentName) {
            // If the region is for the student name, set a whitelist for alphabets and spaces
            tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz ");
        } else {
            // If the region is for the student ID, set a whitelist for digits only
            tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "0123456789");
        }

        // Crop the region
        Bitmap croppedImage = Bitmap.createBitmap(preprocessedImage, region.x, region.y, region.width, region.height);

        // Set the cropped image
        tessBaseAPI.setImage(croppedImage);

        // Perform OCR
        String ocrResult = tessBaseAPI.getUTF8Text();
        Log.d("ImageProcessorFragment", "OCR Result: " + ocrResult);

        // Cleanup
        tessBaseAPI.end();

        // Remove any non-alphabetic characters if the region is for the student name
        if (isStudentName) {
            ocrResult = ocrResult.replaceAll("[^A-Za-z]", "");
        }

        // Return the OCR result
        return ocrResult.trim();
    }



    private int currentQuestionNumber = 1;

    private String checkFilledAnswerBubble(Bitmap preprocessedImage, Rect region) {
        // Define the regions for each option (A, B, C, and D)
        int smallerWidth = region.width / 8;
        int equalWidth = (region.width - smallerWidth) / 4;

        Rect[] optionRegions = new Rect[]{
                new Rect(region.x, region.y, smallerWidth, region.height),
                new Rect(region.x + smallerWidth, region.y, equalWidth, region.height),
                new Rect(region.x + smallerWidth + equalWidth, region.y, equalWidth, region.height),
                new Rect(region.x + smallerWidth + 2 * equalWidth, region.y, equalWidth, region.height),
                new Rect(region.x + smallerWidth + 3 * equalWidth, region.y, equalWidth, region.height)
        };

        double[] optionIntensities = new double[4];

        // Create a mutable copy of the preprocessedImage to draw rectangles on
        Bitmap markedImage = preprocessedImage.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(markedImage);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);

        // Draw rectangles for A, B, C, and D options only (skip the Question number rectangle)
        for (int i = 1; i < optionRegions.length; i++) {
            Bitmap optionBitmap = Bitmap.createBitmap(preprocessedImage, optionRegions[i].x, optionRegions[i].y, optionRegions[i].width, optionRegions[i].height);
            optionIntensities[i - 1] = getIntensity(optionBitmap);

            // Draw a rectangle on the markedImage to mark the area to be cut
            canvas.drawRect(optionRegions[i].x, optionRegions[i].y, optionRegions[i].x + optionRegions[i].width, optionRegions[i].y + optionRegions[i].height, paint);
        }

        // Save the markedImage as a PNG file
        String fileName = "marked_image_" + currentQuestionNumber + ".png";
        saveBitmapAsPng(markedImage, fileName);

        // Increment the currentQuestionNumber
        currentQuestionNumber++;

        return getFilledBubbleLetter(optionIntensities);
    }

    public void processAllQuestions(Bitmap preprocessedImage, List<Rect> questionRegions) {
        List<String> answers = new ArrayList<>();

        for (int i = 0; i < questionRegions.size(); i++) {
            String answer = checkFilledAnswerBubble(preprocessedImage, questionRegions.get(i));
            answers.add(answer);
        }
    }


    public void saveBitmapAsPng(Bitmap bitmap, String fileName) {
        FileOutputStream out = null;
        try {
            File storageDir = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "your_app_name");
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }

            File file = new File(storageDir, fileName);
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private double getIntensity(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        double intensity = 0;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = bitmap.getPixel(x, y);
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);
                intensity += (255 - r) + (255 - g) + (255 - b);
            }
        }

        return intensity;
    }



    private String getFilledBubbleLetter(double[] optionIntensities) {
        int maxIndex = 0;
        double maxValue = optionIntensities[0];

        for (int i = 1; i < optionIntensities.length; i++) {
            if (optionIntensities[i] > maxValue) {
                maxValue = optionIntensities[i];
                maxIndex = i;
            }
        }

        switch (maxIndex) {
            case 0:
                return "A";
            case 1:
                return "B";
            case 2:
                return "C";
            case 3:
                return "D";
            default:
                return "Error";
        }
    }


    private List<String> performOcr(Bitmap preprocessedImage, List<Rect> regions) {
        Log.d("ImageProcessorFragment", "Performing OCR...");

        List<String> results = new ArrayList<>();

        TessBaseAPI tessBaseAPI = new TessBaseAPI();

        // Set the Tesseract data path and language
        String dataPath = requireContext().getFilesDir().getAbsolutePath() + "/tesseract/";
        tessBaseAPI.init(dataPath, "eng");

        for (Rect region : regions) {
            // Crop the region
            Bitmap croppedImage = Bitmap.createBitmap(preprocessedImage, region.x, region.y, region.width, region.height);

            // Set the cropped image
            tessBaseAPI.setImage(croppedImage);

            // Perform OCR
            String ocrResult = tessBaseAPI.getUTF8Text();
            Log.d("ImageProcessorFragment", "OCR Result: " + ocrResult);

            // Add the result to the list
            results.add(ocrResult);
        }
        // Cleanup
        tessBaseAPI.end();
        return results;
    }


    private ExamData extractData(Bitmap preprocessedImage, List<Rect> regions) {
        // Extract Student Name and ID
        Rect studentNameRegion = regions.get(0);
        Rect studentIDRegion = regions.get(1);
        String studentName = performOcrForStudentInfo(preprocessedImage, studentNameRegion, true);
        String studentID = performOcrForStudentInfo(preprocessedImage, studentIDRegion, false);

        // Extract answers
        List<String> answers = new ArrayList<>();
        List<Rect> answerRegions = regions.subList(2, regions.size());
        for (Rect region : answerRegions) {
            String answer = checkFilledAnswerBubble(preprocessedImage, region);
            answers.add(answer);
        }

        ExamData examData = new ExamData(studentName, studentID, answers);

        // Log the extracted data
        Log.d("ImageProcessorFragment", "Student Name: " + studentName);
        Log.d("ImageProcessorFragment", "Student ID: " + studentID);
        Log.d("ImageProcessorFragment", "Answers: " + answers);

        return examData;
    }



    private void copyTessDataToInternalStorage() {
        try {
            String filePath = requireContext().getFilesDir() + "/tesseract/tessdata/";
            File dir = new File(filePath);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.e("ImageProcessorFragment", "Unable to create tessdata directory.");
                    return;
                }
            }

            String tessDataPath = filePath + "eng.traineddata";
            File tessDataFile = new File(tessDataPath);
            if (!tessDataFile.exists()) {
                InputStream is = requireContext().getAssets().open("tessdata/eng.traineddata");
                OutputStream os = new FileOutputStream(tessDataPath);

                byte[] buffer = new byte[1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }

                is.close();
                os.close();
            } else {
                Log.d("ImageProcessorFragment", "Tessdata file already exists.");
            }
        } catch (IOException e) {
            Log.e("ImageProcessorFragment", "Error copying tessdata: " + e.getMessage());
        }
    }
}
