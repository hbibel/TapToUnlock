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

import com.abominableshrine.taptounlock.DeviceSide;
import com.abominableshrine.taptounlock.ITapDetector;
import com.abominableshrine.taptounlock.TapPattern;

import junit.framework.TestCase;

import java.util.ArrayList;

public class MockTapDetectorTest extends TestCase implements ITapDetector.TapObserver {

    private MockTapDetector detector;
    private ArrayList<Long> timestamps;
    private ArrayList<DeviceSide> sides;
    private ArrayList<Long> nows;

    private static long secondsToNanos(int seconds) {
        return seconds * 1000000000;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.timestamps = new ArrayList<>();
        this.sides = new ArrayList<>();
        this.nows = new ArrayList<>();
        MockTapDetector.pattern = null;
        MockTapDetector.isAsync = false;
        MockTapDetector.delay = MockTapDetectorTest.secondsToNanos(1);
        MockTapDetector.now = MockTapDetector.delay;
        this.detector = new MockTapDetector();
    }

    @Override
    public void onTap(long timestamp, long now, DeviceSide side) {
        this.timestamps.add(timestamp);
        this.sides.add(side);
        this.nows.add(now);
    }

    public void testNoTapPatternSet() {
        this.detector.registerTapObserver(this);

        assertEquals(0, this.sides.size());
    }

    public void testSingleTapPattern() {
        DeviceSide side = DeviceSide.BACK;
        MockTapDetector.pattern = new TapPattern().appendTap(side, 0);

        this.detector.registerTapObserver(this);

        assertEquals(1, this.timestamps.size());
        assertEquals(0, this.timestamps.get(0).longValue());
        assertEquals(MockTapDetectorTest.secondsToNanos(1), this.nows.get(0).longValue());
        assertEquals(side, this.sides.get(0));
    }

    public void testComplexTapPattern() {
        MockTapDetector.delay = 500;
        MockTapDetector.now = MockTapDetector.delay;
        DeviceSide sides[] = new DeviceSide[]{DeviceSide.BACK, DeviceSide.BOTTOM, DeviceSide.FRONT};
        MockTapDetector.pattern = new TapPattern();
        for (int i = 0; i < sides.length; i++) {
            MockTapDetector.pattern.appendTap(sides[i], i);
        }

        this.detector.registerTapObserver(this);

        assertEquals(sides.length, this.timestamps.size());
        int timestamp = 0;
        for (int i = 0; i < sides.length; i++) {
            timestamp += i;
            assertEquals(sides[i], this.sides.get(i));
            assertEquals(timestamp, this.timestamps.get(i).longValue());
            assertEquals(MockTapDetector.delay + timestamp, this.nows.get(i).longValue());
        }
    }

    public void testAsyncTapsFireOnlyAfterSend() {
        DeviceSide side = DeviceSide.BACK;
        MockTapDetector.pattern = new TapPattern().appendTap(side, 0);
        MockTapDetector.isAsync = true;

        this.detector.registerTapObserver(this);

        assertEquals(0, this.timestamps.size());

        MockTapDetector.sendTaps();

        assertEquals(1, this.timestamps.size());
        assertEquals(0, this.timestamps.get(0).longValue());
        assertEquals(MockTapDetectorTest.secondsToNanos(1), this.nows.get(0).longValue());
        assertEquals(side, this.sides.get(0));
    }
}
