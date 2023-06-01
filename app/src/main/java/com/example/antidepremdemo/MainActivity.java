package com.example.antidepremdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.airbnb.lottie.LottieAnimationView;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Button btnOn, btnOff;
    private SensorManager sensorManager;
    private Sensor mAccelerometer;
    private int currentAcceleration;
    private int prevAcceleration;
    private int changeInAcceleration;
    private SeekBar seekSensitivity, seekVolume;
    private Button btnMore;
    private int sensitivityCutoff = 1; //the lower value the more sensitive
    private SensorEventListener sensorEventListener;
    private FragmentContainerView fragmentContainerView;

    public static MediaService mService;
    public Boolean mIsBound = false;

    private MotionLayout motionLayout;

    private TextSwitcher textSwitcher;

    private LottieAnimationView lStarsActivation;


    private static Context contex;
    public static Context getContex() {
        return contex; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contex = getApplicationContext();

        btnOn = findViewById(R.id.btn_on);
        btnOff = findViewById(R.id.btn_off);
//        txtStatus = findViewById(R.id.txt_status);
        seekSensitivity = findViewById(R.id.seek_sensitivity);
        seekVolume = findViewById(R.id.seek_volume);
        btnMore = findViewById(R.id.btn_more);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        fragmentContainerView = findViewById(R.id.fragmentContainerView);


        motionLayout = findViewById(R.id.motion_layout);

//        lStarsActivation = findViewById(R.id.sta);

//        motionLayout.setTransition(R.id.transition_inactive_to_active);
//        motionLayout.jumpToState(R.id.inactive);
//        motionLayout.transitionToState(R.id.active);

//        motionLayout.transitionToStart();
//        motionLayout.transitionToEnd(); //worked alone

//        TransitionManager.go(R.id.transition2, new ChangeBounds());
//        TransitionManager.go(R.id.transition_inactive_to_active);

//        MotionScene.Transition transition = motionLayout.getTransition(R.id.transition_inactive_to_active);
//        transition.start();

//        TransitionManager transitionManager = new TransitionManager();
//        transitionManager.

//        motionLayout.setTransition(R.id.transition_inactive_to_active);
//        motionLayout.transitionToEnd();
////        motionLayout.transitionToStart();

        textSwitcher = (TextSwitcher) findViewById(R.id.text_switcher);
        textSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView textView = new TextView(MainActivity.this);
                textView.setTextColor(Color.BLACK);
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                textView.setTypeface(Typeface.SERIF);
                return textView;
            }
        });
        textSwitcher.setCurrentText(getString(R.string.status_inactive));

        Log.d(TAG, "onCreate: " + mIsBound);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);


        //sensorEventListener
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                mSensorChanged(event);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };

        //to avoid reinitializing a new service when configurations change (e.g. screen rotation)
        if (savedInstanceState == null) {
            mService = new MediaService();
            Log.d(TAG, "onCreate: 1" + "savedInstanceState..."+" "+c(mService)+" "+mIsBound);
            mOn(true, 0);
        } else if (savedInstanceState.getBoolean("mIsBound") == true) {
            mOn(false, 0);
            Log.d(TAG, "onCreate: 21" +" "+c(mService)+" "+mIsBound);
        } else { //savedInstanceState.getBoolean("mIsBound") == false
            Log.d(TAG, "onCreate: 22" +" "+c(mService)+" "+mIsBound);
        }

        //btnOn
        btnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOn(true, 1);
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
//        seekSensitivity.setProgress(2);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        seekSensitivity.setProgress(
                seekSensitivity.getMax() - sharedPreferences.getInt("sensitivity_cutoff", 0) );
        Log.d(TAG, "sensitivity cutoff after getInt = " + sharedPreferences.getInt("sensitivity_cutoff", 111));
        Log.d(TAG, "max sensitivity is " + seekSensitivity.getMax());

        seekSensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//                sensitivityCutoff = (9 - (i * 4)) / 2;
                sensitivityCutoff = seekSensitivity.getMax() - i;

                sharedPreferences.edit().putInt("sensitivity_cutoff", sensitivityCutoff).apply();
                Log.d(TAG, "sensitivity cutoff after putInt = " + sharedPreferences.getInt("sensitivity_cutoff", 111));

                Toast.makeText(MainActivity.this, i  + " " + "sensitivityCutoff" + sensitivityCutoff, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        //seekVolume
        seekVolume.setMax(mService.audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        seekVolume.setProgress(mService.audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

        seekVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mService.audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        //btnMore
        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragmentContainerView, new MoreFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

    }//onCreate

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("mIsBound", mIsBound);
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: " + mIsBound+" "+c(mService));
    }//onResume

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: " + mIsBound+" "+c(mService));
    }//onPause

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: " + mIsBound+" "+c(mService));
        if (isFinishing()) {
            mOff(); //especially for unregisterListener()
            Log.d(TAG, "onDestroy: isFinishing " + c(mService));
        }

        if (mIsBound) {
            unbindService(serviceConnection);
            mIsBound = false;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
//            seekVolume.setProgress((seekVolume.getProgress()+1>seekVolume.getMax()) ? seekVolume.getMax() : seekVolume.getProgress()+1);
//            seekVolume.setProgress((seekVolume.getProgress()+1>seekVolume.getMax()) ? seekVolume.getMax() : audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            seekVolume.setProgress((seekVolume.getProgress() + 1 > seekVolume.getMax()) ?
                    seekVolume.getMax() : mService.audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
//            seekVolume.setProgress((seekVolume.getProgress()-1<0) ? 0 : seekVolume.getProgress()-1);
//            seekVolume.setProgress((seekVolume.getProgress()-1<0) ? 0 : audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            seekVolume.setProgress((seekVolume.getProgress() - 1 < 0) ?
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
            if (mIsBound) {
                mService.playAudio();
            }
        }
    }//mSensorChanged

    private void mOn(boolean firstStartingService, int transitionAnimation) {

        if (!mIsBound) {
            sensorManager.registerListener(sensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

            Log.d(TAG, "mOn: " + mIsBound+" "+c(mService));
            if (firstStartingService) {
                startService();
            } else {
                bindService();
            }

            animate(transitionAnimation);

//            txtStatus.setText(R.string.status_active);
//            txtStatus.setTextSize(84);
//            txtStatus.setAllCaps(true);
        }

//        ObjectAnimator animator = ObjectAnimator.ofObject(txtStatus, "text", new TypeEvaluator<String>() {
//            @Override
//            public String evaluate(float fraction, String startValue, String endValue) {
//                // Interpolate the text value based on the fraction
//                return null; // Calculate interpolated text based on fraction, startValue, and endValue
//            }
//        }, String.valueOf(R.string.status_inactive), "End Text");
//        animator.setDuration(1000); // Animation duration in milliseconds
//        animator.start();

//        motionLayout.setTransition(R.id.inactive, R.id.active);
//        motionLayout.transitionToEnd();

//        Animation animation = AnimationUtils.loadAnimation(this, R.anim.text_animation);
//        txtStatus.startAnimation(animation);

    }//mOn

    private void animate(int transition) {

        Animation animationIn = AnimationUtils.loadAnimation(this, R.anim.animation_in);
        Animation animationOut = AnimationUtils.loadAnimation(this, R.anim.animation_out);

        textSwitcher.setInAnimation(animationIn);
        textSwitcher.setOutAnimation(animationOut);

        if (transition == 0) { //app initiation (initial to active state)
//            motionLayout.setTransition(R.id.initial, R.id.active);
//            motionLayout.transitionToEnd();

            textSwitcher.setText(getString(R.string.status_active));
            TextView textView = (TextView) textSwitcher.getCurrentView();
            textView.setTextSize(84);
            textView.setAllCaps(true);

        } else if (transition == 1) { //to active state (inactive to active state)
            motionLayout.setTransition(R.id.inactive, R.id.active);
            motionLayout.transitionToEnd();

            textSwitcher.setText(getString(R.string.status_active));
            TextView textView = (TextView) textSwitcher.getCurrentView();
            textView.setTextSize(84);
            textView.setAllCaps(true);

        } else if (transition == 2) { //to inactive state (inactive to active state)
            motionLayout.setTransition(R.id.active, R.id.inactive);
            motionLayout.transitionToEnd();

            textSwitcher.setText(getString(R.string.status_inactive));
            TextView textView = (TextView) textSwitcher.getCurrentView();
            textView.setTextSize(72);
            textView.setAllCaps(false);
        }

    }//animate()

    private void mOff() {

        if (mIsBound) {

            sensorManager.unregisterListener(sensorEventListener);

            unbindService(serviceConnection);
            Log.d(TAG, "mOff: after unbindService " + mIsBound+" "+c(mService));

            stopService(new Intent(this, MediaService.class));
            Log.d(TAG, "mOff: afterstopService " + mIsBound+" "+c(mService));

            animate(2);

            mIsBound = false;

//            txtStatus.setText(R.string.status_inactive);
//            txtStatus.setTextSize(72);
//            txtStatus.setAllCaps(false);
        }
    }//mOff

    private void startService() {
        Intent serviceIntent = new Intent(this, MediaService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
        Log.d(TAG, "startService: " + mIsBound);
        bindService();
    }
    private void bindService() {
        Intent serviceBindIntent = new Intent(this, MediaService.class);
        bindService(serviceBindIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "bindService: "+ mIsBound);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: " + c(mService));

            MediaService.MyBinder binder = (MediaService.MyBinder) service;
            mService = binder.getService();
//            mService = ((MediaService.MyBinder) service).getService();
            mIsBound = true;
            Log.d(TAG, mIsBound + " mService = " + c(mService));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: disconnected from service." + c(mService));
            mIsBound = false;

        }
    };

    //helper method for cropping mService's name in the logs
    public String c(Object objectName) {
        return objectName.toString().replace("com.example.antidepremdemo.", "");
    }

}//MainActivity

//TODO: txt_status text fill the TextView
//TODO: use a template fot the design

// Done:
//todo: save preferences of sensitivity.
//todo: increase the points of sensitivity seekbar
//Todo: remove onRetainCustomNonConfigurationInstance() and mService = (MediaService) getLastCustomNonConfigurationInstance()
// because there is no need to them.
//todo: "send Email" option
//todo: "Change tone" option
//todo: fix buttons under MoreFragment's layout are still clickable
//todo: turn btnMore into an edit icon with a context? menu that has a Change audio and send Email options
//todo: hide the title bar
//todo: edit the placeholder attribute tools:text="Active"
//todo: survive configuration changes
//todo: add a landscape layout
//todo: do I need to add wake lock even if I'm using a foreground service?
// Manage device awake state (wake lock). MediaPlayer.setWakeMode().
//todo: should I invoke abandonAudioFocus in onPause() mStop()?
//TODO: sync seekVolume with the device's original one.
//TODO: check on the MediaPlayer code in 1MAC's and Edraak's project.
//todo: requestAudioFocus
//todo try adding "android.permission.MODIFY_AUDIO_SETTINGS" to the manifest to use STREAM_SYSTEM.
// it didn't work.
//Todo: the audio is not playing as STREAM_SYSTEM, while seekVolume is controlling the volume as STREAM_SYSTEM properly.
// couldn't be done because Samsung has restricted the use of STREAM_SYSTEM audio stream type on some of its devices, including the Samsung Galaxy J7 Prime.


//Notes:
//Galaxy J7 Prime. 5.5" 1080x1920. Nougat 7 (API / SDK 24)