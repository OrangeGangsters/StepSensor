package com.orangegangsters.github.stepsensor;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.WindowManager;

import com.orangegangsters.github.lib.SensorStepServiceManager;
import com.orangegangsters.github.stepsensor.Implementation.SensorStepReceiverImpl;
import com.orangegangsters.github.stepsensor.Implementation.SensorStepServiceImpl;
import com.orangegangsters.github.stepsensor.Implementation.SensorStepServiceManagerImpl;
import com.robotium.solo.Condition;
import com.robotium.solo.Solo;

/**
 * Created by oliviergoutay on 2/10/15.
 */
public class ApplicationTest extends ActivityInstrumentationTestCase2<MainActivity> {

    protected Solo solo;

    public ApplicationTest() {
        super(MainActivity.class);
    }

    /**
     * Set up.
     * Called before each tests.
     *
     * @throws Exception if one
     */
    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    protected void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
        wakeUpScreen();
    }

    /**
     * Called at the end of each tests (allows to log out and finish all the activities)
     *
     * @throws Exception
     */
    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    @Override
    protected void tearDown() throws Exception {
        solo.finishOpenedActivities();

        super.tearDown();
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    private void wakeUpScreen() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            }
        });
        solo.unlockScreen();
    }

    /**
     * Test the activation/deactivation of the StepTracking service.
     * Needs to be run first. PendingIntent instances are kept in app memory even if disabled until
     * the app is shutdown. Close the app before (remove from app history or force close).
     */
    @SmallTest
    public void testActivateDeactivateStepTracking() {
        //Check no service running
        final Intent retrieverIntent = new Intent(getActivity(), SensorStepReceiverImpl.class);
        assertTrue(solo.waitForCondition(new Condition() {
            @Override
            public boolean isSatisfied() {
                PendingIntent pIntent = PendingIntent.getBroadcast(getActivity(), SensorStepServiceManager.SERVICE_ID, retrieverIntent, PendingIntent.FLAG_NO_CREATE);
                return pIntent == null;
            }
        }, 60000));

        //Check started
        SensorStepServiceManagerImpl.startAutoUpdate(solo.getCurrentActivity());
        assertTrue(solo.waitForCondition(new Condition() {
            @Override
            public boolean isSatisfied() {
                PendingIntent pIntent = PendingIntent.getBroadcast(getActivity(), SensorStepServiceManager.SERVICE_ID, retrieverIntent, PendingIntent.FLAG_NO_CREATE);
                return pIntent != null;
            }
        }, 60000));
        assertTrue(SensorStepServiceManager.isStepCounterActivated(solo.getCurrentActivity()));

        SensorStepServiceManagerImpl.stopAutoUpdate(solo.getCurrentActivity());
        assertFalse(solo.waitForCondition(new Condition() {
            @Override
            public boolean isSatisfied() {
                return !SensorStepServiceManager.isStepCounterActivated(solo.getCurrentActivity());
            }
        }, 60000));
    }

    /**
     * Test if the method getSteps() is returning rawSteps - zeroSteps
     */
    @SmallTest
    public void testGetSteps() {
        //Store numbers
        int raw = 200;
        int zero = 50;

        SensorStepServiceImpl sensorStepService = new SensorStepServiceImpl(solo.getCurrentActivity());
        sensorStepService.storeRawSteps(zero);
        solo.sleep(1000);
        sensorStepService.storeZeroSteps();
        solo.sleep(1000);
        assertEquals(0, sensorStepService.getSteps());
        sensorStepService.storeRawSteps(raw);

        solo.sleep(1000);
        assertEquals(raw - zero, sensorStepService.getSteps());
    }

    /**
     * Test the zeroSteps setting on first activation and on the manager's method
     */
    @SmallTest
    public void testSetZeroSteps() {
        //Reset everything
        String STEPS_PREFERENCE_KEY = "STEPS_PREFERENCE_KEY";
        String ZERO_STEPS_PREFERENCE_KEY = "ZERO_STEPS_PREFERENCE_KEY";
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(solo.getCurrentActivity());
        sharedPreferences.edit().remove(STEPS_PREFERENCE_KEY)
                .remove(ZERO_STEPS_PREFERENCE_KEY)
                .remove(SensorStepServiceManager.STEP_COUNTER_ACTIVATED_PREFERENCE_KEY)
                .apply();

        //Set zero steps on first call
        final SensorStepServiceImpl sensorStepService = new SensorStepServiceImpl(solo.getCurrentActivity());
        SensorStepServiceManagerImpl.startAutoUpdate(solo.getCurrentActivity());
        assertTrue(solo.waitForCondition(new Condition() {
            @Override
            public boolean isSatisfied() {
                return sensorStepService.getZeroSteps() != 0;
            }
        }, 60000));

        //Set zero steps on method called
        sensorStepService.storeRawSteps(sensorStepService.getZeroSteps() + 5);
        solo.clickOnText("zero");
        assertTrue(solo.waitForCondition(new Condition() {
            @Override
            public boolean isSatisfied() {
                return sensorStepService.getZeroSteps() == sensorStepService.getRawSteps();
            }
        }, 30000));
    }
}