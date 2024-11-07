package com.thewhitewings.shaky;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MediaAndSensorServiceManagerImpl implements MediaAndSensorServiceManager {

    private static final String TAG = "ServiceManagerImpl";
    private final Context context;
    private SensorHelper sensorHelper;
    private MediaHandler mediaHandler;

    public MediaAndSensorServiceManagerImpl(Context context) {
        this.context = context;
        sensorHelper = new SensorHelper(context);
        mediaHandler = new MediaHandler(context);
    }

    @Override
    public void play() {
        Intent intent = new Intent(context, MediaAndSensorService.class);
        intent.setAction("ACTION_PLAY");
        context.startService(intent);
        Log.d(TAG, "play: ");

//        sensorHelper.activateSensor();

//        sensorHelper.getIsShaking().observe(context, isShaking -> {
//            if (isShaking) {
////                mediaHandler.playMedia();
//                Log.d(TAG, "play: isShaking");
//            }
//        });
    }

    @Override
    public void pause() {
        Intent intent = new Intent(context, MediaAndSensorService.class);
        intent.setAction("ACTION_PAUSE");
        context.startService(intent);
        Log.d(TAG, "pause: ");
    }

    @Override
    public void stop() {
        Intent intent = new Intent(context, MediaAndSensorService.class);
        intent.setAction("ACTION_STOP");
        context.startService(intent);
        Log.d(TAG, "stop: ");

        sensorHelper.deactivateSensor();
    }
}
