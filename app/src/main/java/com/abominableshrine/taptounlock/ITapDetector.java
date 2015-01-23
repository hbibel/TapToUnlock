package com.abominableshrine.taptounlock;

import android.hardware.SensorEventListener;

public interface ITapDetector extends SensorEventListener {
    /**
     * Notify the detector about sensor changes.
     * <p/>
     * This API gets the data from {@link #onSensorChanged(android.hardware.SensorEvent)} and has
     * been added for testing because it is not possible to feed specific data to a
     * {@link android.hardware.SensorEventListener} because it is not possible to create a
     * {@link android.hardware.SensorEvent} in user code.
     *
     * @param timestamp The timestamp of the event
     * @param senorType The type of sensor with new readings
     * @param accuracy  The accuracy of the reading
     * @param values    The readings from the sensor
     */
    public void onSensorChanged(long timestamp, int senorType, int accuracy, float values[]);

    public void onAccuracyChanged(int sensorType, int i);

    /**
     * Register an observer to be notified on each detected tap
     *
     * @param o The observer
     */
    public void registerTapObserver(TapDetector.TapObserver o);

    /**
     * Remove a previously registered observer
     *
     * @param o The observer to remove
     */
    public void removeTapObserver(TapDetector.TapObserver o);

    /**
     * A simple interface to be implemented by observers if they require Tap notifications
     */
    public interface TapObserver {
        /**
         * Callback when a Tap has been detected
         *
         * @param timestamp When the tap occured
         * @param now       The current time
         */
        public void onTap(long timestamp, long now, TapPattern.DeviceSide side);
    }
}
