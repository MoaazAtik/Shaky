package com.example.antidepremdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.KeyEvent;
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
    private int sensitivityCutoff = 0; //the lower value the more sensitive
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

//        setVolumeControlStream(AudioManager.STREAM_SYSTEM);
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
                sensitivityCutoff = (9 - (i * 4)) / 2;
                Toast.makeText(MainActivity.this, i  + " " + "sensitivityCutoff" + sensitivityCutoff, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        //seekVolume
        seekVolume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM));
        seekVolume.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM));

        seekVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, i, 0);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP){
//            seekVolume.setProgress((seekVolume.getProgress()+1>seekVolume.getMax()) ? seekVolume.getMax() : seekVolume.getProgress()+1);
            seekVolume.setProgress((seekVolume.getProgress()+1>seekVolume.getMax()) ? seekVolume.getMax() : audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        }else if (keyCode==KeyEvent.KEYCODE_VOLUME_DOWN){
//            seekVolume.setProgress((seekVolume.getProgress()-1<0) ? 0 : seekVolume.getProgress()-1);
            seekVolume.setProgress((seekVolume.getProgress()-1<0) ? 0 : audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        }

        return super.onKeyDown(keyCode, event);
    }//onKeyDown


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
//            audioManager.requestAudioFocus();
            txtStatus.setText("Active");
            txtStatus.setTextSize(84);
            txtStatus.setAllCaps(true);
        }
    }//mActivate

    private void mStop() {
        if (mediaPlayer != null) {
            sensorManager.unregisterListener(sensorEventListener);
            mediaPlayer.release();
            mediaPlayer = null;
//            txtStatus.setText("Inactive");
            txtStatus.setText("inactive");
            txtStatus.setTextSize(72);
            txtStatus.setAllCaps(false);
        }
    }//mStop


}//MainActivity


//Todo: seekVolume is not controlling the volume as STREAM_SYSTEM
//TODO: check on the MediaPlayer code in 1MAC's and Edraak's project.
//TODO: MediaPlayer.setWakeMode().
//TODO: txt_status text fill the TextView
//TODO: use a template fot the design
//TODO: feature: feedback and email

// Done:
//TODO: sync seekVolume with the device's original one.

//Notes:
//Nougat 7 (API / SDK 24)