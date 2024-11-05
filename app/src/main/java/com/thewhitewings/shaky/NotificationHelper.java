package com.thewhitewings.shaky;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    private static final String CHANNEL_ID = "media_playback_channel";
    private final Context context;
    private final NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Media Playback",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Channel for media playback controls");
            notificationManager.createNotificationChannel(channel);
        }
    }

    public Notification buildNotification(String trackTitle) {
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Now Playing")
                .setContentText(trackTitle)
                .setSmallIcon(R.drawable.app_icon_notification)
                .setContentIntent(pendingIntent)
                .setOngoing(true) // Keeps it persistent
                .build();
    }


    public void updateNotification(String trackTitle) {
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Now Playing")
                .setContentText(trackTitle)
                .setSmallIcon(R.drawable.app_icon_notification)
                .setOngoing(true)
                .build();
        notificationManager.notify(1, notification);
    }
}
