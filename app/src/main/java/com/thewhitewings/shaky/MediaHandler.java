package com.thewhitewings.shaky;

import static android.content.Context.MODE_PRIVATE;

import static com.thewhitewings.shaky.Constants.ALARM_TONE_KEY;
import static com.thewhitewings.shaky.Constants.PREFERENCES_NAME;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

public class MediaHandler {

    private static final String TAG = "MediaHandler";
    private MediaPlayer mediaPlayer;
    private final Context context;
    private final AudioManager audioManager;
    private final AudioManager.OnAudioFocusChangeListener focusChangeListener;
    private float volumeBeforeDucking;

    public MediaHandler(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        focusChangeListener = this::onAudioFocusChange;
        this.context = context;
    }

    public void triggerAlarm() {
        if (requestAudioFocus()) {
            playMedia();
        }
    }

    public boolean requestAudioFocus() {
        int result = audioManager.requestAudioFocus(focusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    public void releaseAudioFocus() {
        audioManager.abandonAudioFocus(focusChangeListener);
    }

    private void onAudioFocusChange(int focusChange) {
        volumeBeforeDucking = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                mediaPlayer.setVolume(0.1f, 0.1f); // Reduce volume to 10%
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                mediaPlayer.setVolume(volumeBeforeDucking, volumeBeforeDucking);
                break;
        }
    }

    public void playMedia() {
        if (mediaPlayer != null) return;
        mediaPlayer = MediaPlayer.create(context, getPreferredTone());
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        new Handler(Looper.getMainLooper())
                .postDelayed(this::stopMedia, 10000);
    }

    public void stopMedia() {
        if (mediaPlayer == null) return;
        mediaPlayer.release();
        mediaPlayer = null;
        volumeBeforeDucking = 0;
        releaseAudioFocus();
    }

    private Uri getPreferredTone() {
        int rawResourceId = R.raw.soft;
        String rawResourceString = ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.getResources().getResourcePackageName(rawResourceId) + '/' +
                context.getResources().getResourceTypeName(rawResourceId) + '/' +
                context.getResources().getResourceEntryName(rawResourceId);

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);

        return Uri.parse(sharedPreferences.getString(ALARM_TONE_KEY, rawResourceString));
    }

    public int getVolumeMusicStreamMax() {
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    public int getVolumeMusicStream() {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    public void adjustVolume(int direction, boolean fromDeviceVolumeKeys) {
        int flag = fromDeviceVolumeKeys ? AudioManager.FLAG_SHOW_UI : 0;
        audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                direction,
                flag
        );
    }
}
