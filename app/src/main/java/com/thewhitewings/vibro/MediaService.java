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
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


public class MediaService extends Service {

    private static final String TAG = "MediaService";

    private final IBinder mBinder = new MyBinder();

    public static final String CHANNEL_ID = "mediaServiceChannel";

    MediaPlayer mediaPlayer;
    AudioAttributes audioAttributes;
    AudioManager audioManager;
    float volumeBeforeDucking;

    public MediaService() {
        audioManager = (AudioManager) MainActivity.getContex().getSystemService(Context.AUDIO_SERVICE);
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

        startForeground(1, mNotification());

        audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
    }//onCreate

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        mReleaseMediaPlayer();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public void playAudio() {

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
        MediaService getService() {
            return MediaService.this;
        }
    }

    //mNotification()
    private Notification mNotification() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.alarm_notification),
                    NotificationManager.IMPORTANCE_HIGH
            );
            serviceChannel.setDescription(getString(R.string.this_is_the_channel_of_alarm_notifications));

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                    .createNotificationChannel(serviceChannel);
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.vibro_is_active))
                .setContentText(getString(R.string.click_to_get_back_to_vibro))
                .setSmallIcon(R.drawable.app_icon_notification)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
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

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        return Uri.parse(sharedPreferences.getString("alarm_tone", rawResourceString));
    }


}//MediaService.class

