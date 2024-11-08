package com.thewhitewings.shaky;

import static com.thewhitewings.shaky.Constants.PREFERENCES_NAME;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.thewhitewings.shaky.data.ShakyPreferences;

public class ShakyApplication extends Application {

    private static final String TAG = "ShakyApplication";

    private ShakyPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        new NotificationHandler(this).createNotificationChannel();

        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        preferences = new ShakyPreferences(sharedPreferences);
    }

    public ShakyPreferences getPreferences() {
        return preferences;
    }
}