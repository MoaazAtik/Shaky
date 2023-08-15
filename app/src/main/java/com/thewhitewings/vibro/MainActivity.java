package com.thewhitewings.vibro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Button btnOn, btnOff;
    private SeekBar seekSensitivity, seekVolume;
    private Button btnMore;

    public static MediaAndSensorService mService;
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

        motionLayout = findViewById(R.id.motion_layout);

        textSwitcher = (TextSwitcher) findViewById(R.id.text_switcher);
        textSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                AppCompatTextView textView = new AppCompatTextView(MainActivity.this);
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                textView.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.montserrat_semi_bold));

                textView.setAllCaps(true);
                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                        textView, 10, 120, 1,
                        TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM
                );
                textView.setLayoutParams(new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
                ));
                textView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        setTextGradientColor(textView);
                    }
                });

                return textView;
            }
        });
        textSwitcher.setCurrentText(getString(R.string.status_inactive));

        TextView txtSensitivity = findViewById(R.id.txt_sensitivity);
        TextView txtVolume = findViewById(R.id.txt_volume);
        setTextGradientColor(txtSensitivity);
        setTextGradientColor(txtVolume);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);


        //to avoid reinitializing a new service when configurations change (e.g. screen rotation)
        if (savedInstanceState == null) {
            mService = new MediaAndSensorService();
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
                mService.sensitivityCutoff = seekSensitivity.getMax() - i;

                sharedPreferences.edit().putInt("sensitivity_cutoff", mService.sensitivityCutoff).apply();
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

        // Check battery optimization status and show a message if not disabled
        checkBatteryOptimization();

    }//onCreate

    private void setTextGradientColor(TextView textView) {

        int[] colors = { getResources().getColor(R.color.premium_white1),
                getResources().getColor(R.color.premium_white2),
                getResources().getColor(R.color.premium_white3),
                getResources().getColor(R.color.premium_white4) };

        float[] positions = {0, 0.31f, 0.75f, 1};

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
        Log.d(TAG, "onResume: ");
    }//onResume

    @Override
    protected void onPause() {
        super.onPause();
//        Log.d(TAG, "onPause: " + mIsBound+" "+c(mService));
        Log.d(TAG, "onPause: ");
    }//onPause

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
//        Log.d(TAG, "onDestroy: " + mIsBound+" "+c(mService));
        if (isFinishing()) {
            Log.d(TAG, "onDestroy: isFinishing()");
            mOff(); //especially for unregisterListener()
        }

        if (mIsBound) {
            Log.d(TAG, "onDestroy: if(mIsBound)");
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


    private void mOn(boolean firstStartingService, int transitionAnimation) {

        if (!mIsBound) {

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

        } else if (transition == 1) { //to active state (inactive to active state)
            motionLayout.setTransition(R.id.inactive, R.id.active);
            motionLayout.transitionToEnd();

            textSwitcher.setText(getString(R.string.status_active));

        } else if (transition == 2) { //to inactive state (active to inactive state)
            motionLayout.setTransition(R.id.active, R.id.inactive);
            motionLayout.transitionToEnd();

            textSwitcher.setText(getString(R.string.status_inactive));
            AppCompatTextView textView = (AppCompatTextView) textSwitcher.getCurrentView();
            textView.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.montserrat_regular));
        }

    }//animate()

    private void mOff() {
        Log.d(TAG, "mOff: ");

        if (mIsBound) {

            unbindService(serviceConnection);
//            Log.d(TAG, "mOff: after unbindService " + mIsBound+" "+c(mService));

            stopService(new Intent(this, MediaAndSensorService.class));
//            Log.d(TAG, "mOff: after stopService " + mIsBound+" "+c(mService));

            animate(2);

            mIsBound = false;

        }
    }//mOff

    private void startService() {
        Intent serviceIntent = new Intent(this, MediaAndSensorService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
        Log.d(TAG, "startService: my method");
        bindService();
    }
    private void bindService() {
        Intent serviceBindIntent = new Intent(this, MediaAndSensorService.class);
        bindService(serviceBindIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "bindService: ");
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            MediaAndSensorService.MyBinder binder = (MediaAndSensorService.MyBinder) service;
            mService = binder.getService();
            mIsBound = true;
            Log.d(TAG, "onServiceConnected: ");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsBound = false;
            Log.d(TAG, "onServiceDisconnected: ");
        }
    };

    private void checkBatteryOptimization() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean isIgnoringBatteryOptimizations = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            isIgnoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(getPackageName());
        }

        if (!isIgnoringBatteryOptimizations) {
            // Battery optimization is not disabled, show a message
            showBatteryOptimizationDialog();
        }
    }

    private void showBatteryOptimizationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Battery Optimization");
        builder.setMessage(
                "To ensure proper app functionality in the background, please disable battery optimization for this app." + "\n\n" +
                        "To fix it, please follow the provided steps");

        // Inflate a custom layout for the dialog content
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_battery_optimization, null);
        builder.setView(dialogView);

        // Find the checkbox in the custom layout
        CheckBox dontShowAgainCheckbox = dialogView.findViewById(R.id.checkbox_dont_show_again);

        String manufacturer = android.os.Build.MANUFACTURER.toUpperCase();
        String versionName = getVersionName();
        String versionRelease = Build.VERSION.RELEASE;
        String versionSDKLevel = String.valueOf(Build.VERSION.SDK_INT);
        Log.d(TAG, "openBatteryOptimizationWebsite: " +
                "Manufacturer: " + manufacturer +
                ", Version name: " + getVersionName() +
                ", Release: " + versionRelease +
                ", SDK: " + versionSDKLevel);
        TextView txtSpecs = dialogView.findViewById(R.id.txt_specs);
        txtSpecs.setText("Apply the steps related to your phone specifications:\n\n" +
                manufacturer + " • Android " + versionName + " " + versionRelease + " (SDK Level " + versionSDKLevel + ")");


//            builder.setPositiveButton("Open Battery Settings", new DialogInterface.OnClickListener() {
        builder.setPositiveButton("Let's fix it", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                openBatterySettings();
                openBatteryOptimizationWebsite();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.setCancelable(false);


        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (dontShowAgainCheckbox.isChecked()) {
                    // Save a preference to not show the dialog again
                    SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    preferences.edit().putBoolean("dontShowBatteryDialog", true).apply();
                }
            }
        });

        // Check if the dialog should be shown based on the preference
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean shouldShowDialog = !preferences.getBoolean("dontShowBatteryDialog", false);

        if (shouldShowDialog) {
            builder.show();
        }
    }

//    private void openBatterySettings() {
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
//            startActivity(intent);
//        }
//    }

    private void openBatteryOptimizationWebsite() {
        String manufacturer = android.os.Build.MANUFACTURER.toLowerCase();

        String websiteUrl = "https://dontkillmyapp.com/" + manufacturer;

        Uri uri = Uri.parse(websiteUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private String getVersionName() {
        String versionRelease = Build.VERSION.RELEASE;

        switch (versionRelease) {
            case "5.0":
                return "Lollipop";
            case "5.1":
                return "Lollipop MR1";
            case "6.0":
                return "Marshmallow";
            case "7.0":
                return "Nougat";
            case "7.1":
                return "Nougat MR1";
            case "8.0":
                return "Oreo";
            case "8.1":
                return "Oreo MR1";
            case "9":
                return "Pie";
            case "10":
                return "Q";
            case "11":
                return "R";
            default:
                return "Unknown";
        }
    }


    //helper method for cropping mService's name in the logs
//    public String c(Object objectName) {
//        return objectName.toString().replace("com.thewhitewings.vibro.", "");
//    }

}//MainActivity
