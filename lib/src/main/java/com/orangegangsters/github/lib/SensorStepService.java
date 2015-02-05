package com.orangegangsters.github.lib;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by oliviergoutay on 2/4/15.
 */
public abstract class SensorStepService extends Service implements SensorEventListener {

    private static final String TAG = "SensorStepService";

    protected Context mContext;
    private static SensorStepCallback mCallback;

    /**
     * The {@link android.content.SharedPreferences} used to store the activate state
     */
    protected SharedPreferences mSharedPreferences;

    public SensorStepService() {
    }

    public SensorStepService(Context context) {
        this.mContext = context;
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "SensorStepService onStartCommand service");

        this.mContext = getApplicationContext();
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        if (isStepCounterActivated()) {
            registerSensorStep();
        } else {
            unregisterSensorStep();
        }

        return Service.START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isStepCounterActivated()) {
            if (event.values[0] > Integer.MAX_VALUE) {
                Log.d(TAG, "Sensor: probably not a real value: " + event.values[0]);
                return;
            } else {
                int steps = (int) event.values[0];
                if (steps > 0) {
                    Log.d(TAG, "Sensor: from registering " + event.values[0]);
                    storeSteps(steps);

                    updateCallback(steps);
                }
            }
        } else {
            unregisterSensorStep();
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

    protected void updateCallback(int steps) {
        if (mCallback != null) {
            mCallback.onUpdateSteps(steps);
        }
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    public static void setCallback(SensorStepCallback sensorStepCallback) {
        mCallback = sensorStepCallback;
    }

    public boolean isStepCounterActivated() {
        return mSharedPreferences.getBoolean(SensorStepServiceManager.STEP_COUNTER_ACTIVATED_PREFERENCE_KEY, false);
    }

    public abstract int getSteps();

    public abstract void storeSteps(int steps);

}
