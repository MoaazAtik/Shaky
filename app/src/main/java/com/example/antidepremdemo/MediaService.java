package com.example.antidepremdemo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

//import static com.example.antidepremdemo.App.CHANNEL_ID;

public class MediaService extends Service {

    private static final String TAG = "MediaService";

    private final IBinder mBinder = new MyBinder();

    public static final String CHANNEL_ID = "mediaServiceChannel";

    MediaPlayer mediaPlayer;
    AudioAttributes audioAttributes;
    AudioManager audioManager;
    float volumeBeforeDucking;
    Handler handler;
    Runnable runnable;

    public MediaService() {
        audioManager = (AudioManager) MainActivity.getContex().getSystemService(Context.AUDIO_SERVICE);
    }

    int result = 0;
    AudioFocusRequest focusRequest = null;

//    AudioFocusRequest finalFocusRequest = focusRequest;
    MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            mReleaseMediaPlayer();
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    audioManager.abandonAudioFocusRequest(finalFocusRequest);
//                } else {
//                    audioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
//                }
        }
    };

    AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {

            volumeBeforeDucking = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:

                    Toast.makeText(MainActivity.getContex(), "Duck", Toast.LENGTH_SHORT).show();
                    mediaPlayer.setVolume(0.1f, 0.1f);

                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    Toast.makeText(MainActivity.getContex(), "Gain", Toast.LENGTH_SHORT).show();
                    if (mediaPlayer != null)
                        mediaPlayer.setVolume(volumeBeforeDucking, volumeBeforeDucking);

                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    Toast.makeText(MainActivity.getContex(), "Loss", Toast.LENGTH_SHORT).show();
                    mediaPlayer.setVolume(0.1f, 0.1f);
//                mReleaseMediaPlayer();
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        mNotification();

        Log.d(TAG, "onCreate:");
        Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();

        audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

    }//onCreate

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        return super.onStartCommand(intent, flags, startId);
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
//        super.onDestroy();
//        Log.d(TAG, "onDestroy: eeeeee "+mediaPlayer+audioManager+audioAttributes+volumeBeforeDucking);
        mReleaseMediaPlayer();
//        Log.d(TAG, "onDestroy: nuuuuu "+mediaPlayer+audioManager+audioAttributes+volumeBeforeDucking);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public void playAudio() {

//        int result = 0;
//        AudioFocusRequest focusRequest = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //for Android 8.0 (API level 26) through Android 11 (API level 30), and Android 12 (API level 31) or later
//            focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
            focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(mOnAudioFocusChangeListener)
                    .build();
            result = audioManager.requestAudioFocus(focusRequest);
        } else {
            //for Android 7.1 (API level 25) and lower
            result = audioManager.requestAudioFocus(mOnAudioFocusChangeListener,
                    AudioManager.STREAM_MUSIC,
//                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
                    AudioManager.AUDIOFOCUS_GAIN);
        }

//        AudioFocusRequest finalFocusRequest = focusRequest;
//        MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                mReleaseMediaPlayer();
////                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
////                    audioManager.abandonAudioFocusRequest(finalFocusRequest);
////                } else {
////                    audioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
////                }
//            }
//        };

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            if (mediaPlayer == null) {
//                mediaPlayer = MediaPlayer.create(this, R.raw.breach_alarm);
                mediaPlayer = MediaPlayer.create(this, R.raw.soft);
                mediaPlayer.setAudioAttributes(audioAttributes);
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(mCompletionListener);

                handler = new Handler(Looper.getMainLooper());
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "handler of loop breaking");
                        if (mediaPlayer != null)
                            mediaPlayer.setLooping(false);
                    }
                };
//                handler.postDelayed(runnable, 2 * 60 * 1000);
                handler.postDelayed(runnable, 5 * 1000);
            }
        }

    }//playAudio

    private void mReleaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
//            audioAttributes = null;
            volumeBeforeDucking = 0;
            handler = null;
            runnable = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                audioManager.abandonAudioFocusRequest(finalFocusRequest);
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
    private void mNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this, 0, notificationIntent, 0);

            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Media Service Chennel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            //do I really need this line?
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(serviceChannel);


            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("My App")
                    .setContentText("My app is running... hurray!")
                    .setSmallIcon(R.drawable.baseline_home_24)
                    .setContentIntent(pendingIntent)
                    .build();

            startForeground(1, notification);
        }
    }//mNotification()


}//MediaService.class

//todo: revise the role of pendingIntent
//todo: create a Tag on github of the main branch (where the app was working
// fine before using a Service)
//todo try adding "android.permission.MODIFY_AUDIO_SETTINGS" to the manifest
// to use STREAM_SYSTEM
//todo: edit notification title ...
//todo: edit handler.postDelayed() to 2 minutes
//todo: complete the overview of Services on Notion

//done:
//todo: use switch instead of if in focusChangeListener
//todo: loop the player,
// and make the handler and the runnable = null
//todo: remove volumeBeforeDucking because it's no longer needed,
// and increase the volume of the mediaPlayer after regaining the focus
//todo: the volume is ducking, but I can't get its value before and after ducking
//todo: Cannot resolve method 'baseSetVolume' in 'MediaPlayer'
// ducking mutes the volume of the app forever. mediaPlayer.setVolume().
//todo: the mediaPlayer.setVolume() crashes the app. rearrange the mediaPlayer methods (create, pause,...)
// like the mediaPlayer in Miwok
//todo: audio isn't playing after stop()
//todo: CodingWithMitch video: make this service a bound service to
// fix calling null audioManger issue
//todo: the problem is mOn() in MainActivity's onCreate is not being called
// so the service is not being created. NO***. it is being called but
// I didn't get an onServiceConnected() callback from serviceConnection
// aka. there is no connection between MainActivity and MediaService yet.
//todo: Audio is not playing after AudioManager.AUDIOFOCUS_LOSS