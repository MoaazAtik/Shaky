package com.thewhitewings.vibro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextPaint;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;


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

    public static MediaService mService;
    public Boolean mIsBound = false;

    private MotionLayout motionLayout;

    private TextSwitcher textSwitcher;

    private static Context contex;
    public static Context getContex() {
        return contex; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: ");

        contex = getApplicationContext();

        btnOn = findViewById(R.id.btn_on);
        btnOff = findViewById(R.id.btn_off);
        seekSensitivity = findViewById(R.id.seek_sensitivity);
        seekVolume = findViewById(R.id.seek_volume);
        btnMore = findViewById(R.id.btn_more);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        motionLayout = findViewById(R.id.motion_layout);

        textSwitcher = (TextSwitcher) findViewById(R.id.text_switcher);
        textSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView textView = new TextView(MainActivity.this);
                setTextGradientColor(textView);
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                textView.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.montserrat_semi_bold));
                return textView;
            }
        });
        textSwitcher.setCurrentText(getString(R.string.status_inactive));

        TextView txtSensitivity = findViewById(R.id.txt_sensitivity);
        TextView txtVolume = findViewById(R.id.txt_volume);
        setTextGradientColor(txtSensitivity);
        setTextGradientColor(txtVolume);

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
//            Log.d(TAG, "onCreate: 1" + "savedInstanceState..."+" "+c(mService)+" "+mIsBound);
            mOn(true, 0);
        } else if (savedInstanceState.getBoolean("mIsBound") == true) {
            mOn(false, 0);
//            Log.d(TAG, "onCreate: 21" +" "+c(mService)+" "+mIsBound);
        } else { //savedInstanceState.getBoolean("mIsBound") == false
//            Log.d(TAG, "onCreate: 22" +" "+c(mService)+" "+mIsBound);
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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        seekSensitivity.setProgress(
                seekSensitivity.getMax() - sharedPreferences.getInt("sensitivity_cutoff", 0) );

        seekSensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//                sensitivityCutoff = (9 - (i * 4)) / 2;
                sensitivityCutoff = seekSensitivity.getMax() - i;

                sharedPreferences.edit().putInt("sensitivity_cutoff", sensitivityCutoff).apply();
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

    private void setTextGradientColor(TextView textView) {

        TextPaint paint = textView.getPaint();
        float width = paint.measureText(textView.getText().toString());
        int colors[] = { getResources().getColor(R.color.premium_white1),
                getResources().getColor(R.color.premium_white2),
                getResources().getColor(R.color.premium_white3),
                getResources().getColor(R.color.premium_white4) };

        float positions[] = {0, 0.31f, 0.75f, 1};

        Shader shader = new LinearGradient(0, textView.getTextSize(), 0, 0, colors,
                positions, Shader.TileMode.CLAMP);

        textView.getPaint().setShader(shader); //sets the shader (color) of the text
        textView.setTextColor(getResources().getColor(R.color.white)); //sets the color of the text initially or if the shader failed to render
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("mIsBound", mIsBound);
    }


    @Override
    protected void onResume() {
        super.onResume();
//        Log.d(TAG, "onResume: " + mIsBound+" "+c(mService));
    }//onResume

    @Override
    protected void onPause() {
        super.onPause();
//        Log.d(TAG, "onPause: " + mIsBound+" "+c(mService));
    }//onPause

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
//        Log.d(TAG, "onDestroy: " + mIsBound+" "+c(mService));
        if (isFinishing()) {
            mOff(); //especially for unregisterListener()
        }

        if (mIsBound) {
            unbindService(serviceConnection);
            mIsBound = false;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            seekVolume.setProgress((seekVolume.getProgress() + 1 > seekVolume.getMax()) ?
                    seekVolume.getMax() : mService.audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
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

            if (firstStartingService) {
                startService();
            } else {
                bindService();
            }

            animate(transitionAnimation);

        }

    }//mOn

    private void animate(int transition) {

        Animation animationIn = AnimationUtils.loadAnimation(this, R.anim.animation_in);
        Animation animationOut = AnimationUtils.loadAnimation(this, R.anim.animation_out);

        textSwitcher.setInAnimation(animationIn);
        textSwitcher.setOutAnimation(animationOut);

        if (transition == 0) { //app initiation (initial to active state)

            textSwitcher.setText(getString(R.string.status_active));
            TextView textView = (TextView) textSwitcher.getCurrentView();
            textView.setTextSize(84);
            textView.setAllCaps(true);
            setTextGradientColor(textView);

        } else if (transition == 1) { //to active state (inactive to active state)
            motionLayout.setTransition(R.id.inactive, R.id.active);
            motionLayout.transitionToEnd();

            textSwitcher.setText(getString(R.string.status_active));
            TextView textView = (TextView) textSwitcher.getCurrentView();
            textView.setTextSize(84);
            textView.setAllCaps(true);
            setTextGradientColor(textView);

        } else if (transition == 2) { //to inactive state (active to inactive state)
            motionLayout.setTransition(R.id.active, R.id.inactive);
            motionLayout.transitionToEnd();

            textSwitcher.setText(getString(R.string.status_inactive));
            TextView textView = (TextView) textSwitcher.getCurrentView();
            textView.setTextSize(72);
            textView.setAllCaps(true);
            setTextGradientColor(textView);
            textView.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.montserrat_regular));
        }

    }//animate()

    private void mOff() {

        if (mIsBound) {

            sensorManager.unregisterListener(sensorEventListener);

            unbindService(serviceConnection);
//            Log.d(TAG, "mOff: after unbindService " + mIsBound+" "+c(mService));

            stopService(new Intent(this, MediaService.class));
//            Log.d(TAG, "mOff: afterstopService " + mIsBound+" "+c(mService));

            animate(2);

            mIsBound = false;

        }
    }//mOff

    private void startService() {
        Intent serviceIntent = new Intent(this, MediaService.class);
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

            MediaService.MyBinder binder = (MediaService.MyBinder) service;
            mService = binder.getService();
            mIsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsBound = false;

        }
    };

    //helper method for cropping mService's name in the logs
//    public String c(Object objectName) {
//        return objectName.toString().replace("com.thewhitewings.vibro.", "");
//    }

}//MainActivity
