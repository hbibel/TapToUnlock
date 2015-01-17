package com.abominableshrine.taptounlock;

import android.os.Bundle;

import junit.framework.TestCase;

public class TapPatternTest extends TestCase {

    public void testEmptyPatternSizeDuration() {
        TapPattern p = new TapPattern();
        assertEquals(0, p.size());
        assertEquals(0, p.duration());
    }

    public void testSingleTapPatternDuration() {
        TapPattern p = new TapPattern();
        assertNotNull(p.appendTap(TapPattern.DeviceSide.BACK, 10));
        assertEquals(1, p.size());
        assertEquals(0, p.duration());
    }

    public void testNullOnNegativeDuration() {
        TapPattern p = new TapPattern().appendTap(TapPattern.DeviceSide.TOP, 10);
        assertNull(p.appendTap(TapPattern.DeviceSide.FRONT, -1));
    }

    public void testIgnorePauseFirstTap() {
        assertNotNull(new TapPattern().appendTap(TapPattern.DeviceSide.LEFT, 0));
        assertNotNull(new TapPattern().appendTap(TapPattern.DeviceSide.LEFT, -20));
    }

    public void testNullOnZeroDuration() {
        TapPattern p = new TapPattern();
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
        TapPattern p = new TapPattern();
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
        TapPattern p2 = new TapPattern();
        fillComplexPattern(p1);
        fillComplexPattern(p2);
        assertEquals(p1, p2);
    }

    public void testBundleRoundTrip() {
        TapPattern p1 = new TapPattern();
        fillComplexPattern(p1);
        Bundle b = p1.toBundle();
        assertEquals(p1, new TapPattern(b));
    }
}
