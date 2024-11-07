package com.thewhitewings.shaky;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class SensorHandler {

    private static final String TAG = "SensorHandler";

    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private final SensorEventListener sensorEventListener;

    private int currentAcceleration;
    private int lastAcceleration;
    private int magnitudeOfAcceleration;
    private int sensitivityThreshold = 0;
    private final MutableLiveData<Boolean> isShaking = new MutableLiveData<>();

    public SensorHandler(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorEventListener = createSensorEventListener();
    }

    private SensorEventListener createSensorEventListener() {
        return new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                calculateChange(event);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
    }

    public void activateSensor() {
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void deactivateSensor() {
        sensorManager.unregisterListener(sensorEventListener);
    }

    public void updateSensitivityThreshold(int sensitivityThreshold) {
        this.sensitivityThreshold = sensitivityThreshold;
    }


    private void calculateChange(SensorEvent event) {
        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];

        // Calculate the magnitude of the acceleration vector
        currentAcceleration = (int) Math.sqrt(x * x + y * y + z * z);
        if (lastAcceleration != 0) {
            magnitudeOfAcceleration = currentAcceleration - lastAcceleration;
        }
        lastAcceleration = currentAcceleration;

        if (magnitudeOfAcceleration > sensitivityThreshold) {
            isShaking.setValue(true);
            // Reset the shaking state to prevent emitting true again
            // to the observer after the observer re-connects to the live data.
            isShaking.setValue(null);
        }
    }

    public LiveData<Boolean> getIsShaking() {
        return isShaking;
    }
}