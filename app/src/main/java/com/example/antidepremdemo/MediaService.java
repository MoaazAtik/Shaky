package com.example.antidepremdemo;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import static com.example.antidepremdemo.App.CHANNEL_ID;

public class MediaService extends Service {

    MediaPlayer mediaPlayer;
    AudioAttributes audioAttributes;
    AudioManager audioManager;
    float volumeBeforeDucking;

    private static final String TAG = "MediaService";

    //todo: for now, create a new branch from the main one, and enable the WAKE_LOCK,
    // instead of using a service.
    // then just invoke mOff() in onDestroy().
    //todo CodingWithMitch video: make this service a bound service to
    // fix calling null audioManger issue

    @Override
    public void onCreate() {
        super.onCreate();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("My App")
                .setContentText("My app is running... hurray!")
                .setSmallIcon(R.drawable.baseline_home_24)
                .setContentIntent(pendingIntent)
                .build();

        Log.d(TAG, "onCreate: my MediaService");
        Toast.makeText(this, "tossst", Toast.LENGTH_SHORT).show();

        audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        volumeBeforeDucking = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);


        if (mediaPlayer == null) {
//            sensorManager.registerListener(sensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
//            mediaPlayer = MediaPlayer.create(this, R.raw.breach_alarm);
            mediaPlayer = MediaPlayer.create(this, R.raw.soft);
            mediaPlayer.setAudioAttributes(audioAttributes);
            //to enable playing in the background. the MediaPlayer holds the
            // specified lock (in this case, the CPU remains awake)
            // while playing and releases the lock when paused or stopped.
//            mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            //todo: don't forget to add the Wake Lock Permission.
//            txtStatus.setText("Active");
//            txtStatus.setTextSize(84);
//            txtStatus.setAllCaps(true);
        }

        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        return super.onStartCommand(intent, flags, startId);

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
//        super.onDestroy();

        if (mediaPlayer != null) {
//            sensorManager.unregisterListener(sensorEventListener);
            mediaPlayer.release();
            mediaPlayer = null;
//            txtStatus.setText("inactive");
//            txtStatus.setTextSize(72);
//            txtStatus.setAllCaps(false);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    void playAudio() {

        AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                        focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                    volumeBeforeDucking = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//                    mediaPlayer.setVolume(0.1f,0.1f);
                } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                    mediaPlayer.setVolume(volumeBeforeDucking, volumeBeforeDucking);
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    mediaPlayer.stop();
                }
//                if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
//                if (mediaPlayer != null)
//                    mediaPlayer.start();
//                }
            }
        };

        int result = 0;
        AudioFocusRequest focusRequest = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //for Android 8.0 (API level 26) through Android 11 (API level 30), and Android 12 (API level 31) or later
            focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                    .build();
            result = audioManager.requestAudioFocus(focusRequest);
        } else {
            //for Android 7.1 (API level 25) and lower
            result = audioManager.requestAudioFocus(mOnAudioFocusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
        }

        AudioFocusRequest finalFocusRequest = focusRequest;
        MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    audioManager.abandonAudioFocusRequest(finalFocusRequest);
                } else {
                    audioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
                }
            }
        };

//        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(mCompletionListener);
//        }

    }//playAudio

    int volumeMax() {
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }
}

////Running asynchronously
//public class MyService extends Service implements MediaPlayer.OnPreparedListener {
//    private static final String ACTION_PLAY = "com.example.action.PLAY";
//    MediaPlayer mediaPlayer = null;
//
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        ...
//        if (intent.getAction().equals(ACTION_PLAY)) {
//            mediaPlayer = ... // initialize it here
//            mediaPlayer.setOnPreparedListener(this);
//            mediaPlayer.prepareAsync(); // prepare async to not block main thread
//        }
//    }
//
//    /** Called when MediaPlayer is ready */
//    public void onPrepared(MediaPlayer player) {
//        player.start();
//    }
//
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        if (mediaPlayer != null) mediaPlayer.release();
//    }
//}
