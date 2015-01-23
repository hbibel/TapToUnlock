package com.abominableshrine.taptounlock;

import java.util.ArrayList;

/**
 * Abstract TapDetector that takes care of observer house-keeping
 * <p/>
 * TapDetection itself is not implemented, but it provides a convenient
 * {@link #notifyObservers(long, long, com.abominableshrine.taptounlock.TapPattern.DeviceSide)}
 * method.
 */
public abstract class BaseTapDetector implements ITapDetector {

    private ArrayList<TapDetector.TapObserver> observers;

    public BaseTapDetector() {
        this.observers = new ArrayList<>();
    }

    /**
     * Call {@link com.abominableshrine.taptounlock.TapDetector.TapObserver#onTap(long, long, com.abominableshrine.taptounlock.TapPattern.DeviceSide)}
     * for all registered observers
     * <p/>
     * The arguments will be passed directly to the observers.
     *
     * @param timestamp The timestamp of the tap
     * @param now       The current time
     * @param side      The side of the tap
     */
    protected void notifyObservers(long timestamp, long now, TapPattern.DeviceSide side) {
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
