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

    private TapPattern createTapPatternFromArray(long a[], TapPattern.DeviceSide side) {
        TapPattern ret = new TapPattern();
        ret.appendTap(side, 0);
        for (long interval : a) {
            // Sanity check against careless use of tests
            assertTrue(interval < Integer.MAX_VALUE);

            ret.appendTap(TapPattern.DeviceSide.BACK, (int) interval);
        }
        return ret;
    }

    public void testEmptyPatternMatch() {
        assertTrue(new TapPattern().matches(new TapPattern()));
    }

    public void testSingleTapPatternMatch() {
        assertFalse(new TapPattern().appendTap(TapPattern.DeviceSide.BACK, 0).matches(new TapPattern()));
        assertTrue(new TapPattern().appendTap(TapPattern.DeviceSide.BACK, 0).matches(new TapPattern().appendTap(TapPattern.DeviceSide.BACK, 0)));
        assertTrue(new TapPattern().appendTap(TapPattern.DeviceSide.BACK, 0).matches(new TapPattern().appendTap(TapPattern.DeviceSide.ANY, 0)));
        assertFalse(new TapPattern().appendTap(TapPattern.DeviceSide.BACK, 0).matches(new TapPattern().appendTap(TapPattern.DeviceSide.FRONT, 0)));
    }

    public void testComplexPatternMatch() {
        /*
         * Reference Interval:
         * 225158691L, 222741885L, 769665620L, 695953369L, 673431397L, 1052740898L, 206970215L, 251190185L
         *
         * Working Comparisons:
         * 223968506L, 229278565L, 882171631L, 716393942L, 770782471L, 1162902832L, 244018555L, 254455566L
         *
         * Failed Comparisons:
         * 161102294L, 151153565L, 654687604L, 433697574L, 458740234L, 1007165550L, 165161133L, 166961670L
         * 176220766L, 161132813L, 558929443L, 166168213L, 171203613L, 584375129L, 40283203L, 125885010L
         * 161132813L, 558929443L, 166168213L, 171203613L, 584375129L, 40283203L, 125885010L, 171325683L
         * 558929443L, 166168213L, 171203613L, 584375129L, 40283203L, 125885010L, 171325683L, 281860352L
         * 166168213L, 171203613L, 584375129L, 40283203L, 125885010L, 171325683L, 281860352L, 45349121L
         * 171203613L, 584375129L, 40283203L, 125885010L, 171325683L, 281860352L, 45349121L, 171277161L
         * 166266085L, 146026611L, 699932969L, 171234131L, 171173096L, 704925537L, 156097412L, 161132813L
         */
        TapPattern reference = this.createTapPatternFromArray(new long[]{225158691L, 222741885L, 769665620L, 695953369L, 673431397L, 1052740898L, 206970215L, 251190185L},
                TapPattern.DeviceSide.BACK);

        assertTrue(reference.matches(this.createTapPatternFromArray(new long[]{223968506L, 229278565L, 882171631L, 716393942L, 770782471L, 1162902832L, 244018555L, 254455566L},
                TapPattern.DeviceSide.BACK)));
        assertTrue(reference.matches(this.createTapPatternFromArray(new long[]{223968506L, 229278565L, 882171631L, 716393942L, 770782471L, 1162902832L, 244018555L, 254455566L},
                TapPattern.DeviceSide.ANY)));
        assertFalse(reference.matches(this.createTapPatternFromArray(new long[]{223968506L, 229278565L, 882171631L, 716393942L, 770782471L, 1162902832L, 244018555L, 254455566L},
                TapPattern.DeviceSide.BOTTOM)));
        assertFalse(reference.matches(this.createTapPatternFromArray(new long[]{223968506L, 229278565L, 882171631L, 716393942L, 770782471L, 1162902832L, 244018555L},
                TapPattern.DeviceSide.BACK)));

        assertFalse(reference.matches(this.createTapPatternFromArray(new long[]{161102294L, 151153565L, 654687604L, 433697574L, 458740234L, 1007165550L, 165161133L, 166961670L}, TapPattern.DeviceSide.BACK)));
        assertFalse(reference.matches(this.createTapPatternFromArray(new long[]{176220766L, 161132813L, 558929443L, 166168213L, 171203613L, 584375129L, 40283203L, 125885010L}, TapPattern.DeviceSide.BACK)));
        assertFalse(reference.matches(this.createTapPatternFromArray(new long[]{161132813L, 558929443L, 166168213L, 171203613L, 584375129L, 40283203L, 125885010L, 171325683L}, TapPattern.DeviceSide.BACK)));
        assertFalse(reference.matches(this.createTapPatternFromArray(new long[]{558929443L, 166168213L, 171203613L, 584375129L, 40283203L, 125885010L, 171325683L, 281860352L}, TapPattern.DeviceSide.BACK)));
        assertFalse(reference.matches(this.createTapPatternFromArray(new long[]{166168213L, 171203613L, 584375129L, 40283203L, 125885010L, 171325683L, 281860352L, 45349121L}, TapPattern.DeviceSide.BACK)));
        assertFalse(reference.matches(this.createTapPatternFromArray(new long[]{171203613L, 584375129L, 40283203L, 125885010L, 171325683L, 281860352L, 45349121L, 171277161L}, TapPattern.DeviceSide.BACK)));
        assertFalse(reference.matches(this.createTapPatternFromArray(new long[]{166266085L, 146026611L, 699932969L, 171234131L, 171173096L, 704925537L, 156097412L, 161132813L}, TapPattern.DeviceSide.BACK)));
    }
}
