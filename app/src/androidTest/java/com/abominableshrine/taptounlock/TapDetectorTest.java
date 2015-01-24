package com.abominableshrine.taptounlock;

import android.test.AndroidTestCase;

import com.abominableshrine.taptounlock.mocks.MockCsvSensor;

import java.io.InputStream;
import java.util.ArrayList;

public class TapDetectorTest extends AndroidTestCase implements TapDetector.TapObserver {

    private MockCsvSensor mockCsvSensor;
    private TapDetector detector;
    private ArrayList<Long> detectedTaps;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        this.detector = new TapDetector();
        this.detector.registerTapObserver(this);
        this.detectedTaps = new ArrayList<>();
    }

    @Override
    protected void tearDown() throws Exception {
        if (this.mockCsvSensor != null) {
            this.mockCsvSensor.close();
        }

        super.tearDown();
    }

    public void assertTaps(int res, long taps[]) {
        InputStream inputStream = getContext().getResources().openRawResource(res);
        mockCsvSensor = new MockCsvSensor(inputStream);

        MockCsvSensor.MockSensorEvent e = mockCsvSensor.nextSensorEvent();
        while (null != e) {
            this.detector.onSensorChanged(e.timestamp, e.type, e.accuracy, e.values);
            e = mockCsvSensor.nextSensorEvent();
        }

        assertEquals(taps.length, this.detectedTaps.size());
        for (int i = 0; i < taps.length; i++) {
            assertEquals(taps[i], this.detectedTaps.get(i), 10000000L);
        }
    }

    public void testFindsSosPatternStale() {
        assertTaps(R.raw.sample1_morse_sos_stale_nexus4,
                new long[]{
                        1418329997885521010L, 1418329998110679701L, 1418329998333421586L,
                        1418329999103087206L, 1418329999799040575L, 1418330000472471972L,
                        1418330001525212870L, 1418330001732183085L, 1418330001983373270L
                });
    }

    public void testFindSosPatternStaleSample2() {
        assertTaps(R.raw.sample2_morse_sos_stale_nexus4,
                new long[]{
                        1418329889746796570L, 1418329889970765076L, 1418329890207825623L,
                        1418329891082215272L, 1418329891798609214L, 1418329892569391685L,
                        1418329893732294517L, 1418329893976313072L, 1418329894230768638L
                });
    }

    public void testFindsSosPatternWalking() {
        assertTaps(R.raw.sample3_morse_sos_walking_nexus4,
                new long[]{
                        1418330668030524423L, 1418330668191626717L, 1418330668342780282L,
                        1418330668997467886l, 1418330669431165460l, 1418330669889905694L,
                        1418330670897071244l, 1418330671062232377l, 1418330671229194047L
                });
    }

    public void testFindBrokenSosWalking() {
        assertTaps(R.raw.sample4_morse_sos_broken,
                new long[]{
                        1419822855505798592l, 1419822855692077889l, 1419822855888458504l,
                        1419822856673993187l,
                        1419822857394055443l, 1419822857610577660l, 1419822857812024193l
                });
    }

    @Override
    public void onTap(long timestamp, long now, DeviceSide side) {
        this.detectedTaps.add(timestamp);
    }
}
