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

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.d("activ/ity", "setspeed = " + String.valueOf(speedometer.getmCurrentSpeed()+5));
//                speedometer.onSpeedChange(speedometer.getmCurrentSpeed()+5);
            }
        });

        go.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(speedometer.getmCurrentFuelLevel() > 0) {

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        speedometer.setGo(true);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        speedometer.setGo(false);
                    }
                }

//                Log.d("BUTTON", "++++++++++++++++++++++++++++++++++++++++++++");
//
//
//                Log.d("BUTTON", "action = " + String.valueOf(event.getAction()));
//                Log.d("BUTTON", "masked = " + String.valueOf(event.getActionMasked()));
//                Log.d("BUTTON", "state = " + String.valueOf(event.getButtonState()));
//                Log.d("BUTTON", "index = " + String.valueOf(event.getActionIndex()));

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

//                Log.d("BUTTON", "++++++++++++++++++++++++++++++++++++++++++++");
//
//
//                Log.d("BUTTON", "action = " + String.valueOf(event.getAction()));
//                Log.d("BUTTON", "masked = " + String.valueOf(event.getActionMasked()));
//                Log.d("BUTTON", "state = " + String.valueOf(event.getButtonState()));
//                Log.d("BUTTON", "index = " + String.valueOf(event.getActionIndex()));

                return false;
            }
        });
    }

    @Override
    public void onSpeedChange(float newSpeedValue) {
//        catch exception
        speedValue.setText(String.valueOf((int) newSpeedValue));
    }

    @Override
    public void showInfoMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
