package com.thewhitewings.shaky.ui.main;

import android.app.Application;
import android.content.Context;
import android.os.PowerManager;

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
    public static final String WAKE_LOCK_TAG = "shaky:service_wake_lock_tag";

    private final MutableLiveData<MediaAndSensorUiState> uiState;
    private final Application context;
    private final ShakyPreferences preferences;
    private final SensorHandler sensorHandler;
    private final MediaHandler mediaHandler;
    private final PowerManager.WakeLock wakeLock;

    public MediaAndSensorViewModel(Application application, ShakyPreferences preferences) {
        super(application);
        context = application;
        this.preferences = preferences;
        sensorHandler = new SensorHandler(context);
        mediaHandler = new MediaHandler(context);
        uiState = new MutableLiveData<>(new MediaAndSensorUiState(
                ActivationState.INITIALIZATION_TO_ACTIVE,
                getSensitivityThresholdPreference(),
                getVolumeMusicStream()));
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);

        activate();
    }


    public LiveData<MediaAndSensorUiState> getUiState() {
        return uiState;
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

    public boolean getBatteryOptimizationDialogPreference() {
        // Check if the dialog should be shown based on the preferences
        return preferences.getBatteryOptimizationDialogPreference();
    }

    /**
     * Update the preferences to not show the dialog again
     */
    public void updateBatteryOptimizationDialogPreference() {
        preferences.updateBatteryOptimizationDialogPreference();
    }

    private int getSensitivityThresholdPreference() {
        return preferences.getSensitivityThresholdPreference();
    }

    public void updateSensitivityThreshold(int sensitivityThreshold) {
        preferences.updateSensitivityThresholdPreference(sensitivityThreshold);

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

    public void activate() {
        sensorHandler.activateSensor();
        sensorHandler.getIsShaking().observeForever(shakingObserver);
        // Acquire the wake lock for 12 hours
        wakeLock.acquire(12 * 60 * 60 * 1000L);

        if (uiState.getValue() == null) return;
        if (uiState.getValue().getActivationState() != ActivationState.INITIALIZATION_TO_ACTIVE)
            updateActivationState(ActivationState.MANUAL_INACTIVE_TO_ACTIVE);
    }

    public void deactivate() {
        sensorHandler.deactivateSensor();
        sensorHandler.getIsShaking().removeObserver(shakingObserver);
        mediaHandler.stopMedia();
        mediaHandler.releaseAudioFocus();
        // Release the wake lock
        if (wakeLock.isHeld())
            wakeLock.release();

        updateActivationState(ActivationState.MANUAL_ACTIVE_TO_INACTIVE);
    }


    private final Observer<Boolean> shakingObserver = new Observer<Boolean>() {
        @Override
        public void onChanged(@Nullable final Boolean isShaking) {
            if (Boolean.TRUE.equals(isShaking))
                mediaHandler.triggerAlarm();
        }
    };
}