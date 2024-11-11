package com.thewhitewings.shaky.ui.main;

import android.app.Application;
import android.content.Context;
import android.media.AudioManager;
import android.os.PowerManager;

import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.thewhitewings.shaky.MediaHandler;
import com.thewhitewings.shaky.SensorHandler;
import com.thewhitewings.shaky.data.ShakyPreferences;

/**
 * ViewModel for the activity of the main screen
 */
public class MediaAndSensorViewModel extends AndroidViewModel {

    private static final String TAG = "MediaAndSensorViewModel";
    public static final String WAKE_LOCK_TAG = "shaky:service_wake_lock_tag";

    private final MutableLiveData<MediaAndSensorUiState> uiState;
    private final Application context;
    private final ShakyPreferences preferences;
    private final SensorHandler sensorHandler;
    private final MediaHandler mediaHandler;
    private final PowerManager.WakeLock wakeLock;

    /**
     * Constructor
     */
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


    /**
     * Get the current UI state
     */
    public LiveData<MediaAndSensorUiState> getUiState() {
        return uiState;
    }

    /**
     * Update the state of {@link ActivationState} with the given state
     */
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

    /**
     * Update the state of volume
     */
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

    /**
     * Check if the dialog should be shown based on the stored preferences
     *
     * @return true if the dialog should be shown, false otherwise
     */
    public boolean getBatteryOptimizationDialogPreference() {
        return preferences.getBatteryOptimizationDialogPreference();
    }

    /**
     * Update the preferences to not show the dialog again
     */
    public void updateBatteryOptimizationDialogPreference() {
        preferences.updateBatteryOptimizationDialogPreference();
    }

    /**
     * Get the sensitivity threshold preference
     */
    private int getSensitivityThresholdPreference() {
        return preferences.getSensitivityThresholdPreference();
    }

    /**
     * Update the sensitivity threshold of the sensor to trigger alarms
     * and store it in the preferences.
     * <br>
     * The change in acceleration must be greater than this value
     * so that the device is considered shaking.
     * <br>
     * The lower the value, the higher the sensitivity,
     * i.e., the sensor needs less vibration to trigger alarms.
     * <br>
     * The minimum value is 0 which is the default value, and the maximum value is 10.
     *
     * @see SensorHandler
     */
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

    /**
     * Get the maximum volume of the music stream
     */
    public int getVolumeMusicStreamMax() {
        return mediaHandler.getVolumeMusicStreamMax();
    }

    /**
     * Get the current volume of the music stream
     */
    public int getVolumeMusicStream() {
        return mediaHandler.getVolumeMusicStream();
    }

    /**
     * Adjust the volume of the music stream and update its state
     *
     * @param direction            {@link AudioManager#ADJUST_RAISE} to increase the volume,
     *                             {@link AudioManager#ADJUST_LOWER} to decrease the volume.
     * @param fromDeviceVolumeKeys true if the volume is adjusted from the device volume keys,
     *                             false if it is adjusted from the volume seekbar of the app.
     * @see MediaHandler#adjustVolume(int, boolean)
     */
    public void adjustVolume(int direction, boolean fromDeviceVolumeKeys) {
        mediaHandler.adjustVolume(direction, fromDeviceVolumeKeys);
        updateVolumeState();
    }

    /**
     * Activate the app by activating and observing the sensor, acquiring the wake lock,
     * and updating the state of activation state which triggers the service to start and UI to change.
     */
    public void activate() {
        sensorHandler.activateSensor();
        sensorHandler.getIsShaking().observeForever(shakingObserver);
        // Acquire the wake lock for 12 hours
        wakeLock.acquire(12 * 60 * 60 * 1000L);

        if (uiState.getValue() == null) return;
        if (uiState.getValue().getActivationState() != ActivationState.INITIALIZATION_TO_ACTIVE)
            updateActivationState(ActivationState.MANUAL_INACTIVE_TO_ACTIVE);
    }

    /**
     * Deactivate the app by deactivating the sensor, releasing the wake lock, and updating the
     * state of activation state which triggers the service to stop and UI to change.
     */
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


    /**
     * Observer for the shaking state of the sensor
     */
    private final Observer<Boolean> shakingObserver = new Observer<Boolean>() {
        @Override
        public void onChanged(@Nullable final Boolean isShaking) {
            if (Boolean.TRUE.equals(isShaking))
                mediaHandler.triggerAlarm();
        }
    };
}