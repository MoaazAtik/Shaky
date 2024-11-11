package com.thewhitewings.shaky;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.thewhitewings.shaky.ui.main.MainActivity;

/**
 * Class that handles the notifications for the app
 */
public class NotificationHandler {

    /**
     * The id of the notification channel
     */
    private static final String CHANNEL_ID = "media_and_sensor_service_channel";
    private final Context context;
    private final NotificationManager notificationManager;

    /**
     * Constructor
     */
    public NotificationHandler(Context context) {
        this.context = context;
        notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
    }


    /**
     * Create a notification channel for the foreground service of the app
     */
    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.alarm_notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription(context.getString(R.string.alarm_notification_channel_description));
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Build the notification for the foreground service of the app
     */
    public Notification buildNotification() {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(context.getString(R.string.alarm_notification_title))
                .setContentText(context.getString(R.string.alarm_notification_text))
                .setSmallIcon(R.drawable.app_icon_notification)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();
    }
}