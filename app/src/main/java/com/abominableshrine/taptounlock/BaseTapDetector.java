/*
 * Copyright 2015 Hannes Bibel, Valentin Sawadski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.abominableshrine.taptounlock;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

import java.util.ArrayList;

/**
 * Abstract TapDetector that takes care of observer house-keeping
 * <p/>
 * TapDetection itself is not implemented, but it provides a convenient
 * {@link #notifyObservers(long, long, DeviceSide)}
 * method.
 */
public abstract class BaseTapDetector implements ITapDetector {

    private ArrayList<TapDetector.TapObserver> observers;

    public BaseTapDetector() {
        this.observers = new ArrayList<>();
    }

    @Override
    public void onSensorChanged(SensorEvent e) {
        this.onSensorChanged(e.timestamp, e.sensor.getType(), e.accuracy, e.values);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        this.onAccuracyChanged(sensor.getType(), i);
    }

    /**
     * Call {@link com.abominableshrine.taptounlock.TapDetector.TapObserver#onTap(long, long, DeviceSide)}
     * for all registered observers
     * <p/>
     * The arguments will be passed directly to the observers.
     *
     * @param timestamp The timestamp of the tap
     * @param now       The current time
     * @param side      The side of the tap
     */
    protected void notifyObservers(long timestamp, long now, DeviceSide side) {
        for (TapDetector.TapObserver obs : this.observers) {
            obs.onTap(timestamp, now, side);
        }
    }

    @Override
    public void registerTapObserver(TapDetector.TapObserver o) {
        if (null != o) {
            this.observers.add(o);
        }
    }

    @Override
    public void removeTapObserver(TapDetector.TapObserver o) {
        if (null != o) {
            this.observers.remove(o);
        }
    }
}
