package com.example.mond.speedometer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements SpeedometerView.SpeedChangeListener {

    Random mRandom;
    SpeedometerView mSpeedometer;
    SpeedometerView mAnotherSpeedometer;
    TextView speedValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRandom = new Random();

        mSpeedometer = (SpeedometerView) findViewById(R.id.speedometer);
        mAnotherSpeedometer = (SpeedometerView) findViewById(R.id.speedometer1);
        mSpeedometer.setListener(this);

        speedValue = (TextView) findViewById(R.id.speed_value);

        Button setBtn = (Button) findViewById(R.id.setBtn);
        setBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAnotherSpeedometer.setBackgroundColor(getRandomColor());
                mAnotherSpeedometer.setSpeedIndicatorColor(getRandomColor());
                mAnotherSpeedometer.setBeforeArrowSectorColor(getRandomColor());
                mAnotherSpeedometer.setAfterArrowSectorColor(getRandomColor());
                mAnotherSpeedometer.setArrowColor(getRandomColor());
                mAnotherSpeedometer.setBorderColor(getRandomColor());
            }
        });


        Button stop = (Button) findViewById(R.id.stop_btn);
        Button go = (Button) findViewById(R.id.go_btn);

        go.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(mSpeedometer.getCurrentFuelLevel() > 0) {

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        mSpeedometer.setGo(true);
                        mAnotherSpeedometer.setGo(true);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        mSpeedometer.setGo(false);
                        mAnotherSpeedometer.setGo(false);
                    }
                }

                return false;
            }
        });

        stop.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    mSpeedometer.setStop(true);
                    mAnotherSpeedometer.setStop(true);
                } else if(event.getAction() == MotionEvent.ACTION_UP){
                    mSpeedometer.setStop(false);
                    mAnotherSpeedometer.setStop(false);
                }

                return false;
            }
        });

        Button refill = (Button) findViewById(R.id.refill);
        refill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             mSpeedometer.setFuelMaxLevel();
             mAnotherSpeedometer.setFuelMaxLevel();
            }
        });
    }

    @Override
    public void onSpeedChange(float newSpeedValue) {
        speedValue.setText(String.valueOf((int) newSpeedValue));
    }

    private int getRandomColor(){
        int color = 0;
        switch (mRandom.nextInt(6)){
            case 0:
                color = getResources().getColor(R.color.blue);
                break;
            case 1:
                color = getResources().getColor(R.color.red);
                break;
            case 2:
                color = getResources().getColor(R.color.yellow);
                break;
            case 3:
                color = getResources().getColor(R.color.green);
                break;
            case 4:
                color = getResources().getColor(R.color.orange);
                break;
            case 5:
                color = getResources().getColor(R.color.brown);
                break;
            case 6:
                color = getResources().getColor(R.color.pink);
                break;
        }

        return color;
    }
}
