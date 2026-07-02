// L23-0717 -Haziq Bin Zargham
// L23-0808 -Hannan Asad
package com.example.rearcameracollision;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.media.MediaPlayer;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_CODE = 100;

    private PreviewView previewView;
    private TextView distanceText;
    private TextView warningText;
    private ProgressBar distanceProgress;
    private ObjectDetector objectDetector;
    private ExecutorService cameraExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //views
        previewView = findViewById(R.id.previewView);
        distanceText = findViewById(R.id.distanceText);
        warningText = findViewById(R.id.warningText);
        distanceProgress = findViewById(R.id.distanceProgress);

        //Overlay detected object bounding box
        boundingBoxOverlay = findViewById(R.id.boundingBoxOverlay);

        //Initialize object detector
        ObjectDetectorOptions options = new ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build();
        objectDetector = ObjectDetection.getClient(options);

        //Initialize camera executor
        cameraExecutor = Executors.newSingleThreadExecutor();

        //cCamera permission check
        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this,
            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                distanceText.setText("Camera permission required");
            }
        }
    }

    private int imageWidth = 0;
    private int imageHeight = 0;
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
        ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
        try {
            ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

            //Set up preview
            Preview preview = new Preview.Builder().build();
            preview.setSurfaceProvider(previewView.getSurfaceProvider());

            //Set up image analysis for object detection
            ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

            imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
                @SuppressWarnings("UnsafeOptInUsageError")
                Image mediaImage = imageProxy.getImage();
                if (mediaImage != null) {
                    InputImage image = InputImage.fromMediaImage(
                            mediaImage, imageProxy.getImageInfo().getRotationDegrees());

                    // For Bounding Box UI Overlay
                    imageWidth = imageProxy.getWidth();
                    imageHeight = imageProxy.getHeight();

                    // Detect objects
                    objectDetector.process(image)
                        .addOnSuccessListener(detectedObjects -> {
                        processDetectedObjects(detectedObjects);
                    })
                    .addOnFailureListener(e -> {
                        runOnUiThread(() ->
                        distanceText.setText("Detection error"));
                    })
                    .addOnCompleteListener(task -> imageProxy.close());
                }
            });

            // Select back camera
            CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

            // Unbind before binding
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector,
                preview, imageAnalysis);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }, ContextCompat.getMainExecutor(this));
    }

    //for 0 object detected problem
    private float lastValidDistance = 10.0f;
    private BoundBoxUIOverlay boundingBoxOverlay;

    //For audio
    private MediaPlayer warningPlayer;
    private boolean isWarningPlaying = false;

    private void processDetectedObjects(List<DetectedObject> objects) {
        if (objects.isEmpty()) {
            updateUI(lastValidDistance);
            return;
        }

        // Find closest object (assuming it's the largest/most prominent)
        DetectedObject closestObject = objects.get(0);
        float maxArea = 0;

        for (DetectedObject obj : objects) {

            float area =
                    obj.getBoundingBox().width() *
                            obj.getBoundingBox().height();

            if (area > maxArea) {
                maxArea = area;
                closestObject = obj;
            }
        }

        DetectedObject finalClosestObject = closestObject;

        runOnUiThread(() -> {
            boundingBoxOverlay.setBoundingBox(
                    finalClosestObject.getBoundingBox(),
                    imageWidth,
                    imageHeight
            );
        });

        // Estimate distance based on bounding box size
        // This is a simplified estimation - larger box = closer object
        float boundingBoxHeight = closestObject.getBoundingBox().height();
        float screenHeight = previewView.getHeight();
        float heightRatio = boundingBoxHeight / screenHeight;

        // Convert ratio to approximate distance in meters
        float estimatedDistance = 0.23f / Math.max(heightRatio, 0.01f);
        estimatedDistance = Math.min(estimatedDistance, 10.0f);
        estimatedDistance = Math.round(estimatedDistance * 10) / 10.0f;
        lastValidDistance = estimatedDistance;

        updateUI(estimatedDistance);
    }

    private void updateUI(float distance) {
        runOnUiThread(() -> {
        distanceText.setText(String.format("%.1f m", distance));

        // Warning audio pause
        if (distance >= 0.8 && isWarningPlaying && warningPlayer != null) {
            warningPlayer.pause();
            warningPlayer.seekTo(0);
            isWarningPlaying = false;
        }

        // Update progress bar (max = 10 meters)
        int progress = (int) ((10 - distance) * 10);
        distanceProgress.setProgress(Math.min(progress, 100));
        warningPlayer = MediaPlayer.create(this, R.raw.warning_beep);

        // Update warning based on distance zones
        if (distance < 0.8) {
            warningText.setText("DANGER - STOP!");
            warningText.setTextColor(Color.RED);
            distanceProgress.setProgressTintList(
                ColorStateList.valueOf(Color.RED));

            //Warning audio play
            if (!isWarningPlaying && warningPlayer != null) {
                isWarningPlaying = true;
                warningPlayer.start();

                warningPlayer.setOnCompletionListener(mp -> {
                    isWarningPlaying = false;
                });
            }
        } else if (distance < 1.5) {
            if (isWarningPlaying && warningPlayer != null) {
                warningPlayer.pause();
                warningPlayer.seekTo(0);
                isWarningPlaying = false;}
            warningText.setText("WARNING - Close");
            warningText.setTextColor(Color.parseColor("#FFA500"));
            distanceProgress.setProgressTintList(
                ColorStateList.valueOf(Color.parseColor("#FFA500")));
        } else if (distance < 2.0) {
            warningText.setText("Caution");
            warningText.setTextColor(Color.YELLOW);
            distanceProgress.setProgressTintList(
                ColorStateList.valueOf(Color.YELLOW));
        } else {
            warningText.setText("Safe");
            warningText.setTextColor(Color.GREEN);
            distanceProgress.setProgressTintList(
                ColorStateList.valueOf(Color.GREEN));
        }
    });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (warningPlayer != null) {
            warningPlayer.release();
            warningPlayer = null;
        }
        cameraExecutor.shutdown();
        if (objectDetector != null) {
            objectDetector.close();
        }
    }
}