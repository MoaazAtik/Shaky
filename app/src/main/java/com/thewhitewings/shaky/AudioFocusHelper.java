package com.thewhitewings.shaky;

import android.content.Context;
import android.media.AudioManager;

public class AudioFocusHelper {

    private final AudioManager audioManager;
    private final AudioManager.OnAudioFocusChangeListener focusChangeListener;

    public AudioFocusHelper(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        focusChangeListener = this::onAudioFocusChange;
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
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
                // Handle audio focus loss (e.g., stop playback)
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Handle transient audio focus loss (e.g., pause playback)
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Handle ducking (e.g., lower volume)
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                // Handle audio focus gain (e.g., resume playback or reset volume)
                break;
        }
    }
}
