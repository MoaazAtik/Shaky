package com.thewhitewings.shaky.ui.main;

import android.app.Application;

import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.thewhitewings.shaky.MediaHandler;
import com.thewhitewings.shaky.SensorHandler;
import com.thewhitewings.shaky.data.ShakyPreferences;

public class MediaAndSensorViewModel extends AndroidViewModel {

    private static final String TAG = "MediaAndSensorViewModel";

    private final ShakyPreferences preference;
    private final MutableLiveData<MediaAndSensorUiState> uiState;
    private final SensorHandler sensorHandler;
    private final MediaHandler mediaHandler;
    private final Application context;

    public MediaAndSensorViewModel(Application application, ShakyPreferences preference) {
        super(application);
        context = application;
        this.preference = preference;
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
        return preference.getSensitivityThresholdPreference();
    }

    public void updateSensitivityThreshold(int sensitivityThreshold) {
        preference.updateSensitivityThresholdPreference(sensitivityThreshold);

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
        sensorHandler.activateSensor();
        sensorHandler.getIsShaking().observeForever(shakingObserver);

        if (uiState.getValue() == null) return;
        if (uiState.getValue().getActivationState() != ActivationState.INITIALIZATION_TO_ACTIVE)
            updateActivationState(ActivationState.MANUAL_INACTIVE_TO_ACTIVE);
    }

    public void deactivate() {
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
        return preference.getBatteryOptimizationDialogPreference();
    }

    /**
     * Update the preference to not show the dialog again
     */
    public void updateBatteryOptimizationDialogPreference() {
        preference.updateBatteryOptimizationDialogPreference();
    }
}