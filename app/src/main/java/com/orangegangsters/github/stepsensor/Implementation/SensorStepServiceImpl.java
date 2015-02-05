package com.orangegangsters.github.stepsensor.Implementation;

import android.content.Context;
import android.content.SharedPreferences;

import com.orangegangsters.github.lib.SensorStepService;

/**
 * Created by oliviergoutay on 2/5/15.
 */
public class SensorStepServiceImpl extends SensorStepService {

    public SensorStepServiceImpl() {}

    public SensorStepServiceImpl(Context context) {
        super(context);
    }

    /**
     * The {@link android.content.SharedPreferences} key used to store the steps
     */
    private static final String STEPS_PREFERENCE_KEY = "STEPS_PREFERENCE_KEY";

    @Override
    public int getSteps() {
        return mSharedPreferences.getInt(STEPS_PREFERENCE_KEY, 0);
    }

    @Override
    public void storeSteps(int steps) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(STEPS_PREFERENCE_KEY, steps);
        editor.apply();
    }
}
