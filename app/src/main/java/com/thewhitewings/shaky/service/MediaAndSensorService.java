package com.thewhitewings.shaky.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.thewhitewings.shaky.NotificationHandler;

/**
 * Foreground service for Media and Sensor
 */
public class MediaAndSensorService extends Service {

    private static final String TAG = "MediaAndSensorService";
    private NotificationHandler notificationHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationHandler = new NotificationHandler(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (Action.ACTIVATE.name().equals(action))
                startForeground(1, notificationHandler.buildNotification());
            else if (Action.DEACTIVATE.name().equals(action))
                stopSelf();
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     * Actions to interact with MediaAndSensorService
     */
    public enum Action {
        ACTIVATE, DEACTIVATE
    }
}