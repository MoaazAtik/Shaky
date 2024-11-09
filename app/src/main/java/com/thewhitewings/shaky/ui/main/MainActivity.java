package com.thewhitewings.shaky.ui.main;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.thewhitewings.shaky.OnSeekBarChangeListenerImpl;
import com.thewhitewings.shaky.R;
import com.thewhitewings.shaky.ShakyApplication;
import com.thewhitewings.shaky.Util;
import com.thewhitewings.shaky.databinding.ActivityMainBinding;
import com.thewhitewings.shaky.databinding.DialogBatteryOptimizationBinding;
import com.thewhitewings.shaky.databinding.DialogMoreInformationBinding;
import com.thewhitewings.shaky.service.MediaAndSensorService;
import com.thewhitewings.shaky.ui.more.MoreFragment;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private TextSwitcher textSwitcher;
    private MotionLayout motionLayout;
    private MediaAndSensorViewModel viewModel;
    private ActivationState currentActivationState = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ShakyApplication application = (ShakyApplication) getApplication();
        viewModel = new ViewModelProvider(
                this,
                new MediaAndSensorViewModelFactory(application, application.getPreferences())
        ).get(MediaAndSensorViewModel.class);

        setupUiComponents();
        setupUiStateObserver();

        requestPermissions();
        showBatteryOptimizationDialog();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    private void setupUiComponents() {
        textSwitcher = binding.textSwitcher;
        motionLayout = binding.motionLayout;

        textSwitcher.setFactory(textViewFactory);
        textSwitcher.setCurrentText(getString(R.string.status_inactive));

        binding.btnOn.setOnClickListener(v -> viewModel.activate());

        enableBtnOff();
        binding.btnOff.setOnClickListener(v -> viewModel.deactivate());

        setTextGradientColor(binding.txtSensitivity);
        setTextGradientColor(binding.txtVolume);

        binding.seekBarSensitivity.setOnSeekBarChangeListener(sensitivitySeekBarListener);

        binding.seekBarVolume.setMax(viewModel.getVolumeMusicStreamMax());
        binding.seekBarVolume.setOnSeekBarChangeListener(volumeSeekBarListener);

        binding.btnMore.setOnClickListener(v -> navigateToMoreFragment());
    }

    /**
     * Enable {@link ActivityMainBinding#btnOff} after The initial transition ends
     */
    private void enableBtnOff() {
        new Handler(Looper.getMainLooper()).postDelayed(
                () -> binding.btnOff.setEnabled(true),
                3000);
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

    private void showBatteryOptimizationDialog() {
        if (!viewModel.getBatteryOptimizationDialogPreference())
            return;

        Runnable runnable = () -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            // Initialize the view binding for the dialog layout
            DialogBatteryOptimizationBinding dialogBinding =
                    DialogBatteryOptimizationBinding.inflate(getLayoutInflater());

            // Set the custom view for the dialog using the binding's root view
            builder.setView(dialogBinding.getRoot());

            Dialog dialog = builder.create();
            dialog.show();

            // Show the device's specifications
            dialogBinding.txtSpecs.setText(Util.getDeviceSpecs());

            // Handle the positive button (btnFix) in the custom layout
            dialogBinding.btnFix.setOnClickListener(v -> openBatteryOptimizationGuide1());

            // Handle the negative button (btnCancel) in the custom layout
            dialogBinding.btnCancel.setOnClickListener(v -> {
                // Check if the dialog should show again
                if (dialogBinding.checkboxDontShowAgain.isChecked())
                    viewModel.updateBatteryOptimizationDialogPreference();

                dialog.dismiss();
            });

            // set OnDismissListener for the dialog
            /*
             onDismiss will be called when btnCancel, device's back button,
              or outside the dialog box is clicked.
             Eventually showMoreInformationDialog() is called in all situations
              even if btnFix is clicked.
             */
            dialog.setOnDismissListener(dismissedDialog -> showMoreInformationDialog());
        };

        // Show the dialog after a delay of 5 seconds for better UX
        new Handler(Looper.getMainLooper()).postDelayed(
                runnable,
                5000);
    }

    private void showMoreInformationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        DialogMoreInformationBinding dialogBinding =
                DialogMoreInformationBinding.inflate(getLayoutInflater());

        builder.setView(dialogBinding.getRoot());

        Dialog dialog = builder.create();
        dialog.show();

        // Handle the negative button (btnGotIt) in the custom layout
        dialogBinding.btnGotIt.setOnClickListener(v -> dialog.dismiss());
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
        viewModel.getUiState().observe(this, uiState -> {

            ActivationState newActivationState = uiState.getActivationState();
            if (newActivationState != currentActivationState) {
                animate(newActivationState);
                currentActivationState = newActivationState;
                if (newActivationState == ActivationState.MANUAL_ACTIVE_TO_INACTIVE)
                    stopService();
                else
                    startService();
            }

            binding.seekBarSensitivity.setProgress(
                    binding.seekBarSensitivity.getMax() - uiState.getSensitivityThreshold()
            );

            binding.seekBarVolume.setProgress(uiState.getVolume());
        });
    }

    private void startService() {
        Intent intent = new Intent(this, MediaAndSensorService.class);
        intent.setAction(MediaAndSensorService.Action.ACTIVATE.name());
        startService(intent);
    }

    private void stopService() {
        Intent intent = new Intent(this, MediaAndSensorService.class);
        intent.setAction(MediaAndSensorService.Action.DEACTIVATE.name());
        startService(intent);
    }

    private void openBatteryOptimizationGuide1() {
        Uri uri = Util.getBatteryOptimizationGuideUri1();

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private void navigateToMoreFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction
                // Animations. this has to be before fragmentTransaction.replace()
                .setCustomAnimations(
                        androidx.fragment.R.animator.fragment_fade_enter, // Enter animation
                        androidx.fragment.R.animator.fragment_fade_exit, // Exit animation
                        androidx.fragment.R.animator.fragment_close_enter, // Pop enter animation (when navigating back)
                        androidx.fragment.R.animator.fragment_fade_exit // Pop exit animation (when navigating back)
                )
                .replace(R.id.fragmentContainerView, new MoreFragment())
                .addToBackStack(null)
                .commit();
    }

    private final SeekBar.OnSeekBarChangeListener sensitivitySeekBarListener = new OnSeekBarChangeListenerImpl() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            viewModel.updateSensitivityThreshold(
                    binding.seekBarSensitivity.getMax() - progress
            );
        }
    };

    private final SeekBar.OnSeekBarChangeListener volumeSeekBarListener = new OnSeekBarChangeListenerImpl() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                if (viewModel.getUiState().getValue() == null) return;
                if (progress > viewModel.getUiState().getValue().getVolume())
                    viewModel.adjustVolume(
                            AudioManager.ADJUST_RAISE,
                            false
                    );
                else
                    viewModel.adjustVolume(
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
            viewModel.adjustVolume(
                    AudioManager.ADJUST_RAISE,
                    true
            );
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            viewModel.adjustVolume(
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
        viewModel.updateVolumeState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing())
            viewModel.deactivate();
    }
}