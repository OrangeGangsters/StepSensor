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
public abstract class SensorStepService extends Service implements SensorEventListener {

    private static final String TAG = "SensorStepService";

    protected Context mContext;
    private static SensorStepCallback mCallback;
    private SensorStepServiceManager mSensorManager;

    public SensorStepService() {
    }

    public SensorStepService(Context context) {
        this.mContext = context;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "SensorStepService onStartCommand service");

        this.mContext = getApplicationContext();

        if (SensorStepServiceManager.isStepCounterActivated(mContext)) {
            registerSensorStep();
        } else {
            unregisterSensorStep();
        }

        return Service.START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (SensorStepServiceManager.isStepCounterActivated(mContext)) {
            if (event.values[0] > Integer.MAX_VALUE) {
                Log.d(TAG, "Sensor: probably not a real value: " + event.values[0]);
                return;
            } else {
                int steps = (int) event.values[0];
                if (steps > 0) {
                    Log.d(TAG, "Sensor: from registering " + event.values[0]);

                    storeRawSteps(steps);

                    //Store the number of zero steps if none yet
                    if(getZeroSteps() == 0) {
                        storeZeroSteps();
                    }

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

    /**
     * Returns the step stored by doing:
     * {@link #getRawSteps()} minus {@link #getZeroSteps()}
     *
     * @return the actual steps
     */
    public int getSteps() {
        Log.d(TAG, "getSteps called ==> Steps:" + getRawSteps() + " getZeroSteps:" + getZeroSteps());
        int steps = getRawSteps() - getZeroSteps();

        if(steps < 0) {
            storeZeroSteps();
            return 0;
        }

        return steps;
    }

    /**
     * Let the implementation handles the storage of the steps
     * - {@link android.content.SharedPreferences}
     * - {@link android.database.sqlite.SQLiteDatabase}
     * - Others
     *
     * @return the steps stored
     */
    public abstract int getRawSteps();

    /**
     * Let the implementation handles the storage of the steps
     * - {@link android.content.SharedPreferences}
     * - {@link android.database.sqlite.SQLiteDatabase}
     * - Others
     * Used by {@link android.hardware.SensorEventListener#onSensorChanged(android.hardware.SensorEvent)}
     *
     * @param steps the number of steps we want to store.
     */
    public abstract void storeRawSteps(int steps);

    /**
     * Let the implementation handles the storage of the zero steps.
     * The {@link android.hardware.SensorEventListener} returns the number of steps since the previous reboot.
     * We need to store a zero value we first access it, when the day changes etc...
     *
     * @return the zero steps stored
     */
    public abstract int getZeroSteps();

    /**
     * Let the implementation handles the storage of the zero steps.
     * The {@link android.hardware.SensorEventListener} returns the number of steps since the previous reboot.
     * We need to store a zero value we first access it, when the day changes etc...
     */
    public abstract void storeZeroSteps();

}
