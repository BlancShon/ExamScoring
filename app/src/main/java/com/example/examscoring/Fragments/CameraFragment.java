package com.example.examscoring.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.example.examscoring.MainActivity;
import com.example.examscoring.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraFragment extends Fragment implements DefaultLifecycleObserver {

    private String currentPhotoPath;
    private boolean isCameraActive = false;
    private boolean hasCapturedImage = false;


    private void updateButtonVisibility(boolean show) {
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.setButtonVisibility(show);
        }
    }

    // Add the missing requestCameraPermissionLauncher variable
    private final ActivityResultLauncher<String[]> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (result.get(Manifest.permission.CAMERA)) {
                    Log.d("CameraFragment", "Camera permission granted");
                    captureImage();
                } else {
                    Log.d("CameraFragment", "Camera permission denied");
                }
            });

    private final ActivityResultLauncher<Intent> captureImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.d("CameraFragment", "Image capture successful");
                    hasCapturedImage = true;
                    ImageProcessorFragment imageProcessorFragment = ImageProcessorFragment.newInstance(currentPhotoPath);
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.container, imageProcessorFragment)
                            .addToBackStack(null)
                            .commit();
                } else {
                    Log.d("CameraFragment", "Image capture failed, resultCode: " + result.getResultCode());
                }
            });

    public CameraFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getViewLifecycleOwner().getLifecycle().addObserver(this);
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        Log.d("CameraFragment", "onStart");
        isCameraActive = true;
        updateButtonVisibility(false);
        if (!hasCapturedImage) {
            startCamera();
        }
    }

    public void startCamera() {
        hasCapturedImage = false;
        captureImage();
    }


    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        Log.d("CameraFragment", "onStop");
        isCameraActive = false;
        updateButtonVisibility(true);
    }

    private void captureImage() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Log.d("CameraFragment", "Camera permission already granted");

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                File photoFile;
                try {
                    photoFile = createImageFile();
                } catch (IOException e) {
                    Log.e("CameraFragment", "Error creating image file", e);
                    return;
                }

                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(requireContext(),
                            "com.example.examscoring.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    captureImageLauncher.launch(takePictureIntent);
                } else {
                    Log.d("CameraFragment", "photoFile is null");
                }
            } else {
                Log.d("CameraFragment", "No activity found to handle intent");
            }
        } else {
            Log.d("CameraFragment", "Requesting camera permission");
            requestCameraPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA});
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PNG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(null);
        File image = File.createTempFile(
                imageFileName,
                ".png",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
}
