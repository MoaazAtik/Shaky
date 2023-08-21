package com.thewhitewings.vibro;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


public class MediaAndSensorService extends Service {

    private static final String TAG = "MediaAndSensorService";

    private final IBinder mBinder = new MyBinder();

    public static final String CHANNEL_ID = "mediaServiceChannel";

    MediaPlayer mediaPlayer;
    AudioAttributes audioAttributes;
    AudioManager audioManager;
    float volumeBeforeDucking;

    private SensorManager sensorManager;
    private Sensor mAccelerometer;
    private int currentAcceleration;
    private int prevAcceleration;
    private int changeInAcceleration;
    int sensitivityCutoff = 1; //the lower value the more sensitive
    private SensorEventListener sensorEventListener;

    PowerManager.WakeLock wakeLock;


    public MediaAndSensorService() {
        audioManager = (AudioManager) MainActivity.getContex().getSystemService(Context.AUDIO_SERVICE);
        Log.d(TAG, "MediaAndSensorService: constructor");
    }

    int result = 0;
    AudioFocusRequest focusRequest = null;

    MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            mReleaseMediaPlayer();
        }
    };

    AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {

            volumeBeforeDucking = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                case AudioManager.AUDIOFOCUS_LOSS:

                    mediaPlayer.setVolume(0.1f, 0.1f);

                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (mediaPlayer != null)
                        mediaPlayer.setVolume(volumeBeforeDucking, volumeBeforeDucking);

                    break;
            }
        }
    };

    @Override
    public void onCreate() {

        Log.d(TAG, "onCreate:");

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "vibro:service");
        wakeLock.acquire();


        startForeground(1, mNotification());

        audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //sensorEventListener
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                mSensorChanged(event);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };

        sensorManager.registerListener(sensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL); //SENSOR_DELAY_NORMAL = 200000 microseconds
//        sensorManager.registerListener(sensorEventListener, mAccelerometer, 500000);


    }//onCreate

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand: ");
//        return START_REDELIVER_INTENT;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mReleaseMediaPlayer();

        Log.d(TAG, "onDestroy: ");
        sensorManager.unregisterListener(sensorEventListener);

        if (wakeLock.isHeld())
            wakeLock.release();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        return mBinder;
    }


    public void playAudio() {
        Log.d(TAG, "playAudio: ");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //for Android 8.0 (API level 26) through Android 11 (API level 30), and Android 12 (API level 31) or later
            focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(mOnAudioFocusChangeListener)
                    .build();
            result = audioManager.requestAudioFocus(focusRequest);
        } else {
            //for Android 7.1 (API level 25) and lower
            result = audioManager.requestAudioFocus(mOnAudioFocusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
        }


        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            if (mediaPlayer == null) {
                Log.d(TAG, "playAudio: AUDIOFOCUS_REQUEST_GRANTED");
                mediaPlayer = MediaPlayer.create(this, selectedTone());
                mediaPlayer.setAudioAttributes(audioAttributes);
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(mCompletionListener);

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (mediaPlayer != null) {
                            mediaPlayer.stop();
                            mReleaseMediaPlayer();
                        }
                    }
                };
                new Handler(Looper.getMainLooper())
                        .postDelayed(runnable, 5000);
            }
        }

    }//playAudio

    private void mReleaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            volumeBeforeDucking = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioManager.abandonAudioFocusRequest(focusRequest);
            } else {
                audioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
            }
        }
    }


    //MyBinder class
    public class MyBinder extends Binder {
        MediaAndSensorService getService() {
            Log.d(TAG, "getService: MyBinder");
            return MediaAndSensorService.this;
        }
    }

    //mNotification()
    private Notification mNotification() {
        Log.d(TAG, "mNotification: ");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.alarm_notification),
                    NotificationManager.IMPORTANCE_HIGH
            );
            serviceChannel.setDescription(getString(R.string.this_is_the_channel_of_alarm_notifications));

//            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
            ((NotificationManager) getSystemService(NotificationManager.class))
                    .createNotificationChannel(serviceChannel);
        }

        // FLAG_IMMUTABLE (or FLAG_MUTABLE) is required for APIs 31 and above, and will be ignored for APIs below 31
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.vibro_is_active))
                .setContentText(getString(R.string.click_to_get_back_to_vibro))
                .setSmallIcon(R.drawable.app_icon_notification)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
                .setSound(null)
                .setVibrate(null)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();

        return notification;
    }//mNotification()

    public Uri selectedTone() {
        int rawResourceId = R.raw.soft;
        String rawResourceString = ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                getResources().getResourcePackageName(rawResourceId) + '/' +
                getResources().getResourceTypeName(rawResourceId) + '/' +
                getResources().getResourceEntryName(rawResourceId);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        Log.d(TAG, "selectedTone: ");

        return Uri.parse(sharedPreferences.getString("alarm_tone", rawResourceString));
    }


    private void mSensorChanged(SensorEvent event) {
        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];

        currentAcceleration = (int) Math.sqrt(x * x + y * y + z * z);
        if (prevAcceleration != 0) {
            changeInAcceleration = currentAcceleration - prevAcceleration;
        }
        prevAcceleration = currentAcceleration;

//        Log.d(TAG, "mSensorChanged: ");
        if (changeInAcceleration > sensitivityCutoff) {
            Log.d(TAG, "mSensorChanged: changeInAcceleration > sensitivityCutoff");
            playAudio();
        }
    }//mSensorChanged

}//MediaAndSensorService

