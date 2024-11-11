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
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.motion.widget.TransitionAdapter;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.thewhitewings.shaky.ui.util.OnSeekBarChangeListenerImpl;
import com.thewhitewings.shaky.R;
import com.thewhitewings.shaky.ShakyApplication;
import com.thewhitewings.shaky.Util;
import com.thewhitewings.shaky.databinding.ActivityMainBinding;
import com.thewhitewings.shaky.databinding.DialogBatteryOptimizationBinding;
import com.thewhitewings.shaky.databinding.DialogMoreInformationBinding;
import com.thewhitewings.shaky.service.MediaAndSensorService;
import com.thewhitewings.shaky.ui.more.MoreFragment;

/**
 * The main activity of the app.
 * It represents the main screen of the app.
 */
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

    /**
     * Initialize and set up UI components
     */
    private void setupUiComponents() {
        textSwitcher = binding.textSwitcher;
        motionLayout = binding.motionLayout;

        textSwitcher.setFactory(textViewFactory);
        textSwitcher.setCurrentText(getString(R.string.status_inactive));
        motionLayout.setTransitionListener(transitionListener);

        binding.btnOn.setOnClickListener(v -> viewModel.activate());
        binding.btnOff.setOnClickListener(v -> viewModel.deactivate());

        setTextGradientColor(binding.txtSensitivity);
        setTextGradientColor(binding.txtVolume);

        binding.seekBarSensitivity.setOnSeekBarChangeListener(sensitivitySeekBarListener);

        binding.seekBarVolume.setMax(viewModel.getVolumeMusicStreamMax());
        binding.seekBarVolume.setOnSeekBarChangeListener(volumeSeekBarListener);

        binding.btnMore.setOnClickListener(v -> navigateToMoreFragment());
    }

    /**
     * Set up the UI state observer
     */
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

    /**
     * Request {@link Manifest.permission#POST_NOTIFICATIONS POST_NOTIFICATIONS} permission
     * for the foreground service for Android API 33+
     */
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    0
            );
        }
    }

    /**
     * Show the dialog with information about the battery optimization if the user has not opted out.
     * It shows the device's specifications and directs the user to a step-by-step guide to follow
     * the instructions that are specific to their device to overcome app restrictions
     * that may be caused by the system's battery optimization configurations.
     * Those instructions in the guide can be implemented only manually by the user.
     */
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

    /**
     * Show the dialog to direct the user to the notes section in the app for more information
     * about the battery optimization, its impact on the app, and guides on how to overcome it.
     */
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

    /**
     * Animate and modify the UI components based on the activation state change of the app service.
     *
     * @param transition The {@link ActivationState} change of the app.
     */
    private void animate(ActivationState transition) {
        Animation animationIn = AnimationUtils.loadAnimation(this, R.anim.animation_in);
        Animation animationOut = AnimationUtils.loadAnimation(this, R.anim.animation_out);

        textSwitcher.setInAnimation(animationIn);
        textSwitcher.setOutAnimation(animationOut);

        // App initiation to active state
        if (transition == ActivationState.INITIALIZATION_TO_ACTIVE) {
            textSwitcher.setText(getString(R.string.status_active));

            // Manual state change from inactive to active state
        } else if (transition == ActivationState.MANUAL_INACTIVE_TO_ACTIVE) {
            motionLayout.setTransition(R.id.inactive, R.id.active);
            motionLayout.transitionToEnd();

            textSwitcher.setText(getString(R.string.status_active));

            // Manual state change from active to inactive state
        } else if (transition == ActivationState.MANUAL_ACTIVE_TO_INACTIVE) {
            motionLayout.setTransition(R.id.active, R.id.inactive);
            motionLayout.transitionToEnd();

            textSwitcher.setText(getString(R.string.status_inactive));
            AppCompatTextView textView = (AppCompatTextView) textSwitcher.getCurrentView();
            textView.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.montserrat_regular));
        }
    }

    /**
     * Set the text gradient color for the text of the given TextView.
     *
     * @param textView The TextView whose text gradient color is to be set.
     */
    private void setTextGradientColor(TextView textView) {
        int[] colors = {getResources().getColor(R.color.premium_white1),
                getResources().getColor(R.color.premium_white2),
                getResources().getColor(R.color.premium_white3),
                getResources().getColor(R.color.premium_white4)};

        float[] positions = {0, 0.31f, 0.75f, 1};

        Shader shader = new LinearGradient(0, textView.getTextSize(), 0, 0, colors, positions, Shader.TileMode.CLAMP);

        // Apply the shader (color) to the text
        textView.getPaint().setShader(shader);

        // Set the text color initially or if the shader failed to render
        textView.setTextColor(getResources().getColor(R.color.white));
    }


    /**
     * Start the {@link MediaAndSensorService}
     */
    private void startService() {
        Intent intent = new Intent(this, MediaAndSensorService.class);
        intent.setAction(MediaAndSensorService.Action.ACTIVATE.name());
        startService(intent);
    }

    /**
     * Stop the {@link MediaAndSensorService}
     */
    private void stopService() {
        Intent intent = new Intent(this, MediaAndSensorService.class);
        intent.setAction(MediaAndSensorService.Action.DEACTIVATE.name());
        startService(intent);
    }

    /**
     * Open the website of the first guide of handling app restrictions
     * caused by system's battery optimization.
     *
     * @see #showBatteryOptimizationDialog()
     */
    private void openBatteryOptimizationGuide1() {
        Uri uri = Util.getBatteryOptimizationGuideUri1();

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Opening guide website 1 failed: ", e);
        }
    }

    /**
     * Navigate to the {@link MoreFragment} for more actions and information about the app
     */
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


    /**
     * A {@link ViewSwitcher.ViewFactory} for the {@link TextSwitcher}
     * to handle and customize its TextView.
     */
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

    /**
     * A {@link SeekBar.OnSeekBarChangeListener} for the sensitivity seekbar.
     */
    private final SeekBar.OnSeekBarChangeListener sensitivitySeekBarListener = new OnSeekBarChangeListenerImpl() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            viewModel.updateSensitivityThreshold(
                    binding.seekBarSensitivity.getMax() - progress
            );
        }
    };

    /**
     * A {@link SeekBar.OnSeekBarChangeListener} for the volume seekbar.
     */
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

    /**
     * A {@link MotionLayout.TransitionListener} for the {@link MotionLayout}.
     */
    private final MotionLayout.TransitionListener transitionListener = new TransitionAdapter() {
        @Override
        public void onTransitionStarted(MotionLayout motionLayout, int startId, int endId) {
            binding.btnMore.setEnabled(false);
            binding.btnOff.setEnabled(false);
            binding.btnOn.setEnabled(false);
        }

        @Override
        public void onTransitionCompleted(MotionLayout motionLayout, int currentId) {
            binding.btnMore.setEnabled(true);
            binding.btnOff.setEnabled(true);
            binding.btnOn.setEnabled(true);
        }
    };


    /*
    Note: Implemented adjusting volume by
     overriding onKeyDown and updating the UI state in onResume
     instead of using a BroadcastReceiver to be less Resource-Intensive.
     */
    // Handle hardware volume-up and volume-down buttons
    // to adjust volume and volume seekbar accordingly
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
        // Deactivate the app service when the activity is destroyed by closing the app
        if (isFinishing())
            viewModel.deactivate();
    }
}