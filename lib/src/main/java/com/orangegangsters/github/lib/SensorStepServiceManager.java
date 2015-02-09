package com.orangegangsters.github.lib;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by oliviergoutay on 2/5/15.
 */
public abstract class SensorStepServiceManager<T extends SensorStepReceiver> {

    /**
     * Tag for debugging
     */
    private static final String TAG = "SensorStepServiceManager";

    /**
     * Restart service every 30 minutes, just in case it has been collected
     */
    private static final long REPEAT_TIME_MILLISECONDS = 1000 * 60 * 30;

    /**
     * Broadcast event that is sent when the user grants us permission to get automatic tracking data
     */
    public static final String START_SENSOR_SERVICE = "com.omada.prevent.receiver.BootCompletedReceiver.START_UPDATE_SERVICE";

    /**
     * Broadcast event that is sent when the user wants to revoke permission to get automatic tracking data.
     */
    public static final String STOP_SENSOR_SERVICE = "com.omada.prevent.receiver.BootCompletedReceiver.STOP_UPDATE_SERVICE";

    /**
     * A service ID to recognize if the service is declared in the {@link android.app.AlarmManager} or not
     */
    public static final int SERVICE_ID = 12345;

    /**
     * Remember static intent, so we can stop it if the user revokes
     * authorization to use auto tracking
     */
    private static PendingIntent mService = null;

    /**
     * The {@link android.content.SharedPreferences} used to store the steps
     */
    private SharedPreferences mSharedPreferences;

    /**
     * The {@link android.content.SharedPreferences} key used to store the service started argument
     */
    public static final String STEP_COUNTER_ACTIVATED_PREFERENCE_KEY = "STEP_COUNTER_ACTIVATED_PREFERENCE_KEY";

    /**
     * Starts the {@link com.orangegangsters.github.lib.SensorStepService}
     */
    public void startStepSensorService(Context context, Intent intent) {
        Log.d(TAG, "Starting StepSensorService after broadcast");
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent updateReceiver = new Intent(context, getReceiverClass());

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        activateStepCounter(true);

        if (mService == null) {
            mService = PendingIntent.getBroadcast(context, SERVICE_ID, updateReceiver, PendingIntent.FLAG_CANCEL_CURRENT);
        }

        Calendar cal = Calendar.getInstance();

        if (intent.getAction().equalsIgnoreCase(START_SENSOR_SERVICE)) {
            //start immediately
            cal.add(Calendar.SECOND, 0);
        } else {//android.intent.action.BOOT_COMPLETED
            // Start 20 seconds after boot completed
            cal.add(Calendar.SECOND, 20);
        }

        // InexactRepeating allows Android to optimize the energy consumption
        if (BuildConfig.DEBUG) {//Repeat exactly for tests
            alarm.setRepeating(AlarmManager.RTC,
                    cal.getTimeInMillis(), REPEAT_TIME_MILLISECONDS, mService);
        } else {//Run in power efficient mode on release
            alarm.setInexactRepeating(AlarmManager.RTC,
                    cal.getTimeInMillis(), REPEAT_TIME_MILLISECONDS, mService);
        }
    }

    /**
     * Stops the {@link com.orangegangsters.github.lib.SensorStepService}
     */
    public void stopAutoUpdateService(Context context) {
        Log.d(TAG, "Stopping StepSensorService after broadcast");
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        activateStepCounter(false);

        if (mService == null) {
            Intent retrieverIntent = new Intent(context, getReceiverClass());
            mService = PendingIntent.getBroadcast(context, SERVICE_ID, retrieverIntent, PendingIntent.FLAG_NO_CREATE);
        }
        if (mService != null) {
            alarm.cancel(mService);
        }

        mService = null;
    }

    /**
     * Static method to call to send a {@link #START_SENSOR_SERVICE} intent to this
     * receiver that launches the {@link com.orangegangsters.github.lib.SensorStepService}
     *
     * @param context The context to use to send the broadcast
     */
    public static void startAutoUpdate(Context context) {
        context.sendBroadcast(new Intent(START_SENSOR_SERVICE));
    }

    /**
     * Static method to call to send a {@link #STOP_SENSOR_SERVICE} intent to this
     * receiver that stops the {@link com.orangegangsters.github.lib.SensorStepService}
     *
     * @param context The context to use to send the broadcast
     */
    public static void stopAutoUpdate(Context context) {
        context.sendBroadcast(new Intent(STOP_SENSOR_SERVICE));
    }

    /**
     * Allows to know if the {@link android.hardware.Sensor#TYPE_STEP_COUNTER} is available for the device or not
     *
     * @return true if the feature is available, false otherwise.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isStepCounterFeatureAvailable(PackageManager pm) {   // Require at least Android KitKat
        int currentApiVersion = (int) Build.VERSION.SDK_INT;
        // Check that the device supports the step counter and detector sensors
        return currentApiVersion >= 19 && pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER) && pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR);
    }

    public void activateStepCounter(boolean activate) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(STEP_COUNTER_ACTIVATED_PREFERENCE_KEY, activate);
        editor.apply();
    }

    public static boolean isStepCounterActivated(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(STEP_COUNTER_ACTIVATED_PREFERENCE_KEY, false);
    }

    public abstract Class<T> getReceiverClass();
}
