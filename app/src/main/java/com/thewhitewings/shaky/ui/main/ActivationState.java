package com.thewhitewings.shaky.ui.main;

/**
 * Enum for activation states of the app's service and sensor.
 * <br>
 * It reflects the state of the app service and weather it's been activated or deactivated
 * automatically on app initialization or manually by the user.
 */
public enum ActivationState {

    /**
     * The app is initializing to the active state.
     */
    INITIALIZATION_TO_ACTIVE,

    /**
     * The activation state is changing from active to inactive.
     */
    MANUAL_ACTIVE_TO_INACTIVE,

    /**
     * The activation state is changing from inactive to active.
     */
    MANUAL_INACTIVE_TO_ACTIVE
}