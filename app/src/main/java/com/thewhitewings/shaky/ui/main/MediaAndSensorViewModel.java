package com.thewhitewings.shaky.ui.main;

import static com.thewhitewings.shaky.Constants.DISPLAY_BATTERY_OPTIMIZATION_DIALOG_KEY;
import static com.thewhitewings.shaky.Constants.PREFERENCES_NAME;
import static com.thewhitewings.shaky.Constants.SENSITIVITY_THRESHOLD_KEY;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.thewhitewings.shaky.service.MediaAndSensorService;
import com.thewhitewings.shaky.MediaHandler;
import com.thewhitewings.shaky.SensorHandler;

public class MediaAndSensorViewModel extends AndroidViewModel {

    private static final String TAG = "MediaAndSensorViewModel";

    private final MutableLiveData<MediaAndSensorUiState> uiState;
    private final SensorHandler sensorHandler;
    private final MediaHandler mediaHandler;
    private final Context context;

    public MediaAndSensorViewModel(Application application) {
        super(application);
//        context = application.getApplicationContext();
        context = application;
        sensorHandler = new SensorHandler(context);
        mediaHandler = new MediaHandler(context);
        uiState = new MutableLiveData<>(new MediaAndSensorUiState(
                ActivationState.INITIALIZATION_TO_ACTIVE,
                getSensitivityThresholdPreference(),
                getVolumeMusicStream()));

        activate();
    }

    public LiveData<MediaAndSensorUiState> getUiState() {
        return uiState;
    }

    private int getSensitivityThresholdPreference() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(SENSITIVITY_THRESHOLD_KEY, 0);
    }

    public void updateSensitivityThreshold(int sensitivityThreshold) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit()
                .putInt(SENSITIVITY_THRESHOLD_KEY, sensitivityThreshold)
                .apply();

        sensorHandler.updateSensitivityThreshold(sensitivityThreshold);

        // Update sensitivity threshold state
        if (uiState.getValue() == null) return;
        uiState.setValue(
                new MediaAndSensorUiState(
                        uiState.getValue().getActivationState(),
                        sensitivityThreshold,
                        uiState.getValue().getVolume()
                )
        );
    }

    public int getVolumeMusicStreamMax() {
        return mediaHandler.getVolumeMusicStreamMax();
    }

    public int getVolumeMusicStream() {
        return mediaHandler.getVolumeMusicStream();
    }

    public void adjustVolume(int direction, boolean fromDeviceVolumeKeys) {
        mediaHandler.adjustVolume(direction, fromDeviceVolumeKeys);

        updateVolumeState();
    }

    public void updateVolumeState() {
        if (uiState.getValue() == null) return;
        uiState.setValue(
                new MediaAndSensorUiState(
                        uiState.getValue().getActivationState(),
                        uiState.getValue().getSensitivityThreshold(),
                        getVolumeMusicStream()
                )
        );
    }

    private void updateActivationState(ActivationState activationState) {
        if (uiState.getValue() == null) return;
        uiState.setValue(
                new MediaAndSensorUiState(
                        activationState,
                        uiState.getValue().getSensitivityThreshold(),
                        uiState.getValue().getVolume()
                )
        );
    }

    public void activate() {
        Intent intent = new Intent(context, MediaAndSensorService.class);
        intent.setAction(MediaAndSensorService.Action.ACTIVATE.name());
        context.startService(intent);

        sensorHandler.activateSensor();
        sensorHandler.getIsShaking().observeForever(shakingObserver);

        if (uiState.getValue() == null) return;
        if (uiState.getValue().getActivationState() != ActivationState.INITIALIZATION_TO_ACTIVE)
            updateActivationState(ActivationState.MANUAL_INACTIVE_TO_ACTIVE);
    }

    public void deactivate() {
        Intent intent = new Intent(context, MediaAndSensorService.class);
        intent.setAction(MediaAndSensorService.Action.DEACTIVATE.name());
        context.startService(intent);

        sensorHandler.deactivateSensor();
        sensorHandler.getIsShaking().removeObserver(shakingObserver);
        mediaHandler.stopMedia();
        mediaHandler.releaseAudioFocus();

        updateActivationState(ActivationState.MANUAL_ACTIVE_TO_INACTIVE);
    }

    private final Observer<Boolean> shakingObserver = new Observer<Boolean>() {
        @Override
        public void onChanged(@Nullable final Boolean isShaking) {
            if (Boolean.TRUE.equals(isShaking))
                mediaHandler.triggerAlarm();
        }
    };

    public boolean getBatteryOptimizationDialogPreference() {
        // Check if the dialog should be shown based on the preference
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(DISPLAY_BATTERY_OPTIMIZATION_DIALOG_KEY, true);
    }

    /**
     * Update the preference to not show the dialog again
     */
    public void updateBatteryOptimizationDialogPreference() {
        boolean showDialogPreference = false;
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        preferences.edit()
                .putBoolean(DISPLAY_BATTERY_OPTIMIZATION_DIALOG_KEY, showDialogPreference)
                .apply();
    }
}