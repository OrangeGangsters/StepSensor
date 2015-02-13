package com.orangegangsters.github.lib;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Created by oliviergoutay on 2/4/15.
 */
public abstract class SensorStepService extends Service implements SensorEventListener {

    private static final String TAG = "SensorStepService";
    private static final int SCREEN_OFF_RECEIVER_DELAY = 5000;

    protected Context mContext;
    private static SensorStepCallback mCallback;
    private SensorStepServiceManager mSensorManager;
    private PowerManager.WakeLock mWakeLock;

    private static SensorStepService mInstance;

    public static SensorStepService getInstance() {
        return mInstance;
    }

    public SensorStepService() {
    }

    public SensorStepService(Context context) {
        this.mContext = context;
    }

    BroadcastReceiver mScreenOffBroadcastReceiver = new BroadcastReceiver() {

        //When Event is published, onReceive method is called
        @Override
        public void onReceive(final Context context, Intent intent) {
            Log.i(TAG, "onReceive(" + intent + ")");
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF) || intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                Log.d(TAG, "SensorStepScreenOffReceiver triggered, registering StepSensor again");
                restartListener(context);

                Runnable runnable = new Runnable() {
                    public void run() {
                        Log.d(TAG, "SensorStepScreenOffReceiver Runnable executes");
                        restartListener(context);
                    }
                };

                new Handler().postDelayed(runnable, SCREEN_OFF_RECEIVER_DELAY);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "SensorStepService onCreate");

        PowerManager manager =
                (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.acquire();

        registerReceiver(mScreenOffBroadcastReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        registerReceiver(mScreenOffBroadcastReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "SensorStepService onStartCommand service");

        this.mContext = getApplicationContext();
        mInstance = this;

        //Start as a foreground service to keep running
        if (mContext != null) {
            startForeground(SensorStepServiceManager.SERVICE_ID, getNotification(mContext));
            Log.d(TAG, "SensorStepService startForeground service");
        }

        if (SensorStepServiceManager.isStepCounterActivated(mContext)) {
            registerSensorStep();
        } else {
            unregisterSensorStep();
        }

        if (mWakeLock != null) {
            mWakeLock.acquire();
            Log.d(TAG, "Acquired Partial WakeLock service");
        }

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "SensorStepService onDestroy");
        unregisterReceiver(mScreenOffBroadcastReceiver);
        unregisterSensorStep();
        if (mWakeLock != null) {
            mWakeLock.release();
        }
        super.onDestroy();
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
                    if (getZeroSteps() == 0) {
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
     * Must calls {@link com.orangegangsters.github.lib.SensorStepServiceManager#isStepCounterFeatureAvailable(android.content.pm.PackageManager)} before to
     * know if the feature is available.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void registerSensorStep() {
        if (mContext != null) {
            Log.d(TAG, "Register sensor listener");
            SensorManager sm = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
            sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER), SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    /**
     * Allows to unregister to the {@link android.hardware.Sensor#TYPE_STEP_COUNTER} for counting step
     * thanks to the hardware chip.
     * Must calls {@link com.orangegangsters.github.lib.SensorStepServiceManager#isStepCounterFeatureAvailable(android.content.pm.PackageManager)} before to
     * know if the feature is available.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void unregisterSensorStep() {
        if (mContext != null) {
            Log.d(TAG, "Unregister sensor listener");
            SensorManager sm = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
            sm.unregisterListener(this);
            stopForeground(true);
        }
    }

    /**
     * Allows to restart the listener (screen off etc...)
     */
    public void restartListener(Context context) {
        if (SensorStepServiceManager.isStepCounterActivated(context)) {
            SensorStepService sensorStepService = SensorStepService.getInstance();
            if (sensorStepService != null) {
                sensorStepService.unregisterSensorStep();
                sensorStepService.registerSensorStep();
                mWakeLock.acquire();
            }
        }
    }

    /**
     * Get the notification to display while running the service in foreground.
     */
    private Notification getNotification(Context context) {
        Intent notificationIntent = new Intent(context, getNotificationLaunchClass());
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(getNotificationIcon())
                        .setContentTitle(getNotificationContentTitle())
                        .setContentText(getNotificationContentText())
                        .setContentIntent(contentIntent);

        return mBuilder.build();
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

        if (steps < 0) {
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

    /**
     * Return the class you want to be launch while clicking on the service notification.
     */
    public abstract Class getNotificationLaunchClass();

    /**
     * Return the id of the icon you want to be displayed while the service is launched.
     */
    public abstract int getNotificationIcon();

    /**
     * Return the String you want to be displayed as the title while the service is launched and the notification shown.
     */
    public abstract String getNotificationContentTitle();

    /**
     * Return the String you want to be displayed as the content while the service is launched and the notification shown.
     */
    public abstract String getNotificationContentText();

}
