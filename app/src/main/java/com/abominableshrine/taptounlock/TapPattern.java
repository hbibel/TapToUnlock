package com.abominableshrine.taptounlock;

import android.os.Bundle;

import java.util.ArrayList;

/**
 * Describing a tap pattern
 * <p/>
 * A tap pattern will be described by the side that has been tapped and the interval between two
 * taps.
 * <p/>
 * Facts like the force of the tap or the position of the tap on the side will not be
 * considered by this model.
 * <p/>
 * This class can be easily sent via Messenger to remote services as it provides convenient methods
 * to be bundled and extracted from a bundle. See {@link #toBundle()} and
 * {@link #TapPattern(android.os.Bundle)}
 */
public class TapPattern {

    final private static String KEY_SIDES = "sides";
    final private static String KEY_PAUSES = "pauses";

    private ArrayList<DeviceSide> sides;
    private ArrayList<Integer> pauses;

    /**
     * Create an empty tap pattern
     */
    public TapPattern() {
        this.sides = new ArrayList<>();
        this.pauses = new ArrayList<>();
    }

    /**
     * Create a tap pattern from a Bundle
     *
     * @param b The bundle to create the pattern from
     */
    public TapPattern(Bundle b) {
        this();

        this.pauses = b.getIntegerArrayList(TapPattern.KEY_PAUSES);
        int sides[] = b.getIntArray(TapPattern.KEY_SIDES);
        for (int side : sides) {
            this.sides.add(DeviceSide.values()[side]);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TapPattern that = (TapPattern) o;

        if (!pauses.equals(that.pauses)) return false;
        if (!sides.equals(that.sides)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = sides.hashCode();
        result = 31 * result + pauses.hashCode();
        return result;
    }

    /**
     * Add a new tap to the end of the pattern
     * <p/>
     * The pause will be ignored for the first tap in the pattern
     *
     * @param where          Where the device has been tapped
     * @param pauseBeforeTap Pause before this tap in milliseconds
     * @return The same pattern for call chaining
     */
    public TapPattern appendTap(DeviceSide where, int pauseBeforeTap) {
        if (this.size() != 0) {
            if (pauseBeforeTap <= 0) {
                return null;
            }
            this.pauses.add(pauseBeforeTap);
        }
        this.sides.add(where);
        return this;
    }

    /**
     * The number of taps in the pattern
     *
     * @return The number of taps in the pattern
     */
    public int size() {
        return this.sides.size();
    }

    /**
     * The duration in nanoseconds of the pattern
     *
     * @return The duration in nanoseconds
     */
    public long duration() {
        long totalPause = 0;
        for (long pause : this.pauses) {
            totalPause += pause;
        }
        return totalPause;
    }

    /**
     * Convert this tap pattern to a bundle that can be sent via a Message
     *
     * @return The bundle representation of the tap pattern
     */
    public Bundle toBundle() {
        Bundle b = new Bundle();
        int sides[] = new int[this.sides.size()];
        for (int i = 0; i < sides.length; i++) {
            sides[i] = this.sides.get(i).ordinal();
        }
        b.putIntArray(TapPattern.KEY_SIDES, sides);
        b.putIntegerArrayList(TapPattern.KEY_PAUSES, this.pauses);
        return b;
    }

    /**
     * The different sides a device can be tapped.
     * <p/>
     * Screen is considered to be always in the front.
     */
    public enum DeviceSide {
        ANY,
        FRONT,
        BACK,
        TOP,
        BOTTOM,
        LEFT,
        RIGHT
    }
}
