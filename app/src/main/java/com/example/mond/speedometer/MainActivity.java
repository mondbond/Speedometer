package com.example.mond.speedometer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SpeedometerView.SpeedChangeListener {

    SpeedometerView speedometer;
    TextView speedValue;

    // TODO: 23/05/17 create layout with few  SpeedometerView, set up attributes from xml and from java code,
    // test different sizes(match_parent, wrap_content, width=100dp, height=200dp)
    // test attributes change at the runtime (when you click the button, set random color).


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speedometer = (SpeedometerView) findViewById(R.id.speedometer);
        speedometer.setListener(this);
        speedValue = (TextView) findViewById(R.id.speed_value);

        Button stop = (Button) findViewById(R.id.stop_btn);
        Button go = (Button) findViewById(R.id.go_btn);

        go.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(speedometer.getCurrentFuelLevel() > 0) {

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        speedometer.setGo(true);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        speedometer.setGo(false);
                    }
                }

                return false;
            }
        });


        stop.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    speedometer.setStop(true);
                } else if(event.getAction() == MotionEvent.ACTION_UP){
                    speedometer.setStop(false);
                }

                return false;
            }
        });
    }

    @Override
    public void onSpeedChange(float newSpeedValue) {
        speedValue.setText(String.valueOf((int) newSpeedValue));
    }
}
