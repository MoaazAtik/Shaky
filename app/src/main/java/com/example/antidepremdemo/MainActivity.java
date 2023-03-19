package com.example.antidepremdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Button btnOn, btnOff;
    private TextView txtStatus;
    private SensorManager sensorManager;
    private Sensor mAccelerometer;
    private int currentAcceleration;
    private int prevAcceleration;
    private int changeInAcceleration;
//    private MediaPlayer mediaPlayer;
    private SeekBar seekSensitivity, seekVolume;
    private Button btnReset;
    private int sensitivityCutoff = 0; //the lower value the more sensitive
    private SensorEventListener sensorEventListener;
//    private AudioManager audioManager;
//    private AudioManager audioManager = null;
//    private AudioAttributes audioAttributes;
//    float volumeBeforeDucking;

//    MediaService mediaService;

    MediaService mService;
    Boolean mIsBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOn = findViewById(R.id.btn_on);
        btnOff = findViewById(R.id.btn_off);
        txtStatus = findViewById(R.id.txt_status);
        seekSensitivity = findViewById(R.id.seek_sensitivity);
        seekVolume = findViewById(R.id.seek_volume);
        btnReset = findViewById(R.id.btn_reset);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

//        mediaService = new MediaService();
        mOn();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
//        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

//        audioAttributes = new AudioAttributes.Builder()
//                .setUsage(AudioAttributes.USAGE_MEDIA)
//                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                .build();

//        volumeBeforeDucking = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        //sensorEventListener
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                mSensorChanged(event);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };

        //btnOn
        btnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOn();
            }
        });

        //btnOff
        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOff();
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
//        seekVolume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
//        seekVolume.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        MediaService sService = mService;//why is sService and mService null?
        seekVolume.setMax(mService.audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        seekVolume.setProgress(mService.audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

        seekVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
                mService.audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });



    }//onCreate

    @Override
    protected void onResume() {
        super.onResume();
//        startService();
        mOn();
        Log.d(TAG, "onResume: " + mIsBound);
    }//onResume

    @Override
    protected void onPause() {
        super.onPause();
//        mOff();
//        if (mIsBound) {
//            unbindService(serviceConnection);
//            mIsBound = false;
            mOff();
            Log.d(TAG, "onPause: " + mIsBound);
//        }
    }//onPause

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (mIsBound) {
////            unbindService(serviceConnection);
////            mIsBound = false;
//            mOff();
//        }
        Log.d(TAG, "onDestroy: " + mIsBound);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
//            seekVolume.setProgress((seekVolume.getProgress()+1>seekVolume.getMax()) ? seekVolume.getMax() : seekVolume.getProgress()+1);
//            seekVolume.setProgress((seekVolume.getProgress()+1>seekVolume.getMax()) ? seekVolume.getMax() : audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            seekVolume.setProgress((seekVolume.getProgress()+1>seekVolume.getMax()) ?
                    seekVolume.getMax() :
                    mService.audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        } else if (keyCode==KeyEvent.KEYCODE_VOLUME_DOWN) {
//            seekVolume.setProgress((seekVolume.getProgress()-1<0) ? 0 : seekVolume.getProgress()-1);
//            seekVolume.setProgress((seekVolume.getProgress()-1<0) ? 0 : audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            seekVolume.setProgress((seekVolume.getProgress()-1<0) ?
                    0 : mService.audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
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
//            if (mediaPlayer != null) {
//            if (mediaService.mediaPlayer != null) {
            if (mIsBound) {
                Toast.makeText(MainActivity.this, "shaking", Toast.LENGTH_SHORT).show();
                mService.playAudio();
//                mediaService.playAudio();
            }
        }
    }//mSensorChanged

    private void mOn() {

//        Intent serviceIntent = new Intent(this, MediaService.class);
//        ContextCompat.startForegroundService(this, serviceIntent);

//        if (mediaPlayer == null) {
//        if (mediaService.mediaPlayer == null) {
        if (!mIsBound) {
            sensorManager.registerListener(sensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
////            mediaPlayer = MediaPlayer.create(this, R.raw.breach_alarm);
//            mediaPlayer = MediaPlayer.create(this, R.raw.soft);
//            mediaPlayer.setAudioAttributes(audioAttributes);
            startService();
            txtStatus.setText("Active");
            txtStatus.setTextSize(84);
            txtStatus.setAllCaps(true);
        }
    }//mActivate


//    private void playAudio() {
//
//        AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
//            @Override
//            public void onAudioFocusChange(int focusChange) {
//                if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
//                    volumeBeforeDucking = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
////                    mediaPlayer.setVolume(0.1f,0.1f);
//                } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
//                    mediaPlayer.setVolume(volumeBeforeDucking, volumeBeforeDucking);
//                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
//                    mediaPlayer.stop();
//                }
////                if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
////                if (mediaPlayer != null)
////                    mediaPlayer.start();
////                }
//            }
//        };
//
//        int result = 0;
//        AudioFocusRequest focusRequest = null;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            //for Android 8.0 (API level 26) through Android 11 (API level 30), and Android 12 (API level 31) or later
//            focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
//                    .build();
//            result = audioManager.requestAudioFocus(focusRequest);
//        } else {
//            //for Android 7.1 (API level 25) and lower
//            result = audioManager.requestAudioFocus(mOnAudioFocusChangeListener,
//                    AudioManager.STREAM_MUSIC,
//                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
//        }
//
//        AudioFocusRequest finalFocusRequest = focusRequest;
//        MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    audioManager.abandonAudioFocusRequest(finalFocusRequest);
//                } else {
//                    audioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
//                }
//            }
//        };
//
////        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//            mediaPlayer.start();
//            mediaPlayer.setOnCompletionListener(mCompletionListener);
////        }
//
//    }//playAudio


//    int result = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
//
//    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//
//        Word word = words.get(position);
//        mMediaPlayer = MediaPlayer.create(NumbersActivity.this, word.getAudioFile());
//        mMediaPlayer.start();
//        mMediaPlayer.setOnCompletionListener(mCompletionListener);
//    }
//
//AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
//    @Override
//    public void onAudioFocusChange(int focusChange) {
//        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
//            mMediaPlayer.pause();
//            mMediaPlayer.seekTo(0);
//        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
//            mMediaPlayer.start();
//        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
//            releaseMediaPlayer();
//        }
//    }
//};
//
//
//    //4.17
//    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
//        @Override
//        public void onCompletion(MediaPlayer mp) {
//            releaseMediaPlayer();
//        }
//    };

//        mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);


    private void mOff() {

//        Intent serviceIntent = new Intent(this, MediaService.class);
//        stopService(serviceIntent);

//        if (mediaService.mediaPlayer != null) {
        if (mIsBound) {

            //I may need these 2 lines I got from above
//            Intent serviceIntent = new Intent(this, MediaService.class);
//            stopService(serviceIntent);

            sensorManager.unregisterListener(sensorEventListener);
//            mediaPlayer.release();
//            mediaPlayer = null;
            unbindService(serviceConnection);
            mIsBound = false;
            txtStatus.setText("inactive");
            txtStatus.setTextSize(72);
            txtStatus.setAllCaps(false);
        }
    }//mStop

    private void startService() {
        Intent serviceIntent = new Intent(this, MediaService.class);
//        startService(serviceIntent);
        ContextCompat.startForegroundService(this, serviceIntent);

        bindService();
    }
    private void bindService() {
        Intent serviceBindIntent = new Intent(this, MediaService.class);
        bindService(serviceBindIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: connected to service.");

//            MediaService.MyBinder binder = (MediaService.MyBinder) service;
//            mService = binder.getService();
            mService = ((MediaService.MyBinder) service).getService();

            mIsBound = true;
//            getRandomNumberFromService(); // return a random number from the service
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: disconnected from service.");
            mIsBound = false;
        }
    };


}//MainActivity



//TODO: Manage device awake state. MediaPlayer.setWakeMode().
//TODO: txt_status text fill the TextView
//TODO: use a template fot the design
//TODO: feature: feedback and email
//todo: should I invoke abandonAudioFocus in onPause() mStop()?

// Done:
//TODO: sync seekVolume with the device's original one.
//TODO: check on the MediaPlayer code in 1MAC's and Edraak's project.
//todo: requestAudioFocus
//Todo: the audio is not playing as STREAM_SYSTEM, while seekVolume is controlling the volume as STREAM_SYSTEM properly.
// couldn't be done because Samsung has restricted the use of STREAM_SYSTEM audio stream type on some of its devices, including the Samsung Galaxy J7 Prime.


//Notes:
//Galaxy J7 Prime. 5.5" 1080x1920. Nougat 7 (API / SDK 24)