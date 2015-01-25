package com.abominableshrine.taptounlock;

import android.hardware.Sensor;
import android.hardware.SensorManager;

import java.util.List;

/**
 * Detects Taps based on sensor readings
 * <p/>
 * This will work with the Linear Acceleration readings to detect taps. A reading will be considered
 * a tap if it is larger than all reading following it for a certain window and if it is above a
 * minimum threshold.
 */
public class TapDetector extends BaseTapDetector {

    public static final int MIN_TAP_SQUARE_SUM = 45;
    private static final int LOCAL_MAX_WINDOW_SIZE = 25;
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
        super();
        this.resetLocalMax();
    }

    private void resetLocalMax() {
        this.localMaxWindow = LOCAL_MAX_WINDOW_SIZE;
        this.localMaxTimestamp = 0;
        this.localMaxSquareSum = Float.NEGATIVE_INFINITY;
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

    @Override
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
                    this.notifyObservers(this.localMaxTimestamp, timestamp, DeviceSide.ANY);
                }
                this.resetLocalMax();
            }
        }
    }

    @Override
    public void onAccuracyChanged(int sensorType, int i) {
    }

    @Override
    public void subscribeToSensors(SensorManager sensorManager) {
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION);
        for (Sensor s : sensors) {
            sensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    @Override
    public void unsubscribeFromSensors(SensorManager sensorManager) {
        sensorManager.unregisterListener(this);
    }
}
