package com.abominableshrine.taptounlock;

import android.os.IBinder;

/**
 * Helper class to communicate with the TapPatternDetectorService
 * <p/>
 * This class intends to provide simple interfaces for clients without going through the hassle of
 * Messaging.
 */
public abstract class TapPatternDetectorClient {

    /**
     * Create a new TapPatternDetectorClient to connect to the Service at the other end of the
     * binder
     *
     * @param binder The binder to use for the messaging
     */
    public TapPatternDetectorClient(IBinder binder) {
    }

    /**
     * Callback to be implemented by users when recent taps have been requested
     *
     * @param pattern The pattern that was tapped in the given time frame
     */
    abstract void onRecentTapsResponse(TapPattern pattern);

    /**
     * Callback to be implemented by users when a matching subscription has been found
     *
     * @param pattern The pattern what was matched
     */
    abstract void onPatternMatch(TapPattern pattern);

    /**
     * Request the recent taps detected in the given time span
     * <p/>
     * The time span is given by the nanoseconds from now, the following invocation will create a
     * message to retrieve all taps that happened 5 seconds until 1 nanosecond ago:
     * <p/>
     * <c>client.requestRecentTaps(-5000000000L, -1)</c>
     *
     * @param fromTime Beginning of the time span in nanoseconds from now. Must be less than <c>toTime</c>
     * @param toTime   End of the time span in nanoseconds from now. Must be less than 0
     */
    public void requestRecentTaps(long fromTime, long toTime) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    /**
     * Subscribe to a certain tap pattern
     *
     * @param pattern The pattern to subscribe to. Must not be <c>null</c>
     */
    public void subscribe(TapPattern pattern) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    /**
     * Unsubscribe from a certain tap pattern
     *
     * @param pattern The pattern to unsubscribe from. Must not be <c>null</c>
     */
    public void unsubscribe(TapPattern pattern) {
        throw new UnsupportedOperationException("Not Implemented");
    }
}
