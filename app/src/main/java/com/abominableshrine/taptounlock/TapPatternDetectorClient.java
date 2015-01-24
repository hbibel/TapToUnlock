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
     * @param pattern The subscription that matched, not the actual tapped pattern
     */
    abstract void onPatternMatch(TapPattern pattern);

    /**
     * Request the recent taps detected in the given time span
     * <p/>
     * The time span is given by the nanoseconds from now, the following invocation will create a
     * message to retrieve all taps that happened 5 seconds until 1 nanosecond ago:
     * <p/>
     * {@code client.requestRecentTaps(-5000000000L, -1)}
     *
     * @param fromTime Beginning of the time span in nanoseconds from now. Must be less than
     *                 {@code toTime}
     * @param toTime   End of the time span in nanoseconds from now. Must be less than 0
     */
    public void requestRecentTaps(long fromTime, long toTime) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    /**
     * Subscribe to a certain tap pattern
     *
     * @param pattern The pattern to subscribe to. Must not be <c>null</c> or empty
     */
    public void subscribe(TapPattern pattern) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    /**
     * Unsubscribe from a certain tap pattern
     *
     * @param pattern The pattern to unsubscribe from. Must not be <c>null</c> or empty
     */
    public void unsubscribe(TapPattern pattern) {
        throw new UnsupportedOperationException("Not Implemented");
    }
}
