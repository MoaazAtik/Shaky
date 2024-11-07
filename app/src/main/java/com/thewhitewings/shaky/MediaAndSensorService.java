package com.thewhitewings.shaky;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class MediaAndSensorService extends Service {

    private static final String TAG = "MediaAndSensorService";
    private NotificationHelper notificationHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationHelper = new NotificationHelper(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (Action.ACTIVATE.name().equals(action))
                startForeground(1, notificationHelper.buildNotification());
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

    public enum Action {
        ACTIVATE, DEACTIVATE
    }
}