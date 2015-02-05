package com.orangegangsters.github.stepsensor.Implementation;

import com.orangegangsters.github.lib.SensorStepServiceManager;

/**
 * Created by oliviergoutay on 2/5/15.
 */
public class SensorStepServiceManagerImpl extends SensorStepServiceManager {
    @Override
    public Class getReceiverClass() {
        return SensorStepReceiverImpl.class;
    }
}
