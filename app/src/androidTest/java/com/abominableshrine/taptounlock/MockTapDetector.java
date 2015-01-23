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

public class MockTapDetector extends BaseTapDetector {

    public static TapPattern pattern = null;
    public static long delay = 1000000000;

    public MockTapDetector() {
        super();
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
        if (null != pattern) {
            long timestamp = 0;
            long now = MockTapDetector.delay;
            for (int i = 0; i < MockTapDetector.pattern.size(); i++) {
                timestamp += MockTapDetector.pattern.getPause(i);
                now += MockTapDetector.pattern.getPause(i);
                this.notifyObservers(timestamp, now, MockTapDetector.pattern.getSide(i));
            }
        }
    }
}
