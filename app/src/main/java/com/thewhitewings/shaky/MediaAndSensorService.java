package com.thewhitewings.shaky;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class MediaAndSensorService extends Service {

    private static final String TAG = "MediaAndSensorService";
    private AudioFocusHelper audioFocusHelper;
    private NotificationHelper notificationHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        audioFocusHelper = new AudioFocusHelper(this);
        notificationHelper = new NotificationHelper(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if ("ACTION_PLAY".equals(action)) {
                startForeground(1, notificationHelper.buildNotification("Track Name"));
                if (audioFocusHelper.requestAudioFocus()) {
                    // Start media playback logic here
                }
                Log.d(TAG, "onStartCommand: "+intent.getAction());
            } else if ("ACTION_PAUSE".equals(action)) {
                // Pause media playback logic here
                Log.d(TAG, "onStartCommand: "+intent.getAction());
            } else if ("ACTION_STOP".equals(action)) {
                stopForeground(true);
                audioFocusHelper.releaseAudioFocus();
                // Stop media playback logic here
                Log.d(TAG, "onStartCommand: "+intent.getAction());
                stopSelf();
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        audioFocusHelper.releaseAudioFocus();
        // Stop media playback logic here
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    // Add other methods and logic as needed

    public void updateNotification(String trackTitle) {
        notificationHelper.updateNotification(trackTitle);
    }

    // Other media playback-related methods
}
