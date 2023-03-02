package com.example.antidepremdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
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
    private MediaPlayer mediaPlayer;
    private SeekBar seekSensitivity, seekVolume;
    private Button btnReset;
    private int sensitivityCutoff = 1; //the lower value the more sensitive
//    private AudioManager audioManager;
    private AudioManager audioManager = null;

    private SensorEventListener sensorEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnActivate = findViewById(R.id.btn_on);
        btnStop = findViewById(R.id.btn_off);
        txtStatus = findViewById(R.id.txt_status);
        seekSensitivity = findViewById(R.id.seek_sensitivity);
        seekVolume = findViewById(R.id.seek_volume);
        btnReset = findViewById(R.id.btn_reset);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                mSensorChanged(event);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
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

        //seekSensitivity
        seekSensitivity.setProgress(2);
        seekSensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                sensitivityCutoff = 3 - i;
                Toast.makeText(MainActivity.this, i  + " " + sensitivityCutoff, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        //seekVolume
        seekVolume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        seekVolume.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

        seekVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
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

        if (changeInAcceleration > sensitivityCutoff) {
            if (mediaPlayer != null) {
                Toast.makeText(MainActivity.this, "shaking", Toast.LENGTH_SHORT).show();
                mediaPlayer.start();
            }
        }
    }//mSensorChanged

    private void mActivate() {
        if (mediaPlayer == null) {
            sensorManager.registerListener(sensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
//            mediaPlayer = MediaPlayer.create(this, R.raw.breach_alarm);
            mediaPlayer = MediaPlayer.create(this, R.raw.soft);
            //todo
//            audioManager.requestAudioFocus(new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN));
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

//TODO: sync seekVolume with the device's original one.
//TODO: check on the MediaPlayer code in 1MAC's and Edraak's project.
//TODO: MediaPlayer.setWakeMode().
//TODO: use a template fot the design
//TODO: feature: feedback and email
