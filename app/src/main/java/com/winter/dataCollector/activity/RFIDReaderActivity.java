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
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.widget.SwitchCompat;
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
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RFIDReaderActivity extends Activity {

    private IMUSensorListener accListener;

    private Button btnCntTest;
    private Button btnForceStop;
    private Button btnWriteEpc;
    private ToggleButton tgRead;

    private SwitchCompat port1;
    private SwitchCompat port2;
    private SwitchCompat port3;
    private SwitchCompat port4;
    private SwitchCompat maskToggle;
    private SwitchCompat sw_imu;
    private SwitchCompat sw_clock;
    private SwitchCompat sw_save;

    private EditText txtHost;
    private EditText maskCode;
    //    private EditText targetEpc;
    private EditText newEpc;
    private EditText val_clock;
    private EditText val_delay;
    private EditText val_filedir;

    private Spinner tx_power;
    private Spinner rx_power;
    private Spinner reader_mode;
    private TableLayout table_tags;

    private int ports_state = 1;

    private final MyImpinjReader task_rfid = new MyImpinjReader();

    // ?????????????????????map
    private final Map<String, TagInfo> tagInfoMap = new ConcurrentHashMap<>();

    // ??????????????????
    private final Map<String, ArrayList<String>> report_info = new ConcurrentHashMap<>();

    private int tagNumForShow = 0;

    private boolean clock_mode = false;
    private boolean data_save = true;
    private boolean running = false;

    private ClipboardManager cmb;

    private final String saveRoot;
    private Handler handler;
    private Runnable state_update_task;

    {
        String date = SDF.format(new Date());
        saveRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SensingData/" + date;

    }


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {"android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    private void checkPermission() {
        //???????????????NEED_PERMISSION?????????????????? PackageManager.PERMISSION_GRANTED??????????????????
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                    .WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "????????????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
            }
            //????????????
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);

        }
        //            Toast.makeText(this, "??????????????????", Toast.LENGTH_SHORT).show();

    }


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO ?????????????????????????????? ?????????????????????
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.rfid);
        checkPermission();
        btnCntTest = findViewById(R.id.btn_CT);
        tgRead = findViewById(R.id.tg_read);
        txtHost = findViewById(R.id.val_rfid_host);
        val_filedir = findViewById(R.id.val_filedir);
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
        sw_save = findViewById(R.id.sw_save);
        sw_clock = findViewById(R.id.sw_clock);
        val_clock = findViewById(R.id.val_clock);
        val_delay = findViewById(R.id.val_delay);

        SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Get an instance of the PowerManager
//        PowerManager mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);

        // Get an instance of the WindowManager
//        WindowManager mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // ?????????????????????????????????
//        Display mDisplay = mWindowManager.getDefaultDisplay();

        accListener = new IMUSensorListener(mSensorManager);
        handler = new Handler(Looper.getMainLooper());
        setOnListener();
        state_update_task = new Runnable() {
            @Override
            public void run() {
                if (running) {
                    txtHost.setTextColor(getResources().getColor(R.color.yellow, null));
                } else {
                    txtHost.setTextColor(getResources().getColor(R.color.black, null));
                }
                handler.postDelayed(this, 500);
            }
        };
    }

    /**
     * ????????????????????? ?????? ?????? ?????? ?????????????????????????????? ?????????????????????
     * @param task
     */
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
        report_info.clear();
        if (table_tags.getChildCount() > 1)
            table_tags.removeViews(1, table_tags.getChildCount() - 1);
    }

    /**
     * ?????????????????????
     */
    private void setOnListener() {
        btnCntTest.setOnClickListener(v -> {
            resetReaderParams(task_rfid);
            if (task_rfid.testConnected()) {
                Toast.makeText(this, "????????????", Toast.LENGTH_SHORT).show();
                txtHost.setTextColor(getResources().getColor(R.color.green, null));
            } else {
                Toast.makeText(this, "????????????", Toast.LENGTH_SHORT).show();
                txtHost.setTextColor(getResources().getColor(R.color.red, null));
            }
        });

        port1.setOnCheckedChangeListener((buttonView, isChecked) -> ports_state ^= 1);
        port2.setOnCheckedChangeListener((buttonView, isChecked) -> ports_state ^= 2);
        port3.setOnCheckedChangeListener((buttonView, isChecked) -> ports_state ^= 4);
        port4.setOnCheckedChangeListener((buttonView, isChecked) -> ports_state ^= 8);
        maskToggle.setOnCheckedChangeListener((buttonView, isChecked) -> task_rfid.setOnMaskState(isChecked));

        btnForceStop.setOnClickListener(v -> {
            task_rfid.forceDisConnect();
            tgRead.setSelected(false);
        });
        tgRead.setOnClickListener(v -> {
            if (v.isSelected()) {
                stopAndSaveData();
                v.setSelected(false);
            } else {
                running = startListening();
                v.setSelected(true);
            }
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
        sw_clock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            clock_mode = isChecked;
            Log.d(TAG, "CLock Mode is " + (clock_mode ? "on" : "off"));
        });
        sw_save.setChecked(true);
        sw_save.setOnCheckedChangeListener((buttonView, isChecked) -> data_save = isChecked);
    }

    private boolean startListening() {
        // TODO ??????????????????????????????????????????
        resetReaderParams(task_rfid);
        try {
            // ???????????????
            if (clock_mode) {
                long delay = 0L;
                long duration = (long) (Double.parseDouble(val_clock.getText().toString()) * 1000L);
                if (val_delay.getText().length() > 0) {
                    delay = (long) Double.parseDouble(val_delay.getText().toString()) * 1000L;
                }
                handler.postDelayed(() -> {
                    try {
                        if (sw_imu.isChecked()) {
                            accListener.startListen();
                        }
                        task_rfid.startReader(this);
                        tgRead.setText("??????");
                    } catch (OctaneSdkException e) {
                        e.printStackTrace();
                    }
                }, delay);
                handler.postDelayed(() -> {
                    stopAndSaveData();
                    tgRead.setText("??????");
                    tgRead.setSelected(false);
                }, duration + delay);
            } else {
                if (sw_imu.isChecked()) {
                    accListener.startListen();
                }
                task_rfid.startReader(this);
            }
        } catch (OctaneSdkException e) {
            e.printStackTrace();
        }
        handler.postDelayed(state_update_task, 1000);
        return true;
    }

    public void showRFIDData(List<Tag> tags) {
        // ?????????????????????????????? ???????????????????????????????????????
        if (!running) return;
        handler.post(() -> {
//            Log.d(TAG, "showRFIDData: IS IN tagNum is " + tags.size());
            tagNumForShow += tags.size();
            // ???????????????????????????????????????????????????????????????
            for (Tag tag : tags) {
                String epc = tag.getEpc().toString();
                if (epc.length() > 29) {
                    // ????????????????????????epc????????????tid
                    epc = epc.substring(0, 29);
                }
                saveTagInfo(epc, tag);//??????????????????
                TagInfo taginfo;
                if (tagInfoMap.containsKey(epc)) {
                    taginfo = tagInfoMap.get(epc);
                    assert taginfo != null;
                    taginfo.addInfo(tag.getPeakRssiInDbm(), tag.getPhaseAngleInRadians());
                } else {
                    taginfo = new TagInfo(epc);
                    tagInfoMap.put(epc, taginfo);
                    taginfo.addInfo(tag.getPeakRssiInDbm(), tag.getPhaseAngleInRadians());
                }
            }
            // ??????????????????
            tagNumForShow += tags.size();
            if (tagNumForShow > 49 || !task_rfid.isConnected()) {
                List<TableRow> tableRows = new ArrayList<>();
                List<TagInfo> showedTags = tagInfoMap.values().stream().sorted((o1, o2) -> o2.readiedNum - o1.readiedNum).collect(Collectors.toList());
                for (TagInfo tag : showedTags) {
                    if (!tag.updateInfo()) {
                        // ?????????tag??????????????????
                        continue;
                    }
                    TableRow tr = new TableRow(this);
                    TextView epc = new TextView(this);
                    epc.setText(tag.Epc);
                    epc.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
                    epc.setOnClickListener(v -> {
                        cmb.clearPrimaryClip();
                        cmb.setPrimaryClip(ClipData.newPlainText(null, tag.Epc));
//                        Toast.makeText(this, "?????????????????????", Toast.LENGTH_SHORT).show();
                        maskCode.setText(tag.Epc);
                    });
                    TextView last = new TextView(this);
                    last.setText(String.valueOf(tag.rssi_last));
                    last.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                    last.setWidth(8);
                    TextView avg = new TextView(this);
                    avg.setText(String.valueOf(tag.rssi_avg));
                    avg.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                    avg.setWidth(8);
                    avg.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    TextView max = new TextView(this);
                    max.setText(String.valueOf(tag.rssi_max));
                    max.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                    max.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    TextView cnt = new TextView(this);
                    cnt.setText(String.valueOf(tag.lastNum));
                    cnt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                    cnt.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                    tr.addView(epc, 0);
                    tr.addView(cnt, 1);
                    tr.addView(last, 2);
                    tr.addView(avg, 3);
                    tr.addView(max, 4);
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

    // TODO ???????????????????????????
    private void stopAndSaveData() {
        if(running) {
            // ??????????????????????????????
            running = false;
            accListener.stopListen();
            task_rfid.forceDisConnect();
            handler.removeCallbacks(state_update_task);
            txtHost.setTextColor(getResources().getColor(R.color.black, null));

            if (data_save) saveDataByCsvFile();
        }
    }

    /**
     * ????????????????????????????????????CSV????????????
     */
    private void saveDataByCsvFile() {
        int dirIdx = 1;
        String filedir = val_filedir.getText().toString();
        String dirstr = saveRoot;
        if (StringUtils.isNotEmpty(filedir)) dirstr += "/" + filedir;
        File dir = new File(dirstr + "/" + dirIdx);
        while (dir.exists()) {
            dirIdx++;
            dir = new File(dirstr + "/" + dirIdx);
        }
        if (sw_imu.isChecked()) {
            Map<String, ArrayList<String>> imu_data = accListener.getData();
            for (Map.Entry<String, ArrayList<String>> entry : imu_data.entrySet()) {
                FileWriteUtil.writefileM(dir.getPath() + "/IMU_data/", entry.getKey(), entry.getValue());
            }
        }
        for (Map.Entry<String, ArrayList<String>> entry : report_info.entrySet()) {
            String key = entry.getKey();
            FileWriteUtil.writefileM(dir.getPath() + "/RFID_data/", key.substring(key.length() - 4), entry.getValue());
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private synchronized void saveTagInfo(String epc, Tag t) {
//        Log.d(TAG, "saveTagInfo: " + epc);
        // TODO ????????????????????????????????????????????? ????????????????????????????????????
        String val = t.getFirstSeenTime().ToString()
                + "," + t.getAntennaPortNumber()
                + "," + t.getPhaseAngleInRadians()
                + "," + t.getPeakRssiInDbm()
                + "," + t.getRfDopplerFrequency();
//        ????????????EPC????????????????????????
        if (report_info.containsKey(epc)) {
            Objects.requireNonNull(report_info.get(epc)).add(val);
        } else {
            report_info.put(epc, new ArrayList<>() {{
                add(val);
            }});
        }
    }

    public void EPCWriteComplete() {
        //TODO ??????????????? ???????????? ????????????
        handler.post(() -> {
            task_rfid.forceDisConnect();
            Toast.makeText(this, "EPC Write Success!", Toast.LENGTH_SHORT).show();
        });
    }
}
