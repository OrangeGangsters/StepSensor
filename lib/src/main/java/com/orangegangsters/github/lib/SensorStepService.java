package com.orangegangsters.github.lib;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by oliviergoutay on 2/4/15.
 */
public class SensorStepService extends Service implements SensorEventListener {

    private static final String TAG = "SensorStepService";

    private Context mContext;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "SensorStepService onStartCommand service");

        this.mContext = getApplicationContext();

        return Service.START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values[0] > Integer.MAX_VALUE) {
            Log.d(TAG, "Sensor: probably not a real value: " + event.values[0]);
            return;
        } else {
            int steps = (int) event.values[0];
            if (steps > 0) {
                Log.d(TAG, "Sensor: from registering " + event.values[0]);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Do nothing in here
    }

    @Override
    public IBinder onBind(Intent intent) {
        //Do nothing in here
        return null;
    }

    /**
     * Allows to register to the {@link android.hardware.Sensor#TYPE_STEP_COUNTER} for counting step
     * thanks to the hardware chip.
     * Must calls {@link #isStepCounterFeatureAvailable(android.content.pm.PackageManager)} before to
     * know if the feature is available.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void registerSensorStep() {
        if (mContext != null) {
            Log.d(TAG, "Register sensor listener");
            SensorManager sm = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
            sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER), SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    /**
     * Allows to unregister to the {@link android.hardware.Sensor#TYPE_STEP_COUNTER} for counting step
     * thanks to the hardware chip.
     * Must calls {@link #isStepCounterFeatureAvailable(android.content.pm.PackageManager)} before to
     * know if the feature is available.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void unregisterSensorStep() {
        if (mContext != null) {
            Log.d(TAG, "Unregister sensor listener");
            SensorManager sm = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
            sm.unregisterListener(this);
        }
    }

    /**
     * Allows to know if the {@link android.hardware.Sensor#TYPE_STEP_COUNTER} is available for the device or not
     *
     * @return true if the feature is available, false otherwise.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public boolean isStepCounterFeatureAvailable(PackageManager pm) {   // Require at least Android KitKat
        int currentApiVersion = (int) Build.VERSION.SDK_INT;
        // Check that the device supports the step counter and detector sensors
        return currentApiVersion >= 19 && pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER) && pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR);
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

}
