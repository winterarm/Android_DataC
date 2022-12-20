package com.winter.dataCollector.listener;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IMUSensorListener implements SensorEventListener {

    private final Sensor sensorla;
    private final Sensor sensora;
    private final Sensor sensorg;
    private final Sensor sensorm;
    private final SensorManager sensorManager;

    private List<String> data;

    private Map<String, ArrayList<String>> imu_data;

    public void startListen() {
        data = new ArrayList<>();
        imu_data = new HashMap<>();

        imu_data.put("ACCELEROMETER", new ArrayList<>());
        imu_data.put("GYROSCOPE", new ArrayList<>());
        imu_data.put("MAGNETIC_FIELD", new ArrayList<>());
        imu_data.put("LINEAR_ACCELERATION", new ArrayList<>());

        sensorManager.registerListener(this, sensorla, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, sensora, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, sensorg, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, sensorm, SensorManager.SENSOR_DELAY_GAME);
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
//        sensorm = sensorManager.getDefaultSensor(Sensor.TYPE_POSE_6DOF);

//        BitmapFactory.Options opts = new BitmapFactory.Options();
//        opts.inPreferredConfig = Bitmap.Config.RGB_565;
    }

    // 加速度传感器数据
//    float mAccValues[] = new float[3];
    // 地磁传感器数据
//    float mMagValues[] = new float[3];
    // 旋转矩阵，用来保存磁场和加速度的数据
//    float mRMatrix[] = new float[9];
    // 存储方向传感器的数据（原始数据为弧度）
//    float mPhoneAngleValues[] = new float[3];

    @Override
    public void onSensorChanged(SensorEvent event) {
        long timestamp = event.timestamp;// 毫秒级别
        SystemClock.elapsedRealtime();
        float mSensorX = event.values[0];
        float mSensorY = event.values[1];
        float mSensorZ = event.values[2];

        String metadata = timestamp + "," + mSensorX + ", " + mSensorY + ", " + mSensorZ;
//        Log.d(TAG, "onSensorChanged:[" + metadata + " ]");
        data.add(event.sensor.getType() + "," + metadata + "\n");


        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
//                System.arraycopy(event.values, 0, mAccValues, 0, mAccValues.length);// 获取数据
                imu_data.get("ACCELEROMETER").add(metadata);
                break;
            case Sensor.TYPE_GYROSCOPE:
                imu_data.get("GYROSCOPE").add(metadata);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
//                System.arraycopy(event.values, 0, mMagValues, 0, mMagValues.length);// 获取数据
                imu_data.get("MAGNETIC_FIELD").add(metadata);
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
//                System.arraycopy(event.values, 0, mMagValues, 0, mMagValues.length);// 获取数据
                imu_data.get("LINEAR_ACCELERATION").add(metadata);
                break;
        }
//        SensorManager.getRotationMatrix(mRMatrix, null, mAccValues, mMagValues);
//        SensorManager.getOrientation(mRMatrix, mPhoneAngleValues);// 此时获取到了手机的角度信息
//        mPhoneAzTv.setText(String.format(Locale.CHINA, "Azimuth(地平经度): %f", Math.toDegrees(mPhoneAngleValues[0])));
//        mPhonePitchTv.setText(String.format(Locale.CHINA, "Pitch: %f", Math.toDegrees(mPhoneAngleValues[1])));
//        mPhoneRollTv.setText(String.format(Locale.CHINA, "Roll: %f", Math.toDegrees(mPhoneAngleValues[2])));

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public Map<String, ArrayList<String>> getData() {
        return imu_data;
    }
    public List<String> getAllData() {
        return data;
    }

}