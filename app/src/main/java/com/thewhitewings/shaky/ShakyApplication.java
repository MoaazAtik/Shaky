package com.thewhitewings.shaky;

import android.app.Application;

public class ShakyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        new NotificationHelper(this).createNotificationChannel();
    }
}