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

package com.abominableshrine.taptounlock.mocks;

import android.hardware.SensorManager;

import com.abominableshrine.taptounlock.BaseTapDetector;
import com.abominableshrine.taptounlock.TapPattern;

/**
 * Mock TapDetector that sends a predefined pattern to observers when they subscribe
 */
public class MockTapDetector extends BaseTapDetector {

    /**
     * The pattern that will be sent to observers
     */
    public static TapPattern pattern = null;
    /**
     * The delay that will be subtracted from the timestamp of the first tap compared to
     * {@link #now}
     */
    public static long delay = 1000000000;
    /**
     * This will be the first {@code now} value that will be sent to observers
     * <p/>
     * The following values will be incremented by the pauses defined in the {@link #pattern}.
     */
    public static long now = 1000000000;
    /**
     * Controls when the observers will be notified.
     * <p/>
     * If set to false they get the taps as soon as they register, if set to {@code true} only after
     * {@link #sendTaps()} has been called.
     */
    public static boolean isAsync = false;
    /**
     * Instance used for asynchronous sending of taps
     */
    private static MockTapDetector instance;

    public MockTapDetector() {
        super();
        MockTapDetector.instance = this;
    }

    public static void sendTaps() {
        sendTaps(instance);
    }

    private static void sendTaps(MockTapDetector instance) {
        long timestamp = MockTapDetector.now - MockTapDetector.delay;
        long now = MockTapDetector.now;
        for (int i = 0; i < MockTapDetector.pattern.size(); i++) {
            timestamp += MockTapDetector.pattern.getPause(i);
            now += MockTapDetector.pattern.getPause(i);
            instance.notifyObservers(timestamp, now, MockTapDetector.pattern.getSide(i));
        }
    }

    @Override
    public void onSensorChanged(long timestamp, int senorType, int accuracy, float[] values) {
    }

    @Override
    public void onAccuracyChanged(int sensorType, int i) {
    }

    @Override
    public void registerTapObserver(TapObserver o) {
        super.registerTapObserver(o);
        if (null != pattern && !isAsync) {
            sendTaps(this);
        }
    }

    @Override
    public void subscribeToSensors(SensorManager sensorManager) {
    }

    @Override
    public void unsubscribeFromSensors(SensorManager sensorManager) {
    }
}
