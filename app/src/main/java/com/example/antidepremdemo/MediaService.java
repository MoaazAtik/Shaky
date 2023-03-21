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
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

//import static com.example.antidepremdemo.App.CHANNEL_ID;

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

    @Override
    public void onCreate() {
        super.onCreate();

        mNotification();

        Log.d(TAG, "onCreate: my MediaService");
        Toast.makeText(this, "tossst", Toast.LENGTH_SHORT).show();

        audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

//        audioManager = (AudioManager) MainActivity.getContex().getSystemService(Context.AUDIO_SERVICE);
//        volumeBeforeDucking = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

//        if (mediaPlayer == null) {
//            sensorManager.registerListener(sensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
//            mediaPlayer = MediaPlayer.create(this, R.raw.breach_alarm);
            mediaPlayer = MediaPlayer.create(this, R.raw.soft);
            mediaPlayer.setAudioAttributes(audioAttributes);
//            txtStatus.setText("Active");
//            txtStatus.setTextSize(84);
//            txtStatus.setAllCaps(true);
//        }

    }//onCreate

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        return super.onStartCommand(intent, flags, startId);

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
//        super.onDestroy();
        Log.d(TAG, "onDestroy: eeeeee "+mediaPlayer+audioManager+audioAttributes+volumeBeforeDucking);
//        if (mediaPlayer != null) {
//            sensorManager.unregisterListener(sensorEventListener);
            mediaPlayer.release();
            mediaPlayer = null;
            audioAttributes = null;
            volumeBeforeDucking = 0;
        Log.d(TAG, "onDestroy: nuuuuu "+mediaPlayer+audioManager+audioAttributes+volumeBeforeDucking);
//            txtStatus.setText("inactive");
//            txtStatus.setTextSize(72);
//            txtStatus.setAllCaps(false);
//        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public void playAudio() {

        AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                Log.d(TAG, "playAudio: "+focusChange+"  "+volumeBeforeDucking);
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                        focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                    volumeBeforeDucking = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//                    mediaPlayer.setVolume(0.5f,0.5f);
                Log.d(TAG, "playAudio: "+focusChange+"  "+volumeBeforeDucking+" "+(float)audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
                } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
//                    mediaPlayer.setVolume(volumeBeforeDucking, volumeBeforeDucking);
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
//                    mediaPlayer.setVolume(1.0f,1.0f);
                Log.d(TAG, "playAudio: "+focusChange+"  "+volumeBeforeDucking+" "+(float)audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
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

//todo: Audio is not playing after AudioManager.AUDIOFOCUS_LOSS
//todo: Cannot resolve method 'baseSetVolume' in 'MediaPlayer'
// ducking mutes the volume of the app forever. mediaPlayer.setVolume().
//todo: revise the role of pendingIntent
//todo: create a Tag on github of the main branch (where the app was working
// fine before using a Service)
//todo try adding "android.permission.MODIFY_AUDIO_SETTINGS" to the manifest
// to use STREAM_SYSTEM

//done:
//todo: CodingWithMitch video: make this service a bound service to
// fix calling null audioManger issue
//todo: the problem is mOn() in MainActivity's onCreate is not being called
// so the service is not being created. NO***. it is being called but
// I didn't get an onServiceConnected() callback from serviceConnection
// aka. there is no connection between MainActivity and MediaService yet.