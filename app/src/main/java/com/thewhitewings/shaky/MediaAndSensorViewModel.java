package com.thewhitewings.shaky;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

public class MediaAndSensorViewModel extends AndroidViewModel {

    private static final String TAG = "MediaAndSensorViewModel";
    public static final String PREFERENCES_NAME = "app_preferences";
    public static final String SENSITIVITY_THRESHOLD_KEY = "sensitivity_threshold";

    private final MutableLiveData<MediaAndSensorUiState> uiState;
    private final SensorHelper sensorHelper;
    private final AudioFocusHelper audioFocusHelper;
    private final Context context;

    public MediaAndSensorViewModel(Application application) {
        super(application);
//        context = application.getApplicationContext();
        context = application;
        sensorHelper = new SensorHelper(context);
        audioFocusHelper = new AudioFocusHelper(context);
        uiState = new MutableLiveData<>(new MediaAndSensorUiState(
                ActivationState.INITIALIZATION_TO_ACTIVE,
                getSensitivityThresholdPreference(),
                10));

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

        sensorHelper.updateSensitivityThreshold(sensitivityThreshold);

        uiState.setValue(
                new MediaAndSensorUiState(
                        uiState.getValue().getActivationState(),
                        sensitivityThreshold,
                        uiState.getValue().getVolume())
            );
    }

    public void activate() {
        Intent intent = new Intent(context, MediaAndSensorService.class);
        intent.setAction(MediaAndSensorService.Action.ACTIVATE.name());
        context.startService(intent);

        sensorHelper.activateSensor();
        sensorHelper.getIsShaking().observeForever(shakingObserver);

        if (uiState.getValue().getActivationState() != ActivationState.INITIALIZATION_TO_ACTIVE)
            uiState.setValue(
                new MediaAndSensorUiState(
                        ActivationState.MANUAL_INACTIVE_TO_ACTIVE,
                        uiState.getValue().getSensitivityThreshold(),
                        uiState.getValue().getVolume()
                )
            );
    }

    public void deactivate() {
        Intent intent = new Intent(context, MediaAndSensorService.class);
        intent.setAction(MediaAndSensorService.Action.DEACTIVATE.name());
        context.startService(intent);

        sensorHelper.deactivateSensor();
        sensorHelper.getIsShaking().removeObserver(shakingObserver);
        audioFocusHelper.stopMedia();
        audioFocusHelper.releaseAudioFocus();

        uiState.setValue(
                new MediaAndSensorUiState(
                        ActivationState.MANUAL_ACTIVE_TO_INACTIVE,
                        uiState.getValue().getSensitivityThreshold(),
                        uiState.getValue().getVolume()
                )
        );
    }

    private final Observer<Boolean> shakingObserver = new Observer<Boolean>() {
        @Override
        public void onChanged(@Nullable final Boolean isShaking) {
            if (Boolean.TRUE.equals(isShaking))
                audioFocusHelper.triggerAlarm();
        }
    };

}