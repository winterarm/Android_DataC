/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.winter.dataCollector.activity;

import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.common.util.concurrent.ListenableFuture;
import com.winter.dataCollector.R;

import java.io.File;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    boolean defCamera;
    PreviewView previewView;
    ImageView imageHolder;
    Preview preview;
    ImageButton capture, switchCamera;
    CameraSelector cameraSelector;
    ProcessCameraProvider cameraProvider;
    private final int REQUEST_CODE_PERMISSIONS = 100;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageCapture imageCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_demo);
        defCamera = true;

        previewView = findViewById(R.id.previewView);
        imageHolder = findViewById(R.id.imageView);
        capture = findViewById(R.id.camera_btn);
        switchCamera = findViewById(R.id.switch_camera_btn);

        imageHolder.setOnClickListener(this);
        capture.setOnClickListener(this);
        switchCamera.setOnClickListener(this);

        if (allPermissionsGranted()) {
            startCameraX();
        } else {
            ActivityCompat.requestPermissions(this,
                    REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

    }// end of onCreate


    private void startCameraX() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                bindPreview(cameraProvider);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));


    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        // 绑定预览窗
        cameraProvider.unbindAll();

        //Selector Use case
        if (defCamera) {
            cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build();
        } else {
            cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build();
        }

        //Preview Use case
        preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        //ImageCapture use case
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();
        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
        // For performing operations that affect all outputs.
//        CameraControl cameraControl = camera.getCameraControl();
        // For querying information and states.
//        CameraInfo cameraInfo = camera.getCameraInfo();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageView:
                // do nothing for now
                break;
            case R.id.camera_btn:
                for (int i = 0; i < 1; i++) {
                    capturePhoto();
                }
                break;
            case R.id.switch_camera_btn:
                defCamera = !defCamera;
                startCameraX();
                break;
        }

    }

    private void capturePhoto() {
        //TODO 照片名字
        SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.CHINA);
        File file = new File(getBatchDirectoryName(), mDateFormat.format(new Date()) + ".jpg");

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();

        imageCapture.takePicture(outputFileOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {

                        Uri picUri = outputFileResults.getSavedUri();
                        if (picUri == null) {
                            Toast.makeText(CameraActivity.this, "Uri is Empty, Image might be saved", Toast.LENGTH_SHORT).show();
                        } else
                            Glide.with(getBaseContext())
                                    .load(picUri)
                                    .into(imageHolder);
                        Toast.makeText(CameraActivity.this, "Image saved at " + picUri.getPath() + " Uri is not Empty, saved", Toast.LENGTH_SHORT).show();

                    }


                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        exception.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(CameraActivity.this, "Image Capture error", Toast.LENGTH_SHORT).show());

                    }
                });

    }

    public String getBatchDirectoryName() {

        String app_folder_path = Environment.getExternalStorageDirectory().toString() + "/CameraX";
        File dir = new File(app_folder_path);
        if (!dir.exists() && !dir.mkdirs()) {
        }
        return app_folder_path;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //start camera when permissions have been granted otherwise exit app
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCameraX();
            } else {
                Toast.makeText(CameraActivity.this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean allPermissionsGranted() {
        //check if req permissions have been granted
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(CameraActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
