package com.thewhitewings.shaky;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * Class to handle the device shaking sensor
 */
public class SensorHandler {

    private static final String TAG = "SensorHandler";

    private final SensorManager sensorManager;

    /**
     * The accelerometer sensor that is used to detect shaking
     */
    private final Sensor accelerometer;

    /**
     * The listener that is used to detect sensor changes
     */
    private final SensorEventListener sensorEventListener;

    /**
     * The current state of the device shaking
     */
    private final MutableLiveData<Boolean> isShaking = new MutableLiveData<>();

    /**
     * The current acceleration value
     */
    private int currentAcceleration;

    /**
     * The last acceleration value
     */
    private int lastAcceleration;

    /**
     * The magnitude of the acceleration
     * that is the difference between the current and last acceleration of the device
     */
    private int magnitudeOfAcceleration;

    /**
     * The sensitivity threshold of the sensor to trigger alarms.
     * <br>
     * The change in acceleration must be greater than this value
     * so that the device is considered shaking.
     * <br>
     * The lower the value, the higher the sensitivity,
     * i.e., the sensor needs less vibration to trigger alarms.
     * <br>
     * The minimum value is 0 which is the default value, and the maximum value is 10.
     */
    private int sensitivityThreshold = 0;

    /**
     * Constructor
     */
    public SensorHandler(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorEventListener = createSensorEventListener();
    }


    /**
     * Create a sensor event listener instance by implementing {@link SensorEventListener}
     */
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

    /**
     * Get the current state of the device shaking
     */
    public LiveData<Boolean> getIsShaking() {
        return isShaking;
    }

    /**
     * Update the sensitivity threshold with the given value
     */
    public void updateSensitivityThreshold(int sensitivityThreshold) {
        this.sensitivityThreshold = sensitivityThreshold;
    }

    /**
     * Activate the sensor to detect shaking
     */
    public void activateSensor() {
        sensorManager.registerListener(
                sensorEventListener,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL
        );
    }

    /**
     * Deactivate the sensor to stop detecting shaking and consuming resources
     */
    public void deactivateSensor() {
        sensorManager.unregisterListener(sensorEventListener);
    }

    /**
     * Calculate the change in acceleration of the device to detect shaking
     * and update the {@link #isShaking} state accordingly.
     * <br>
     * It detects shaking by monitoring the the device's acceleration in the three axes,
     * and checking if the device is actually accelerating in any direction, i.e.,
     * the change between the current and last acceleration values
     * is greater than the sensitivity threshold.
     */
    private void calculateChange(SensorEvent event) {
        // Get the acceleration values in the three axes from the passed event argument
        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];

        // Calculate the magnitude of the acceleration vector
        currentAcceleration = (int) Math.sqrt(x * x + y * y + z * z);
        // Ignore false positive case when the device is still
        /*
        To eliminate the effect of gravity on a still device,
         check if the last acceleration value is 0,
         i.e., when the app has just initialized with the sensor falsely
         detecting the gravity as movement. So, on app initialization,
         set the last acceleration value to the gravity value
         and keep the magnitude of the acceleration to 0 to avoid false alarms.
         */
        if (lastAcceleration != 0) {
            // Calculate the magnitude of the acceleration change
            magnitudeOfAcceleration = currentAcceleration - lastAcceleration;
        }
        lastAcceleration = currentAcceleration;

        if (magnitudeOfAcceleration > sensitivityThreshold) {
            isShaking.setValue(true);
            // Reset the shaking state to prevent emitting 'true' again
            // to the observer after the observer disconnects and re-connects to the live data.
            isShaking.setValue(null);
        }
    }
}