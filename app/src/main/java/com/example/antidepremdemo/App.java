package com.example.antidepremdemo;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

public class App extends Application {

    public static final String CHANNEL_ID = "mediaServiceChannel";
    private static Context contex;
    public static Context getContex(){return contex;}

    @Override
    public void onCreate() {
        super.onCreate();
        contex=getApplicationContext();
//        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Media Service Chennel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            //do I really need this line?
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(serviceChannel);

        }
    }

}
