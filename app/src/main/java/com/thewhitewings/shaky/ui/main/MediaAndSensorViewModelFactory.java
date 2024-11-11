package com.thewhitewings.shaky.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.thewhitewings.shaky.data.ShakyPreferences;

/**
 * Factory class for creating instances of {@link MediaAndSensorViewModel}
 */
public class MediaAndSensorViewModelFactory implements ViewModelProvider.Factory {

    private final Application application;
    private final ShakyPreferences preference;

    public MediaAndSensorViewModelFactory(Application application, ShakyPreferences preference) {
        this.application = application;
        this.preference = preference;
    }


    @NonNull
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MediaAndSensorViewModel.class)) {
            return (T) new MediaAndSensorViewModel(application, preference);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}