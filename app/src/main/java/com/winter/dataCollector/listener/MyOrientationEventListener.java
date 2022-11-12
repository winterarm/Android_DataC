package com.winter.dataCollector.listener;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.view.OrientationEventListener;

public class MyOrientationEventListener extends OrientationEventListener {

    private final Activity context;

    public MyOrientationEventListener(Context context) {
        super(context);
        this.context = (Activity) context;
    }

    @Override
    public void onOrientationChanged(int orientation) {
//                Log.d(TAG, "onOrientationChanged: " + orientation);
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
            return; // 手机平放时，检测不到有效的角度
        }
        // 只检测是否有四个角度的改变
        if (orientation > 350 || orientation < 10) {
            // 0度：手机默认竖屏状态（home键在正下方）
//                    Log.d(TAG, "下");
            context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        } else if (orientation > 80 && orientation < 100) {
            // 90度：手机顺时针旋转90度横屏（home建在左侧）
//                    Log.d(TAG, "左");
            context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if (orientation > 170 && orientation < 190) {
            // 180度：手机顺时针旋转180度竖屏（home键在上方）
//                    Log.d(TAG, "上");
            context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        } else if (orientation > 260 && orientation < 280) {
            // 270度：手机顺时针旋转270度横屏，（home键在右侧）
//                    Log.d(TAG, "右");
            context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        }
    }
}
