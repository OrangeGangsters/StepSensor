package com.orangegangsters.github.stepsensor;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private TextView mSteps;
    private Button mStart;
    private Button mStop;

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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_button:

                break;
            case R.id.stop_button:
                break;
        }
    }
}
