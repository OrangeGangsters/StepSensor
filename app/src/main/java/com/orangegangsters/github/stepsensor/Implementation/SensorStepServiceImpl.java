package com.orangegangsters.github.stepsensor.Implementation;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.orangegangsters.github.lib.SensorStepService;

/**
 * Created by oliviergoutay on 2/5/15.
 */
public class SensorStepServiceImpl extends SensorStepService {

    private static final String TAG = "SensorStepServiceImpl";

    public SensorStepServiceImpl() {
    }

    public SensorStepServiceImpl(Context context) {
        super(context);
    }

    /**
     * The {@link android.content.SharedPreferences} key used to store the steps
     */
    private static final String STEPS_PREFERENCE_KEY = "STEPS_PREFERENCE_KEY";
    /**
     * The {@link android.content.SharedPreferences} key used to store the zero steps
     */
    private static final String ZERO_STEPS_PREFERENCE_KEY = "ZERO_STEPS_PREFERENCE_KEY";

    @Override
    public int getRawSteps() {
        return mSharedPreferences.getInt(STEPS_PREFERENCE_KEY, 0);
    }

    @Override
    public void storeRawSteps(int steps) {
        Log.d(TAG, "storeSteps called ==> Steps:" + steps);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(STEPS_PREFERENCE_KEY, steps);
        editor.apply();
    }

    @Override
    public int getZeroSteps() {
        return mSharedPreferences.getInt(ZERO_STEPS_PREFERENCE_KEY, 0);
    }

    @Override
    public void storeZeroSteps() {
        Log.d(TAG, "storeZeroSteps called ==> Steps:" + getRawSteps());
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(ZERO_STEPS_PREFERENCE_KEY, getRawSteps());
        editor.apply();
    }
}
