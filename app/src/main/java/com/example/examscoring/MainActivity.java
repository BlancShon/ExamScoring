package com.example.examscoring;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.content.CursorLoader;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.examscoring.Fragments.CameraFragment;
import com.example.examscoring.Fragments.ImageProcessorFragment;
import com.example.examscoring.Fragments.SetAnswersFragment;
import com.example.examscoring.ViewModel.SharedViewModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button buttonCaptureImage, buttonImportImage, buttonSetAnswers;
    private static final int PICK_IMAGE_REQUEST_CODE = 100;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private List<String> mSavedAnswers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedViewModel sharedViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(SharedViewModel.class);


        buttonCaptureImage = findViewById(R.id.button_capture_image);
        buttonImportImage = findViewById(R.id.button_import_image);
        buttonSetAnswers = findViewById(R.id.button_set_answers);

        buttonCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraFragment cameraFragment = new CameraFragment();
                loadFragment(cameraFragment);
            }
        });

        buttonImportImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_WRITE_EXTERNAL_STORAGE);
                } else {
                    launchImagePicker();
                }
            }
        });

        buttonSetAnswers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetAnswersFragment setAnswersFragment = new SetAnswersFragment();
                loadFragment(setAnswersFragment);
            }
        });
    }

    private void launchImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // Added super call
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                launchImagePicker();
            } else {
                // Permission denied
                Toast.makeText(this, "Permission to access external storage is required.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle result from ScoringActivity
        if (requestCode == 1) {
            showButtons();
        }

        // Handle result from image picker
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            String imagePath = getPathFromUri(selectedImageUri);
            ImageProcessorFragment imageProcessorFragment = ImageProcessorFragment.newInstance(imagePath);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, imageProcessorFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }


    private String getPathFromUri(Uri contentUri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(getApplicationContext(), contentUri, projection, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            setButtonVisibility(true);
        } else {
            super.onBackPressed();
        }
    }


    private void loadFragment(androidx.fragment.app.Fragment fragment) {
        setButtonVisibility(false);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void showButtons() {
        buttonCaptureImage.setVisibility(View.VISIBLE);
        buttonImportImage.setVisibility(View.VISIBLE);
        buttonSetAnswers.setVisibility(View.VISIBLE);
    }

    public void setButtonVisibility(boolean show) {
        // Replace these with the IDs of your buttons
        int[] buttonIds = {R.id.button_capture_image, R.id.button_import_image, R.id.button_set_answers};

        for (int buttonId : buttonIds) {
            View button = findViewById(buttonId);
            if (button != null) {
                button.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        }
    }
}

