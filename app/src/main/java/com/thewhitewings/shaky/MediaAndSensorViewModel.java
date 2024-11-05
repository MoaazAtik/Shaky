package com.thewhitewings.shaky;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import androidx.lifecycle.ViewModel;
import java.io.Closeable;


public class MediaAndSensorViewModel extends AndroidViewModel {
//public class MediaAndSensorViewModel extends ViewModel {

    private static final String TAG = "MediaAndSensorViewModel";
    private final MediaAndSensorServiceManager serviceManager;
    private final MutableLiveData<String> currentTrack = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isPlaying = new MutableLiveData<>();

    public MediaAndSensorViewModel(Application application) {
        super(application);
//        super((Closeable) application);
        serviceManager = new MediaAndSensorServiceManagerImpl(application);
    }


    public LiveData<String> getCurrentTrack() {
        return currentTrack;
    }

    public LiveData<Boolean> getIsPlaying() {
        return isPlaying;
    }

    public void play() {
        serviceManager.play();
        isPlaying.setValue(true);
        Log.d(TAG, "play: ");
    }

    public void pause() {
        serviceManager.pause();
        isPlaying.setValue(false);
        Log.d(TAG, "pause: ");
    }

    public void stop() {
        serviceManager.stop();
        isPlaying.setValue(false);
        Log.d(TAG, "stop: ");
    }
}
