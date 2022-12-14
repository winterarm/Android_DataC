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

import static android.content.ContentValues.TAG;

import static com.winter.dataCollector.AppConstant.SDF;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.winter.dataCollector.R;
import com.winter.dataCollector.impinj.utils.FileWriteUtil;
import com.winter.dataCollector.listener.IMUSensorListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class IMUActivity extends Activity {

    private IMUSensorListener accListener;

    private Button btnStart;

//    private Calendar calendar;
//    private String date;

    private File saveRoot;


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {"android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE","android.permission.MANAGE_EXTERNAL_STORAGE"};

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
//        calendar = Calendar.getInstance();
//        date = calendar.get(Calendar.YEAR) + "年"
//                + (calendar.get(Calendar.MONTH) + 1) + "月"//从0计算
//                + calendar.get(Calendar.DAY_OF_MONTH) + "日";
        String date = SDF.format(new Date());
        saveRoot = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SensingData/" + date);
        if (!saveRoot.exists()) {
            if(!saveRoot.mkdirs()){
                Toast.makeText(this, "创建保存目录失败", Toast.LENGTH_SHORT).show();
            }
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.imu_collector);

        btnStart = (Button) findViewById(R.id.btn_start);
        // Get an instance of the SensorManager
        SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Get an instance of the PowerManager
//        PowerManager mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);

        // Get an instance of the WindowManager
//        WindowManager mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // 获取当前屏幕的一个信息
//        Display mDisplay = mWindowManager.getDefaultDisplay();

        checkPermission();
        accListener = new IMUSensorListener(mSensorManager);
        btnStart.setOnClickListener(v -> {
            Log.d(TAG, "setOnListener: " + v.isSelected());
            if(v.isSelected()) {
                accListener.stopListen();
                saveData();
            } else {
                accListener.startListen();
            }
            v.setSelected(!v.isSelected());
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

    private void saveData() {
        int dirIdx = 1;
        File dir = new File(saveRoot + "/" + dirIdx);
        while (dir.exists()) {
            dirIdx++;
            dir = new File(saveRoot + "/" + dirIdx);
        }
        Map<String, ArrayList<String>> imu_data = accListener.getData();
        for (Map.Entry<String, ArrayList<String>> entry : imu_data.entrySet()) {
            FileWriteUtil.writefileM(dir.getPath() + "/IMU_data/", entry.getKey(), entry.getValue());
        }
    }

    //写数据 存储在本应用的数据文件当中
    public void writeFile(String fileName, String writestr) throws IOException {
        try {

            FileOutputStream fout = openFileOutput(fileName, MODE_PRIVATE);

            byte[] bytes = writestr.getBytes();

            fout.write(bytes);

            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //读数据
    public String readFile(String fileName) throws IOException {
        String res = "";
        try {
            FileInputStream fin = openFileInput(fileName);
            int length = fin.available();
            byte[] buffer = new byte[length];
            fin.read(buffer);
            res = new String(buffer, StandardCharsets.UTF_8);
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
}
