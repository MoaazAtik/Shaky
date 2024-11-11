package com.thewhitewings.shaky;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.thewhitewings.shaky.data.ShakyPreferences;

/**
 * Class that handles the media (alarm) playback
 */
public class MediaHandler {

    private static final String TAG = "MediaHandler";

    private final Context context;
    private MediaPlayer mediaPlayer;
    private final AudioManager audioManager;
    private final AudioManager.OnAudioFocusChangeListener focusChangeListener;
    private AudioFocusRequest focusRequest;

    /**
     * Volume before ducking (if any).
     * <br>
     * It is used to store the volume before ducking when audio focus is lost,
     * and restore the volume after audio focus is regained.
     */
    private float volumeBeforeDucking;

    /**
     * Constructor
     */
    public MediaHandler(Context context) {
        this.context = context;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        focusChangeListener = this::onAudioFocusChange;
    }


    /**
     * Get the maximum volume for the music stream.
     */
    public int getVolumeMusicStreamMax() {
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    /**
     * Get the current volume for the music stream.
     */
    public int getVolumeMusicStream() {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    /**
     * Trigger the alarm
     * <br>
     * It is a super method that calls {@link #requestAudioFocus()} to request audio focus
     * and {@link #playMedia()} to play the media (alarm).
     */
    public void triggerAlarm() {
        if (requestAudioFocus())
            playMedia();
    }

    /**
     * Request audio focus
     *
     * @return true if audio focus is granted, false otherwise
     */
    private boolean requestAudioFocus() {
        int result;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (focusRequest == null)
                focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setOnAudioFocusChangeListener(focusChangeListener)
                        .build();
            result = audioManager.requestAudioFocus(focusRequest);
        } else {
            result = audioManager.requestAudioFocus(focusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    /**
     * Release audio focus
     */
    public void releaseAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && focusRequest != null)
            audioManager.abandonAudioFocusRequest(focusRequest);
        else
            audioManager.abandonAudioFocus(focusChangeListener);
    }

    /**
     * Handle audio focus change.
     * <br>
     * It is used for the implementation of {@link AudioManager.OnAudioFocusChangeListener}.
     * <br>
     * If audio focus is lost, reduce the volume to 10%.
     * <br>
     * If audio focus is regained, restore the volume to the level before ducking.
     *
     * @param focusChange One of audio focus change constants from {@link AudioManager}.
     */
    private void onAudioFocusChange(int focusChange) {
        volumeBeforeDucking = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Reduce volume to 10%
                mediaPlayer.setVolume(0.1f, 0.1f);
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mediaPlayer != null)
                    // Restore volume to the level before ducking
                    mediaPlayer.setVolume(volumeBeforeDucking, volumeBeforeDucking);
                break;
        }
    }

    /**
     * Play the media (alarm) for the specified duration
     */
    private void playMedia() {
        // Don't play the media if it is already playing
        if (mediaPlayer != null) return;

        int alarmDuration = 10000;
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        mediaPlayer = MediaPlayer.create(context, getPreferredTone());
        mediaPlayer.setAudioAttributes(audioAttributes);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        new Handler(Looper.getMainLooper())
                .postDelayed(
                        this::stopMedia,
                        alarmDuration
                );
    }

    /**
     * Stop the media (alarm) and release the resources
     */
    public void stopMedia() {
        if (mediaPlayer == null) return;
        mediaPlayer.release();
        mediaPlayer = null;
        volumeBeforeDucking = 0;
        releaseAudioFocus();
    }

    /**
     * Get the preferred tone for the alarm
     *
     * @return the preferred tone URI
     */
    private Uri getPreferredTone() {
        // The raw resource of the default alarm tone
        int rawResourceId = R.raw.soft;
        String rawResourceString = ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.getResources().getResourcePackageName(rawResourceId) + '/' +
                context.getResources().getResourceTypeName(rawResourceId) + '/' +
                context.getResources().getResourceEntryName(rawResourceId);

        ShakyPreferences preferences = ((ShakyApplication) context).getPreferences();
        return Uri.parse(
                preferences.getAlarmTonePreference(rawResourceString)
        );
    }

    /**
     * Adjust the volume of the music stream
     *
     * @param direction            {@link AudioManager#ADJUST_RAISE} to increase the volume,
     *                             {@link AudioManager#ADJUST_LOWER} to decrease the volume.
     * @param fromDeviceVolumeKeys true if the volume is adjusted from the device volume keys
     *                             to show the system's volume slider,
     *                             false if it is adjusted from the volume seekbar of the app.
     */
    public void adjustVolume(int direction, boolean fromDeviceVolumeKeys) {
        int flag = fromDeviceVolumeKeys ? AudioManager.FLAG_SHOW_UI : 0;
        audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                direction,
                flag
        );
    }
}