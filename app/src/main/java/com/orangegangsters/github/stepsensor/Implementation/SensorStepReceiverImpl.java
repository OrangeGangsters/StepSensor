package com.orangegangsters.github.stepsensor.Implementation;

import com.orangegangsters.github.lib.SensorStepReceiver;

/**
 * Created by oliviergoutay on 2/5/15.
 */
public class SensorStepReceiverImpl extends SensorStepReceiver {
    @Override
    public Class getServiceClass() {
        return SensorStepServiceImpl.class;
    }
}
