package com.winter.dataCollector.listener;

import static android.content.ContentValues.TAG;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class AccelerometerListener implements SensorEventListener {

    private final Sensor mAccelerometer;
    private final SensorManager mSensorManager;

    private List<String> data;

    public void startListen() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        data = new ArrayList<>();
    }

    public void stopListen() {
        mSensorManager.unregisterListener(this);
    }

    public AccelerometerListener(SensorManager mSensorManager) {
        this.mSensorManager = mSensorManager;
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long timestamp = System.currentTimeMillis();
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            float mSensorX = event.values[0];
            float mSensorY = event.values[1];
            float mSensorZ = event.values[2];
            Log.d(TAG, "onSensorChanged:[" + mSensorX + " , " + mSensorY + " , " + mSensorZ + " ]");
            data.add(timestamp + "," + mSensorX + ", " + mSensorY + ", " + mSensorZ + "\n");
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public List<String> getData() {
        return data;
    }
}