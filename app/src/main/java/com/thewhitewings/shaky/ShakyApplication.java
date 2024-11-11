package com.thewhitewings.shaky;

import android.app.Application;

import com.thewhitewings.shaky.data.ShakyPreferences;

public class ShakyApplication extends Application {

    private static final String TAG = "ShakyApplication";
    private ShakyPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        new NotificationHandler(this).createNotificationChannel();
        preferences = new ShakyPreferences(this);
    }

    public ShakyPreferences getPreferences() {
        return preferences;
    }
}