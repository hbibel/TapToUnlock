package com.abominableshrine.taptounlock;

import android.test.AndroidTestCase;

public class MockSensorManagerTest extends AndroidTestCase {

    private MockCsvSensor mockCsvSensor;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.mockCsvSensor = new MockCsvSensor(getContext().getResources().openRawResource(R.raw.sample4_morse_sos_broken));
    }

    @Override
    protected void tearDown() throws Exception {
        this.mockCsvSensor.close();
        super.tearDown();
    }

    private void assertArrayEquals(float expected[], float actual[]) {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i]);
        }
    }

    public void testCanReadSensorRecording() {
        MockCsvSensor.MockSensorEvent e = this.mockCsvSensor.nextSensorEvent();

        assertNotNull(e);
        assertEquals(1419822855062608543L, e.timestamp);
        assertEquals(10, e.type);
        assertEquals(3, e.accuracy);
        assertArrayEquals(new float[]{-0.042596f, -0.010968f, -0.053983f}, e.values);
    }

    public void testCanReadAllSensorEvents() {
        MockCsvSensor.MockSensorEvent e = null;
        MockCsvSensor.MockSensorEvent lastEvent;
        int count = 0;

        do {
            lastEvent = e;
            e = this.mockCsvSensor.nextSensorEvent();
            count++;
        } while (e != null);

        assertEquals(673, count);
        assertEquals(1419822858441626371L, lastEvent.timestamp);
        assertEquals(10, lastEvent.type);
        assertEquals(3, lastEvent.accuracy);
        assertArrayEquals(new float[]{0.337076f, 0.044595f, 0.356493f}, lastEvent.values);
    }
}
