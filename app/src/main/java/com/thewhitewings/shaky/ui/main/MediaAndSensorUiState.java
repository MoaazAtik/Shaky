package com.thewhitewings.shaky.ui.main;

import androidx.annotation.NonNull;

/**
 * Represents the properties of the UI state of the app's main screen
 */
public class MediaAndSensorUiState {

    /**
     * The state of the app service and sensor activation
     *
     * @see ActivationState
     */
    private final ActivationState activationState;

    /**
     * The sensitivity threshold of the sensor to trigger alarms.
     * <br>
     * The lower the value, the higher the sensitivity.
     * @see com.thewhitewings.shaky.SensorHandler
     */
    private final int sensitivityThreshold;

    /**
     * The volume of the alarm
     */
    private final int volume;

    /**
     * Constructor
     */
    public MediaAndSensorUiState(
            @NonNull ActivationState activationState,
            @NonNull Integer sensitivityThreshold,
            @NonNull Integer volume
    ) {
        this.activationState = activationState;
        this.sensitivityThreshold = sensitivityThreshold;
        this.volume = volume;
    }

    /**
     * Returns the state of the app service and sensor activation
     */
    public ActivationState getActivationState() {
        return activationState;
    }

    /**
     * Returns the sensitivity threshold of the sensor to trigger alarms.
     * <br>
     * The lower the value, the higher the sensitivity.
     */
    public Integer getSensitivityThreshold() {
        return sensitivityThreshold;
    }

    /**
     * Returns the volume of the alarm
     */
    public Integer getVolume() {
        return volume;
    }
}