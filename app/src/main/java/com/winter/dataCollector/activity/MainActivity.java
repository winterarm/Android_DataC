package com.winter.dataCollector.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.winter.dataCollector.R;
import com.winter.dataCollector.listener.MyOrientationEventListener;


public class MainActivity extends AppCompatActivity {

    private OrientationEventListener mOrientationListener;

    Button getin_imuc;
    Button getin_rfid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        getin_imuc = findViewById(R.id.btn_imu);
        getin_rfid = findViewById(R.id.btn_rfid);

        bindListener();
    }

    private void bindListener() {
        mOrientationListener = new MyOrientationEventListener(this);
        getin_imuc.setOnClickListener(v -> {
            Intent intent = new Intent(this, IMUActivity.class);
            startActivity(intent);
        });
        getin_rfid.setOnClickListener(v -> {
            Intent intent = new Intent(this, RFIDReaderActivity.class);
            mOrientationListener.disable();
            startActivity(intent);
        });
    }

}