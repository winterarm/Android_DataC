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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mOrientationListener = new MyOrientationEventListener(this);
    }

    public void dataCollectorOfIMU(View view){

        System.out.println("START IMU data collecting");
        Intent intent = new Intent(this, AccelerometerActivity.class);
        mOrientationListener.disable();
        startActivity(intent);

    }

    public void dataCollectorOfRFID(View view){

        System.out.println("START RFID data collecting");
        Intent intent = new Intent(this, RFIDReaderActivity.class);
        mOrientationListener.disable();
        startActivity(intent);

    }


}