package com.orangegangsters.github.lib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Receiver that is triggered by an {@link android.app.AlarmManager} to start the
 * {@link com.orangegangsters.github.lib.SensorStepService}
 * Created by oliviergoutay on 2/5/15.
 */
public abstract class SensorStepReceiver<T extends SensorStepService> extends BroadcastReceiver {

    private static final String TAG = "SensorStepReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Starting SensorStepReceiver");
        Intent updateService = new Intent(context, getServiceClass());
        context.startService(updateService);
    }

    public abstract Class<T> getServiceClass();
}