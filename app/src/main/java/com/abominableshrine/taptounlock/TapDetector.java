package com.abominableshrine.taptounlock;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import java.util.ArrayList;

/**
 * Detects Taps based on sensor readings
 * <p/>
 * This will work with the Linear Acceleration readings to detect taps. A reading will be considered
 * a tap if it is larger than all reading following it for a certain window and if it is above a
 * minimum threshold.
 */
public class TapDetector implements SensorEventListener {

    public static final int MIN_TAP_SQUARE_SUM = 45;
    private static final int LOCAL_MAX_WINDOW_SIZE = 25;
    private ArrayList<TapObserver> observers;
    /**
     * How many taps we still have to look at to determine if this is a real max
     */
    private int localMaxWindow;
    /**
     * The timestamp of the local max
     */
    private long localMaxTimestamp;
    /**
     * The square sum of the local max values
     */
    private float localMaxSquareSum;

    public TapDetector() {
        this.observers = new ArrayList<>();
        this.resetLocalMax();
    }

    public void resetLocalMax() {
        this.localMaxWindow = LOCAL_MAX_WINDOW_SIZE;
        this.localMaxTimestamp = 0;
        this.localMaxSquareSum = Float.NEGATIVE_INFINITY;
    }

    private void notifyObservers(long timestamp, long now, TapPattern.DeviceSide side) {
        for (TapObserver obs : this.observers) {
            obs.onTap(timestamp, now, side);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent e) {
        this.onSensorChanged(e.timestamp, e.sensor.getType(), e.accuracy, e.values);
    }

    /**
     * Calculate the square sum of the array
     *
     * @param values The sensor readings
     * @return The square sum of readings
     */
    private float squareSum(float values[]) {
        float ret = 0f;
        for (float v : values) {
            ret += v * v;
        }
        return ret;
    }

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
    public void onSensorChanged(long timestamp, int senorType, int accuracy, float values[]) {
        if (Sensor.TYPE_LINEAR_ACCELERATION != senorType) {
            return;
        }

        float squareSum = this.squareSum(values);
        if (squareSum > this.localMaxSquareSum) {
            this.localMaxSquareSum = squareSum;
            this.localMaxTimestamp = timestamp;
            this.localMaxWindow = LOCAL_MAX_WINDOW_SIZE;
        } else {
            this.localMaxWindow--;
            if (this.localMaxWindow == 0) {
                if (this.localMaxSquareSum > MIN_TAP_SQUARE_SUM) {
                    this.notifyObservers(this.localMaxTimestamp, timestamp, TapPattern.DeviceSide.ANY);
                }
                this.resetLocalMax();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        this.onAccuracyChanged(sensor.getType(), i);
    }

    public void onAccuracyChanged(int sensorType, int i) {
    }

    /**
     * Register an observer to be notified on each detected tap
     *
     * @param o The observer
     */
    public void registerTapObserver(TapObserver o) {
        if (null != o) {
            this.observers.add(o);
        }
    }

    /**
     * Remove a previously registered observer
     *
     * @param o The observer to remove
     */
    public void removeTapObserver(TapObserver o) {
        if (null != o) {
            this.observers.remove(o);
        }
    }

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
