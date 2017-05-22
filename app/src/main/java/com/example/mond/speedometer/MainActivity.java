package com.example.mond.speedometer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SpeedometerView.SpeedChangeListener {

    SpeedometerView speedometer;
    TextView speedValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speedometer = (SpeedometerView) findViewById(R.id.speedometer);
        speedometer.setListener(this);
        speedValue = (TextView) findViewById(R.id.speedValue);

        Button stop = (Button) findViewById(R.id.stopBtn);
        Button go = (Button) findViewById(R.id.goBtn);

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
