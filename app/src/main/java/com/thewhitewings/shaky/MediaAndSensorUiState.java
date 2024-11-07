package com.thewhitewings.shaky;

import androidx.annotation.NonNull;

public class MediaAndSensorUiState {

    private final ActivationState activationState;
    private final int sensitivityThreshold;
    private final int volume;

    public MediaAndSensorUiState(
            @NonNull ActivationState activationState,
            @NonNull Integer sensitivityThreshold,
            @NonNull Integer volume
    ) {
        this.activationState = activationState;
        this.sensitivityThreshold = sensitivityThreshold;
        this.volume = volume;
    }

    public ActivationState getActivationState() {
        return activationState;
    }

    public Integer getSensitivityThreshold() {
        return sensitivityThreshold;
    }

    public Integer getVolume() {
        return volume;
    }
}