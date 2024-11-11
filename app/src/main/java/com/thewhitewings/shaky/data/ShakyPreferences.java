package com.thewhitewings.shaky.data;

import static com.thewhitewings.shaky.Constants.ALARM_TONE_KEY;
import static com.thewhitewings.shaky.Constants.DISPLAY_BATTERY_OPTIMIZATION_DIALOG_KEY;
import static com.thewhitewings.shaky.Constants.PREFERENCES_NAME;
import static com.thewhitewings.shaky.Constants.SENSITIVITY_THRESHOLD_KEY;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * A class that interacts with the SharedPreferences of the app and stores user preferences
 */
public class ShakyPreferences {

    private static final String TAG = "ShakyPreferences";
    private final SharedPreferences preferences;

    /**
     * Constructor
     *
     * @param context The application context
     */
    public ShakyPreferences(Context context) {
        preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Get the preference of whether to display the battery optimization info dialog on app startup
     *
     * @return True if the dialog should be displayed, false otherwise
     */
    public boolean getBatteryOptimizationDialogPreference() {
        return preferences.getBoolean(DISPLAY_BATTERY_OPTIMIZATION_DIALOG_KEY, true);
    }

    /**
     * Update the preference of whether to display the battery optimization info dialog
     * on app startup to false, i.e., don't show it again
     */
    public void updateBatteryOptimizationDialogPreference() {
        boolean showDialogPreference = false;
        preferences.edit()
                .putBoolean(DISPLAY_BATTERY_OPTIMIZATION_DIALOG_KEY, showDialogPreference)
                .apply();
    }

    /**
     * Get the sensitivity threshold preference
     */
    public int getSensitivityThresholdPreference() {
        return preferences.getInt(SENSITIVITY_THRESHOLD_KEY, 0);
    }

    /**
     * Update the sensitivity threshold preference with the given value
     */
    public void updateSensitivityThresholdPreference(int sensitivityThreshold) {
        preferences.edit()
                .putInt(SENSITIVITY_THRESHOLD_KEY, sensitivityThreshold)
                .apply();
    }

    /**
     * Get the alarm tone preference
     *
     * @param defaultToneResource The default alarm tone resource URI as a string to be used
     *                            if no preference is set, i.e., the alarm tone has not been changed.
     * @return The URI as a string of the saved alarm tone preference,
     * or the default tone if no preference is set.
     */
    public String getAlarmTonePreference(String defaultToneResource) {
        return preferences.getString(ALARM_TONE_KEY, defaultToneResource);
    }

    /**
     * Update the alarm tone preference with the given tone URI as a string
     */
    public void updateAlarmTonePreference(String tone) {
        preferences.edit()
                .putString(ALARM_TONE_KEY, tone)
                .apply();
    }
}