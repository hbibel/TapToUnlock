package com.abominableshrine.taptounlock;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/*  */
public class SensorEventHandler implements SensorEventListener {
    private static boolean DEBUG;
    private SensorManager mSensorManager;
    private static SensorEventHandler instance;

    /**
     * The SensorEventHandler recognizes any sensor event and decides whether to unlock the device
     * or not.
     *
     * @param sm The SensorManager that registered the SensorEventHandler. It is used by the
     *           SensorEventHandler to unregister itself again.
     * @param sl The SensorListenerService that holds the SensorEventHandler.
     */
    public SensorEventHandler(SensorManager sm, SensorListenerService sl) {
        DEBUG = AppConstants.DEBUG;
        mSensorManager = sm;
        instance = this;
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Nothing to do here
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // This is where the pattern analysis should happen. Right now, we only check if the device
        // measured an acceleration of more than 16.0 m/s^2.
        if (Math.abs(vectorLength(event.values[0], event.values[1], event.values[2])) > 16.0f) {
            // There is no need to listen to the sensors while the phone is unlocked, so we
            // unregister this listener until the phone is locked again.
            mSensorManager.unregisterListener(this);

            if (DEBUG) Log.d(AppConstants.TAG, "Pattern match detected.");
        }
    }

    protected static void lockDevice() {
        // ShellUnlocker.lock();

        // After locking, start listening for unlock events again.
        instance.mSensorManager.registerListener(instance,
                instance.mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    // Calculating the absolute vector length of a three float vector.
    private float vectorLength(float x, float y, float z) {
        float result = x * x + y * y + z * z;
        result = (float) Math.sqrt((double) result);
        return result;
    }
}
