package com.thewhitewings.shaky.data;

import static com.thewhitewings.shaky.Constants.DISPLAY_BATTERY_OPTIMIZATION_DIALOG_KEY;
import static com.thewhitewings.shaky.Constants.SENSITIVITY_THRESHOLD_KEY;
import static com.thewhitewings.shaky.Constants.ALARM_TONE_KEY;

import android.content.SharedPreferences;

public class ShakyPreferences {

    private final SharedPreferences preferences;

    public ShakyPreferences(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public boolean getBatteryOptimizationDialogPreference() {
        return preferences.getBoolean(DISPLAY_BATTERY_OPTIMIZATION_DIALOG_KEY, true);
    }

    public void updateBatteryOptimizationDialogPreference() {
        boolean showDialogPreference = false;
        preferences.edit()
                .putBoolean(DISPLAY_BATTERY_OPTIMIZATION_DIALOG_KEY, showDialogPreference)
                .apply();
    }

    public int getSensitivityThresholdPreference() {
        return preferences.getInt(SENSITIVITY_THRESHOLD_KEY, 0);
    }

    public void updateSensitivityThresholdPreference(int sensitivityThreshold) {
        preferences.edit()
                .putInt(SENSITIVITY_THRESHOLD_KEY, sensitivityThreshold)
                .apply();
    }

    public String getAlarmTonePreference(String defaultToneResource) {
        return preferences.getString(ALARM_TONE_KEY, defaultToneResource);
    }

    public void updateAlarmTonePreference(String tone) {
        preferences.edit()
                .putString(ALARM_TONE_KEY, tone)
                .apply();
    }
}