package com.orangegangsters.github.lib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * A receiver that catches {@link android.content.Intent#ACTION_BOOT_COMPLETED}, {@link com.orangegangsters.github.lib.SensorStepServiceManager#START_SENSOR_SERVICE},
 * and {@link com.orangegangsters.github.lib.SensorStepServiceManager#STOP_SENSOR_SERVICE} actions. Controls the periodic updates done by
 * {@link com.orangegangsters.github.lib.SensorStepService}
 * Created by oliviergoutay on 2/5/15.
 */
public abstract class BootCompletedReceiver<T extends SensorStepServiceManager> extends BroadcastReceiver {
    /**
     * Tag for debugging
     */
    private static final String TAG = "BootCompletedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive, action : " + intent.getAction());

        //Check if we need to start on boot completed
        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            if (SensorStepServiceManager.isStepCounterActivated(context)) {
                getSensorManagerImpl().startStepSensorService(context, intent);
            } else {
                getSensorManagerImpl().stopAutoUpdateService(context);
            }
            return;
        }

        //Start the service on START_SENSOR_SERVICE
        if(intent.getAction().equalsIgnoreCase(SensorStepServiceManager.START_SENSOR_SERVICE)) {
            getSensorManagerImpl().startStepSensorService(context, intent);
        }

        //Stop the service on STOP_SENSOR_SERVICE
        if (intent.getAction().equalsIgnoreCase(SensorStepServiceManager.STOP_SENSOR_SERVICE)) {
            getSensorManagerImpl().stopAutoUpdateService(context);
        }
    }

    public abstract T getSensorManagerImpl();


}
