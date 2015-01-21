package com.abominableshrine.taptounlock;

import android.os.Bundle;

import junit.framework.TestCase;

public class TapPatternTest extends TestCase {

    private TapPattern p;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        p = new TapPattern();
    }

    public void testEmptyPatternSizeDuration() {
        assertEquals(0, p.size());
        assertEquals(0, p.duration());
    }

    public void testSingleTapPatternDuration() {
        assertNotNull(p.appendTap(TapPattern.DeviceSide.BACK, 10));
        assertEquals(1, p.size());
        assertEquals(0, p.duration());
    }

    public void testNullOnNegativeDuration() {
        p.appendTap(TapPattern.DeviceSide.TOP, 10);
        assertNull(p.appendTap(TapPattern.DeviceSide.FRONT, -1));
    }

    public void testIgnorePauseFirstTap() {
        assertNotNull(new TapPattern().appendTap(TapPattern.DeviceSide.LEFT, 0));
        assertNotNull(new TapPattern().appendTap(TapPattern.DeviceSide.LEFT, -20));
    }

    public void testNullOnZeroDuration() {
        TapPattern p1 = p.appendTap(TapPattern.DeviceSide.LEFT, 10);
        assertNotNull(p1);
        assertNull(p1.appendTap(TapPattern.DeviceSide.FRONT, 0));
    }

    private void fillComplexPattern(TapPattern p) {
        p.appendTap(TapPattern.DeviceSide.FRONT, 10)
                .appendTap(TapPattern.DeviceSide.TOP, 10)
                .appendTap(TapPattern.DeviceSide.BOTTOM, 10);
    }

    public void testSizeDurationLongPattern() {
        fillComplexPattern(p);
        assertEquals(3, p.size());
        assertEquals(20, p.duration());
    }

    public void testNotEqualsSingleAndEmptyPattern() {
        TapPattern emptyPattern = new TapPattern();
        TapPattern singlePattern = new TapPattern().appendTap(TapPattern.DeviceSide.LEFT, 10);
        assertFalse(emptyPattern.equals(singlePattern));
    }

    public void testNotEqualsDifferentSides() {
        TapPattern rightTap = new TapPattern().appendTap(TapPattern.DeviceSide.RIGHT, 1);
        TapPattern leftTap = new TapPattern().appendTap(TapPattern.DeviceSide.LEFT, 1);
        assertFalse(leftTap.equals(rightTap));
    }

    public void testEqualComplexPattern() {
        TapPattern p1 = new TapPattern();
        fillComplexPattern(p);
        fillComplexPattern(p1);
        assertEquals(p1, p);
    }

    public void testBundleRoundTrip() {
        fillComplexPattern(p);
        Bundle b = p.toBundle();
        assertEquals(p, new TapPattern(b));
    }

    public void testEmptyBundleRoundTrip() {
        Bundle b = p.toBundle();
        assertEquals(p, new TapPattern(b));
    }

    public void testSingleTapBundleRoundTrip() {
        p.appendTap(TapPattern.DeviceSide.BACK, 10);
        Bundle b = p.toBundle();
        assertEquals(p, new TapPattern(b));
    }
}
