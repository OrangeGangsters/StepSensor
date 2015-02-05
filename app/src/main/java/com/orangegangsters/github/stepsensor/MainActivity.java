package com.orangegangsters.github.stepsensor;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.orangegangsters.github.lib.SensorStepCallback;
import com.orangegangsters.github.lib.SensorStepService;
import com.orangegangsters.github.lib.SensorStepServiceManager;
import com.orangegangsters.github.stepsensor.Implementation.SensorStepServiceImpl;

public class MainActivity extends ActionBarActivity implements View.OnClickListener, SensorStepCallback {

    private TextView mSteps;
    private Button mStart;
    private Button mStop;

    private SensorStepService mSensorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        mSteps = (TextView) findViewById(R.id.steps_textView);
        mStart = (Button) findViewById(R.id.start_button);
        mStop = (Button) findViewById(R.id.stop_button);

        mStart.setOnClickListener(this);
        mStop.setOnClickListener(this);

        SensorStepService.setCallback(this);
        mSensorService = new SensorStepServiceImpl(this);
        mSteps.setText("" + mSensorService.getSteps());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_button:
                SensorStepServiceManager.startAutoUpdate(this);
                break;
            case R.id.stop_button:
                SensorStepServiceManager.stopAutoUpdate(this);
                break;
        }
    }

    @Override
    public void onUpdateSteps(int steps) {
        mSteps.setText("" + mSensorService.getSteps());
    }
}
