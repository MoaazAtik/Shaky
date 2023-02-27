package com.example.antidepremdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button btnActivate, btnStop;
    private TextView txtStatus;
    private SensorManager sensorManager;
    private Sensor mAccelerometer;
    private int currentAcceleration;
    private int prevAcceleration;
    private int changeInAcceleration;
    MediaPlayer mediaPlayer;

    private SensorEventListener sensorEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnActivate = findViewById(R.id.btn_activate);
        btnStop = findViewById(R.id.btn_stop);
        txtStatus = findViewById(R.id.txt_status);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                mSensorChanged(event);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        btnActivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivate();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStop();
            }
        });
    }//onCreate

    protected void onResume() {
        super.onResume();
        mActivate();
    }//onResume

    protected void onPause() {
        super.onPause();
        mStop();
    }//onPause


    private void mSensorChanged(SensorEvent event) {
        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];

        currentAcceleration = (int) Math.sqrt(x * x + y * y + z * z);
        if (prevAcceleration != 0) {
            changeInAcceleration = currentAcceleration - prevAcceleration;
        }
        prevAcceleration = currentAcceleration;

        if (changeInAcceleration > 2) {
            if (mediaPlayer != null) {
                Toast.makeText(MainActivity.this, "shaking", Toast.LENGTH_SHORT).show();
                mediaPlayer.start();
            }
        }
    }//mSensorChanged

    private void mActivate() {
        if (mediaPlayer == null) {
            sensorManager.registerListener(sensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            mediaPlayer = MediaPlayer.create(this, R.raw.breach_alarm);
            txtStatus.setText("Active");
            txtStatus.setTypeface(Typeface.SERIF);
        }
    }//mActivate

    private void mStop() {
        if (mediaPlayer != null) {
            sensorManager.unregisterListener(sensorEventListener);
            mediaPlayer.release();
            mediaPlayer = null;
            txtStatus.setText("Inactive");
            txtStatus.setTypeface(Typeface.DEFAULT);
        }
    }//mStop


}//MainActivity

//TODO: MediaPlayer.setWakeMode().
//todo: check on the MediaPlayer code in 1MAC's and Edraak's project.