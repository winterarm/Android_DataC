package com.winter.dataCollector.listener;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

public class AndroidSensorManager {

    private SensorManager sensorManager;


    protected void onCreate(Bundle savedInstanceState) {

        //sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensora = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(listenera, sensora, SensorManager.SENSOR_DELAY_GAME);
        Sensor sensorg = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(listenerg, sensorg, SensorManager.SENSOR_DELAY_GAME);
        Sensor sensorm = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(listenerm, sensorm, SensorManager.SENSOR_DELAY_GAME);
    }

    private final SensorEventListener listenera = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float accx = event.values[0];
            float accy = event.values[1];
            float accz = event.values[2];
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private final SensorEventListener listenerg = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float gyrox = event.values[0];
            float gyroy = event.values[1];
            float gyroz = event.values[2];
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private final SensorEventListener listenerm = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float magx = event.values[0];
            float magy = event.values[1];
            float magz = event.values[2];
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    protected void onDestroy() {
//        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(listenera);
            sensorManager.unregisterListener(listenerg);
            sensorManager.unregisterListener(listenerm);
        }
    }

}
