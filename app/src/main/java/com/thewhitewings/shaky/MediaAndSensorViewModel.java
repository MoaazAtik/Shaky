package com.thewhitewings.shaky;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

public class MediaAndSensorViewModel extends AndroidViewModel {

    private static final String TAG = "MediaAndSensorViewModel";
    private final MutableLiveData<Boolean> isActive = new MutableLiveData<>();
    private final SensorHelper sensorHelper;
    private final AudioFocusHelper audioFocusHelper;
    private final Context context;

    public MediaAndSensorViewModel(Application application) {
        super(application);
//        context = application.getApplicationContext();
        context = application;
        sensorHelper = new SensorHelper(context);
        audioFocusHelper = new AudioFocusHelper(context);
    }


    public LiveData<Boolean> getIsActive() {
        return isActive;
    }

    public void activate() {
        Intent intent = new Intent(context, MediaAndSensorService.class);
        intent.setAction(MediaAndSensorService.Action.ACTIVATE.name());
        context.startService(intent);

        sensorHelper.activateSensor();
        sensorHelper.getIsShaking().observeForever(shakingObserver);

        isActive.setValue(true);
    }

    public void deactivate() {
        Intent intent = new Intent(context, MediaAndSensorService.class);
        intent.setAction(MediaAndSensorService.Action.DEACTIVATE.name());
        context.startService(intent);

        sensorHelper.deactivateSensor();
        sensorHelper.getIsShaking().removeObserver(shakingObserver);
        audioFocusHelper.stopMedia();
        audioFocusHelper.releaseAudioFocus();

        isActive.setValue(false);
    }

    private final Observer<Boolean> shakingObserver = new Observer<Boolean>() {
        @Override
        public void onChanged(@Nullable final Boolean isShaking) {
            if (Boolean.TRUE.equals(isShaking))
                audioFocusHelper.triggerAlarm();
        }
    };

}
