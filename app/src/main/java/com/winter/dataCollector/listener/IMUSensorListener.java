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

public class IMUSensorListener implements SensorEventListener {

    private final Sensor sensorla;
    private final Sensor sensora;
    private final Sensor sensorg;
    private final Sensor sensorm;
    private final SensorManager sensorManager;

    private List<String> data;

    public void startListen() {
        sensorManager.registerListener(this, sensorla, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, sensora, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, sensorg, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, sensorm, SensorManager.SENSOR_DELAY_GAME);
        data = new ArrayList<>();
    }

    public void stopListen() {
        sensorManager.unregisterListener(this);
    }

    public IMUSensorListener(SensorManager mSensorManager) {
        this.sensorManager = mSensorManager;
        sensorla = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensora = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorg = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorm = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

//        BitmapFactory.Options opts = new BitmapFactory.Options();
//        opts.inPreferredConfig = Bitmap.Config.RGB_565;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long timestamp = System.currentTimeMillis();// 毫秒级别
        float mSensorX = event.values[0];
        float mSensorY = event.values[1];
        float mSensorZ = event.values[2];
        String metadata = timestamp + "," + event.sensor.getType() + "," + mSensorX + ", " + mSensorY + ", " + mSensorZ;
//        Log.d(TAG, "onSensorChanged:[" + metadata + " ]");
        data.add(metadata + "\n");
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public List<String> getData() {
        return data;
    }
}