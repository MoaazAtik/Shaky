package com.thewhitewings.shaky;

import android.Manifest;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.TextViewCompat;
import androidx.lifecycle.ViewModelProvider;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TextSwitcher textSwitcher;
    private Button btnOn, btnOff;
    private TextView txtSensitivity, txtVolume;
    private SeekBar seekBarSensitivity, seekBarVolume;
    private ImageButton btnMore;
    private MotionLayout motionLayout;
    private MediaAndSensorViewModel mediaAndSensorViewModel;
    private ActivationState currentActivationState = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaAndSensorViewModel = new ViewModelProvider(this).get(MediaAndSensorViewModel.class);

        textSwitcher = findViewById(R.id.text_switcher);
        btnOn = findViewById(R.id.btn_on);
        btnOff = findViewById(R.id.btn_off);
        txtSensitivity = findViewById(R.id.txt_sensitivity);
        txtVolume = findViewById(R.id.txt_volume);
        seekBarSensitivity = findViewById(R.id.seek_sensitivity);
        seekBarVolume = findViewById(R.id.seek_volume);
        btnMore = findViewById(R.id.btn_more);
        motionLayout = findViewById(R.id.motion_layout);

        setupUiComponents();

        setupUiStateObserver();

        requestPermissions();
    }

    private void setupUiComponents() {
        textSwitcher.setFactory(textViewFactory);
        textSwitcher.setCurrentText(getString(R.string.status_inactive));

        btnOn.setOnClickListener(v -> mediaAndSensorViewModel.activate());

        btnOff.setEnabled(true);
        btnOff.setOnClickListener(v -> mediaAndSensorViewModel.deactivate());

        setTextGradientColor(txtSensitivity);
        setTextGradientColor(txtVolume);

        seekBarSensitivity.setOnSeekBarChangeListener(sensitivitySeekBarListener);

        seekBarVolume.setMax(mediaAndSensorViewModel.getVolumeMusicStreamMax());
        seekBarVolume.setOnSeekBarChangeListener(volumeSeekBarListener);
    }

    private void requestPermissions() {
        // Request POST_NOTIFICATIONS permission for the foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    0
            );
        }
    }

    private final ViewSwitcher.ViewFactory textViewFactory = new ViewSwitcher.ViewFactory() {
        @Override
        public View makeView() {
            // Specify TextView attributes
            AppCompatTextView textView = new AppCompatTextView(MainActivity.this);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setTypeface(ResourcesCompat.getFont(MainActivity.this, R.font.montserrat_semi_bold));

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
    };

    private void animate(ActivationState transition) {
        Animation animationIn = AnimationUtils.loadAnimation(this, R.anim.animation_in);
        Animation animationOut = AnimationUtils.loadAnimation(this, R.anim.animation_out);

        textSwitcher.setInAnimation(animationIn);
        textSwitcher.setOutAnimation(animationOut);

        if (transition == ActivationState.INITIALIZATION_TO_ACTIVE) { //app initiation (initial to active state)

            textSwitcher.setText(getString(R.string.status_active));

        } else if (transition == ActivationState.MANUAL_INACTIVE_TO_ACTIVE) { //to active state (inactive to active state)
            motionLayout.setTransition(R.id.inactive, R.id.active);
            motionLayout.transitionToEnd();

            textSwitcher.setText(getString(R.string.status_active));

        } else if (transition == ActivationState.MANUAL_ACTIVE_TO_INACTIVE) { //to inactive state (active to inactive state)
            motionLayout.setTransition(R.id.active, R.id.inactive);
            motionLayout.transitionToEnd();

            textSwitcher.setText(getString(R.string.status_inactive));
            AppCompatTextView textView = (AppCompatTextView) textSwitcher.getCurrentView();
            textView.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.montserrat_regular));
        }
    }


    private void setTextGradientColor(TextView textView) {
        int[] colors = {getResources().getColor(R.color.premium_white1),
                getResources().getColor(R.color.premium_white2),
                getResources().getColor(R.color.premium_white3),
                getResources().getColor(R.color.premium_white4)};

        float[] positions = {0, 0.31f, 0.75f, 1};

        Shader shader = new LinearGradient(0, textView.getTextSize(), 0, 0, colors, positions, Shader.TileMode.CLAMP);

        textView.getPaint().setShader(shader); //sets the shader (color) of the text
        textView.setTextColor(getResources().getColor(R.color.white)); //sets the color of the text initially or if the shader failed to render
    }

    private void setupUiStateObserver() {
        mediaAndSensorViewModel.getUiState().observe(this, uiState -> {

            ActivationState newActivationState = uiState.getActivationState();
            if (newActivationState != currentActivationState) {
                animate(newActivationState);
                currentActivationState = newActivationState;
            }
            seekBarSensitivity.setProgress(
                    seekBarSensitivity.getMax() - uiState.getSensitivityThreshold()
            );
            seekBarVolume.setProgress(uiState.getVolume());
        });
    }

    private final SeekBar.OnSeekBarChangeListener sensitivitySeekBarListener = new OnSeekBarChangeListenerImpl() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mediaAndSensorViewModel.updateSensitivityThreshold(
                    seekBarSensitivity.getMax() - progress
            );
        }
    };

    private final SeekBar.OnSeekBarChangeListener volumeSeekBarListener = new OnSeekBarChangeListenerImpl() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                if (mediaAndSensorViewModel.getUiState().getValue() == null) return;
                if (progress > mediaAndSensorViewModel.getUiState().getValue().getVolume())
                    mediaAndSensorViewModel.adjustVolume(
                            AudioManager.ADJUST_RAISE,
                            false
                    );
                else
                    mediaAndSensorViewModel.adjustVolume(
                            AudioManager.ADJUST_LOWER,
                            false
                    );
            }
        }
    };

    /*
    Note: Implemented adjusting volume by
     overriding onKeyDown and updating the UI state in onResume
     instead of using a BroadcastReceiver to be less Resource-Intensive.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            mediaAndSensorViewModel.adjustVolume(
                    AudioManager.ADJUST_RAISE,
                    true
            );
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mediaAndSensorViewModel.adjustVolume(
                    AudioManager.ADJUST_LOWER,
                    true
            );
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update the UI state with the current volume
        /*
        It is needed to make the volume seekbar update
         when user adjusts volume while app is in background.
         */
        mediaAndSensorViewModel.updateVolumeState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing())
            mediaAndSensorViewModel.deactivate();
    }
}