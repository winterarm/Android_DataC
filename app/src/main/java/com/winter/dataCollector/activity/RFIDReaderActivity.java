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
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.core.app.ActivityCompat;

import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.Tag;
import com.winter.dataCollector.R;
import com.winter.dataCollector.impinj.Constants;
import com.winter.dataCollector.impinj.MyReaderMode;
import com.winter.dataCollector.impinj.reader.MyImpinjReader;
import com.winter.dataCollector.impinj.utils.FileWriteUtil;
import com.winter.dataCollector.listener.IMUSensorListener;
import com.winter.dataCollector.vo.TagInfo;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RFIDReaderActivity extends Activity {

    private IMUSensorListener accListener;

    private Button btnCntTest;
    private Button btnForceStop;
    private Button btnWriteEpc;
    private ToggleButton tgRead;

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch port1;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch port2;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch port3;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch port4;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch maskToggle;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch sw_imu;

    private EditText txtHost;
    private EditText maskCode;
    //    private EditText targetEpc;
    private EditText newEpc;
    private EditText et_filename;

    private Spinner tx_power;
    private Spinner rx_power;
    private Spinner reader_mode;
    private TableLayout table_tags;

    private int ports_state = 1;

    private final MyImpinjReader task_rfid = new MyImpinjReader();

    // 用于信息展示的map
    private final Map<String, TagInfo> tagInfoMap = new ConcurrentHashMap<>();

    // 保存标签数据
    private final Map<String, ArrayList<String>> report_infos = new ConcurrentHashMap<>();

    private int tagNumForShow = 0;

    private ClipboardManager cmb;

    //TODO 可能有些线程上的优化需要处理

    private final String saveRoot;
    private Handler handler;

    {
        String date = SDF.format(new Date());
        saveRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SensingData/" + date;
    }


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

        }
        //            Toast.makeText(this, "已授权成功！", Toast.LENGTH_SHORT).show();

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
        checkPermission();
        btnCntTest = findViewById(R.id.btn_CT);
        tgRead = findViewById(R.id.tg_read);
        txtHost = findViewById(R.id.val_rfid_host);
        et_filename = findViewById(R.id.val_filename);
        port1 = findViewById(R.id.sw_ports_1);
        port1.setChecked(true);
        port2 = findViewById(R.id.sw_ports_2);
        port3 = findViewById(R.id.sw_ports_3);
        port4 = findViewById(R.id.sw_ports_4);
        tx_power = findViewById(R.id.spinner_Tx);
        rx_power = findViewById(R.id.spinner_Rx);
        reader_mode = findViewById(R.id.spinner_mode);
        ArrayAdapter<MyReaderMode> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Constants.getModes());
        reader_mode.setAdapter(adapter);
        table_tags = findViewById(R.id.table_recdata);
        btnForceStop = findViewById(R.id.btn_stop);
        maskToggle = findViewById(R.id.sw_mask);
        maskCode = findViewById(R.id.val_epc_mask);
        cmb = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        newEpc = findViewById(R.id.val_epc_new);

        btnWriteEpc = findViewById(R.id.btn_write_epc);
        sw_imu = findViewById(R.id.sw_imu_state);

        SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Get an instance of the PowerManager
//        PowerManager mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);

        // Get an instance of the WindowManager
//        WindowManager mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // 获取当前屏幕的一个信息
//        Display mDisplay = mWindowManager.getDefaultDisplay();

        accListener = new IMUSensorListener(mSensorManager);
        handler = new Handler(Looper.getMainLooper());
        setOnListener();
    }

    private void setOnListener() {

        btnCntTest.setOnClickListener(v -> {
            resetReaderParams(task_rfid);
            if (task_rfid.testConnected()) {
                Toast.makeText(this, "连接成功", Toast.LENGTH_SHORT).show();
                txtHost.setTextColor(getResources().getColor(R.color.green, null));
            } else {
                Toast.makeText(this, "连接失败", Toast.LENGTH_SHORT).show();
                txtHost.setTextColor(getResources().getColor(R.color.red, null));
            }
        });

        port1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) ports_state += 1;
            else ports_state -= 1;
        });
        port2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) ports_state += 2;
            else ports_state -= 2;
        });
        port3.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) ports_state += 4;
            else ports_state -= 4;
        });
        port4.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) ports_state += 8;
            else ports_state -= 8;
        });
        maskToggle.setOnCheckedChangeListener((buttonView, isChecked) -> task_rfid.setOnMaskState(isChecked));

        btnForceStop.setOnClickListener(v -> {
            task_rfid.forceDisConnect();
            tgRead.setSelected(false);
        });
        tgRead.setOnClickListener(v -> {
            Log.d(TAG, "setOnListener: " + v.isSelected());
            if (v.isSelected()) {
                stopSensing();
            } else {
                if (sw_imu.isChecked()) {
                    accListener.startListen();
                }
                resetReaderParams(task_rfid);
                try {
                    task_rfid.startReader(this);
                } catch (OctaneSdkException e) {
                    e.printStackTrace();
                }
            }
            v.setSelected(!v.isSelected());
        });
        btnWriteEpc.setOnClickListener(v -> {
            String mask = maskCode.getText().toString();
            String nEpc = newEpc.getText().toString();
            try {
                task_rfid.rewriteEpc(this, mask, nEpc);
            } catch (OctaneSdkException e) {
                e.printStackTrace();
            }
        });
    }

    private void stopSensing() {
        // 探测可存储的实验编号
        int dirIdx = 1;
        String filename = et_filename.getText().toString();
        if (StringUtils.isNotEmpty(filename)) filename += "_";
        File dir = new File(saveRoot + "/" + filename + dirIdx);
        while (dir.exists()) {
            dirIdx++;
            dir = new File(saveRoot + "/" + filename + dirIdx);
        }

        if (sw_imu.isChecked()) {
            accListener.stopListen();
            Map<String, ArrayList<String>> imu_data = accListener.getData();
            for (Map.Entry<String, ArrayList<String>> entry : imu_data.entrySet()) {
                FileWriteUtil.writefileM(dir.getPath() + "/IMU_data/", entry.getKey(), entry.getValue());
            }
        }
        task_rfid.forceDisConnect();
        for (Map.Entry<String, ArrayList<String>> entry : report_infos.entrySet()) {
            String key = entry.getKey();
            FileWriteUtil.writefileM(dir.getPath() + "/RFID_data/", key.substring(key.length() - 4), entry.getValue());
        }
    }

    public void showRFIDData(List<Tag> tags) {
        // TODO 查看一下阅读器的状态 如果已经停止则不更新表信息
        handler.post(() -> {
//            Log.d(TAG, "showRFIDData: IS IN tagNum is " + tags.size());
            tagNumForShow += tags.size();
            // 统计新到达的标签信息，并对标签数据进行保存
            for (Tag tag : tags) {
                String epc = tag.getEpc().toString();
                if (epc.length() > 29) {
                    // 老的阅读器返回的epc后面有带tid
                    epc = epc.substring(0, 29);
                }
                saveTagInfo(epc, tag);
                TagInfo taginfo;
                if (tagInfoMap.containsKey(epc)) {
                    taginfo = tagInfoMap.get(epc);
                    taginfo.addInfo(tag.getPeakRssiInDbm(), tag.getPhaseAngleInRadians());
                } else {
                    taginfo = new TagInfo(epc);
                    tagInfoMap.put(epc, taginfo);
                    taginfo.addInfo(tag.getPeakRssiInDbm(), tag.getPhaseAngleInRadians());
                }
            }
            // 标签信息展示
            tagNumForShow += tags.size();
            if (tagNumForShow > 49 || !task_rfid.isConnected()) {
                List<TableRow> tableRows = new ArrayList<>();
                List<TagInfo> showedTags = tagInfoMap.values().stream().sorted((o1, o2) -> o2.readiedNum - o1.readiedNum).collect(Collectors.toList());
                for (TagInfo tag : showedTags) {
                    if (!tag.updateInfo()) {
                        // 更新的tag表明需要显示
                        continue;
                    }
                    TableRow tr = new TableRow(this);
                    TextView epc = new TextView(this);
                    epc.setText(tag.Epc);
                    epc.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
                    epc.setOnClickListener(v -> {
                        cmb.clearPrimaryClip();
                        cmb.setPrimaryClip(ClipData.newPlainText(null, tag.Epc));
//                        Toast.makeText(this, "已复制到剪贴板", Toast.LENGTH_SHORT).show();
                        maskCode.setText(tag.Epc);
                    });
                    TextView avg = new TextView(this);
                    avg.setText(String.valueOf(tag.avg_rssi));
                    avg.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                    avg.setWidth(8);
                    avg.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    TextView max = new TextView(this);
                    max.setText(String.valueOf(tag.max_rssi));
                    max.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                    max.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    TextView cnt = new TextView(this);
                    cnt.setText(String.valueOf(tag.lastNum));
                    cnt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                    cnt.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                    tr.addView(epc, 0);
                    tr.addView(cnt, 1);
                    tr.addView(avg, 2);
                    tr.addView(max, 3);
                    tableRows.add(tr);
                }
                if (table_tags.getChildCount() > 1)
                    table_tags.removeViews(1, table_tags.getChildCount() - 1);
                for (TableRow tr : tableRows) {
                    table_tags.addView(tr);
                }
                tagNumForShow = 0;
            }
        });
    }

    private void resetReaderParams(MyImpinjReader task) {
        String hostname = txtHost.getText().toString();
        task.setHostname(hostname);
        task.setPorts(ports_state);
        String txP = tx_power.getSelectedItem().toString();
        String rxP = rx_power.getSelectedItem().toString();
        MyReaderMode mode = (MyReaderMode) reader_mode.getSelectedItem();
        task.setPowerinDbm(Double.parseDouble(txP), Double.parseDouble(rxP));
        task.setReaderMode(mode.getReaderMode());
        task.setTargetMask(maskCode.getText().toString());
        Log.d(TAG, "resetReaderParams: " + hostname + " " + ports_state + " " + txP + " " + rxP + " ");
        tagInfoMap.clear();
        if (table_tags.getChildCount() > 1)
            table_tags.removeViews(1, table_tags.getChildCount() - 1);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void saveRFIDData(String filepath) throws OctaneSdkException {
        // 文件读写 一次性读写 可以优化
//        int dirIdx = 1;
//        File dir = new File(filepath + "/" + dirIdx);
//        while (dir.exists()) {
//            dirIdx++;
//            dir = new File(filepath + "/" + dirIdx);
//        }
        for (Map.Entry<String, ArrayList<String>> entry : report_infos.entrySet()) {
            // 写文件
            String key = entry.getKey();
//            String[] keys = key.split("_");// 多天线代码的处理
//            String fileDir = filepath + "/" + dirIdx;
            FileWriteUtil.writefileM(filepath, key.substring(key.length() - 4), entry.getValue());
        }
    }

    private synchronized void saveTagInfo(String epc, Tag t) {
//        Log.d(TAG, "saveTagInfo: " + epc);
        // TODO 有时间可以改成本地数据库的存储 不知道会不会更快还是更慢
        String val = t.getFirstSeenTime().ToString()
                + "," + t.getAntennaPortNumber()
                + "," + t.getPhaseAngleInRadians()
                + "," + t.getPeakRssiInDbm()
                + "," + t.getRfDopplerFrequency();
//        根据标签EPC存储反射信号信息
        if (report_infos.containsKey(epc)) {
            report_infos.get(epc).add(val);
        } else {
            report_infos.put(epc, new ArrayList<>() {{
                add(val);
            }});
        }
    }

}
