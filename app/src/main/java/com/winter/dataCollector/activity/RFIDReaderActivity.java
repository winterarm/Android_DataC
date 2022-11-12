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

import static com.winter.dataCollector.AppConstant.SDF;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.impinj.octane.OctaneSdkException;
import com.winter.dataCollector.R;
import com.winter.dataCollector.impinj.reader.MyImpinjReader;

import java.util.Date;

public class RFIDReaderActivity extends Activity {

    private Button btnStart;
    private Button btnEnd;


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {"android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    private void checkPermission() {
        //检查权限（NEED_PERMISSION）是否被授权 PackageManager.PERMISSION_GRANTED表示同意授权
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                    .WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "请开通相关权限，否则无法正常使用本应用！", Toast.LENGTH_SHORT).show();
            }
            //申请权限
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);

        } else {
            Toast.makeText(this, "已授权成功！", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.rfid);

        btnStart = findViewById(R.id.btn_start);
        btnEnd = findViewById(R.id.btn_end);

        checkPermission();
        String date = SDF.format(new Date());

        btnStart.setOnClickListener(v -> {
            try {
                short[] ports = new short[]{1};
                String saveRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + "/RFID_data/" + date;
                MyImpinjReader task = new MyImpinjReader("192.168.1.101", ports);
                task.startReader();
                // TODO 控制数据读取的时间
                Thread.sleep(5000);
                task.stopReaderAndSaveData(saveRoot);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (OctaneSdkException e) {
                e.printStackTrace();
            }
        });
        btnEnd.setOnClickListener(v -> {

        });


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
