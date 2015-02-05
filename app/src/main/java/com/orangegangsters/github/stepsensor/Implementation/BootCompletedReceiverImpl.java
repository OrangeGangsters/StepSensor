package com.orangegangsters.github.stepsensor.Implementation;

import com.orangegangsters.github.lib.BootCompletedReceiver;
import com.orangegangsters.github.lib.SensorStepServiceManager;

/**
 * Created by oliviergoutay on 2/5/15.
 */
public class BootCompletedReceiverImpl extends BootCompletedReceiver {

    private SensorStepServiceManager mSensorManager;

    @Override
    public SensorStepServiceManager getSensorManagerImpl() {
        if(mSensorManager == null) {
            mSensorManager = new SensorStepServiceManagerImpl();
        }
        return mSensorManager;
    }
}
