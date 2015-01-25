package com.abominableshrine.taptounlock;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

public class TapPatternDetectorService extends Service implements ITapDetector.TapObserver {

    /**
     * Command to get the last taps for a certain time span
     */
    static final int MSG_REQ_RECENT_TAPS = 1;
    /**
     * Command the service sends to as a response to the MSG_REQ_RECENT_TAPS command
     */
    static final int MSG_RESP_RECENT_TAPS = 2;
    /**
     * Command to subscribe to a new tapping pattern
     */
    static final int MSG_SUB_PATTERN = 3;
    /**
     * Command to notify a subscriber a matching pattern has been tapped
     */
    static final int MSG_PUB_PATTERN_MATCH = 4;
    static final String KEY_TAP_DETECTOR_CLASS = "TapDetectorClass";
    /**
     * Target we publish for clients to send messages to TapDetectorHandler
     */
    final Messenger mMessenger = new Messenger(new TapObserverHandler());
    private ArrayList<Long> timestamps;
    private ArrayList<DeviceSide> sides;
    private ArrayList<SubscriptionEntry> subscriptions;

    /**
     * Create a new message to request the recent taps detected in the given time span
     * <p/>
     * The time span is given by the nanoseconds from now, the following invocation will create a
     * message to retrieve all taps that happened 5 seconds until 1 nanosecond ago:
     * <p/>
     * <c>TapPatternDetectorService.createRecentTapsRequestMsg(replyTo, -5000000000L, -1)</c>
     *
     * @param replyTo  The Messenger to reply to. Must not be <c>null</c>
     * @param fromTime Beginning of the time span in nanoseconds from now. Must be less than <c>toTime</c>
     * @param toTime   End of the time span in nanoseconds from now. Must be less than 0
     * @return A message or <c>null</c> if the parameters where illegal
     */
    static Message createRecentTapsRequestMsg(Messenger replyTo, long fromTime, long toTime) {
        if (replyTo == null) {
            return null;
        }
        if (fromTime >= toTime || toTime >= 0) {
            return null;
        }

        // TODO: Do I need to set the sendingUid or some other message ID?
        Long interval[] = {new Long(fromTime), new Long(toTime)};
        Message msg = Message.obtain(null, MSG_REQ_RECENT_TAPS, interval);
        msg.replyTo = replyTo;
        return msg;
    }

    /**
     * Create a new message to subscribe to a certain tap pattern
     *
     * @param replyTo The Messenger to reply to. Must not be <c>null</c>
     * @param pattern The pattern to subscribe to. Must not be <c>null</c>
     * @return A message or <c>null</c> if the parameters where illegal
     */
    static Message createSubscribeMsg(Messenger replyTo, TapPattern pattern) {
        if (null == replyTo || null == pattern) {
            return null;
        }
        if (0 == pattern.size()) {
            return null;
        }

        Message msg = Message.obtain(null, MSG_SUB_PATTERN);
        msg.replyTo = replyTo;
        msg.setData(pattern.toBundle());
        return msg;
    }

    /**
     * Create a new message to unsubscribe from a certain tap pattern
     *
     * @param replyTo The Messenger to reply to. Must not be <c>null</c>
     * @param pattern The pattern to unsubscribe from. Must not be <c>null</c>
     * @return A message or <c>null</c> if the parameters where illegal
     */
    static Message createUnsubscribeMsg(Messenger replyTo, TapPattern pattern) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public void onCreate() {
        logI("OnCreate");
        super.onCreate();
        this.timestamps = new ArrayList<>();
        this.sides = new ArrayList<>();
        this.subscriptions = new ArrayList<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logI("OnStartCommand");
        ITapDetector detector = null;
        if (intent.hasExtra(TapPatternDetectorService.KEY_TAP_DETECTOR_CLASS)) {
            Class<? extends ITapDetector> detectorClass = (Class<? extends ITapDetector>) intent.getSerializableExtra(TapPatternDetectorService.KEY_TAP_DETECTOR_CLASS);
            logI("Using Detector: " + detectorClass.getName());
            try {
                detector = (ITapDetector) detectorClass.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (null == detector) {
            detector = new TapDetector();
        }
        detector.registerTapObserver(this);
        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        logI("OnUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        logI("OnBind");
        return mMessenger.getBinder();
    }

    @Override
    public synchronized void onTap(long timestamp, long now, DeviceSide side) {
        logI("OnTap: %s %d %d", side.name(), now, timestamp);
        this.timestamps.add(timestamp);
        this.sides.add(side);

        this.checkSubscriptions();
    }

    /**
     * Checks the list of subscriptions to see if we have a match with the currently recorded taps
     */
    private void checkSubscriptions() {
        TapPattern p = new TapPattern();
        for (SubscriptionEntry e : this.subscriptions) {
            // Determine where whe should start looking for matches based on the length of a
            // subscription
            int i = this.timestamps.size() - e.pattern.size();
            if (i < 0) {
                continue;
            }

            // Build the tap pattern starting at i iff the current pattern does not match the size
            // of the subscription. This optimization helps us to only rebuild the pattern if the
            // subscription size has changed, and not on any iteration
            if (e.pattern.size() != p.size()) {
                p = new TapPattern();
                for (; i < this.timestamps.size(); i++) {
                    long pause = 0;
                    if (i > 0) {
                        pause = this.timestamps.get(i) - this.timestamps.get(i - 1);
                    }
                    p.appendTap(this.sides.get(i), (int) pause);
                }
            }

            if (e.pattern.matches(p)) {
                logI("Found match: %s %s %s", e.subscriber.toString(), e.pattern.toString(), p.toString());
                this.notifySubscriber(e, p);
            }
        }
    }

    /**
     * Notify the subscriber about a match for his subscription
     *
     * @param subscription The subscription that got a match
     * @param match        The actual pattern that was matched
     */
    private void notifySubscriber(SubscriptionEntry subscription, TapPattern match) {
        Message m = Message.obtain(null, MSG_PUB_PATTERN_MATCH);
        m.setData(subscription.pattern.toBundle());
        try {
            subscription.subscriber.send(m);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Log an Info message
     *
     * @param s The message to log
     */
    private void logI(String s) {
        if (AppConstants.DEBUG) {
            Log.i(TapPatternDetectorService.class.getSimpleName(), s);
        }
    }

    /**
     * Log an Info message
     *
     * @param format The format of the message
     * @param args   The arguments passed to the formatter
     */
    private void logI(String format, Object... args) {
        if (AppConstants.DEBUG) {
            //Log.i(TapPatternDetectorService.class.getSimpleName(), String.format(format, args));
            logI(String.format(format, args));
        }
    }

    private synchronized Message handleRecentTapsRequest(Message req) {
        try {
            Long timeFrame[] = (Long[]) req.obj;
            if (timeFrame[0] >= timeFrame[1]) {
                return null;
            }

            long now = 1000L * System.currentTimeMillis();
            long minTime = now + timeFrame[0];
            long maxTime = now + timeFrame[1];
            logI("minTime %d", minTime);
            logI("maxTime %d", maxTime);

            TapPattern p = new TapPattern();
            for (int i = 0; i < this.sides.size(); i++) {
                logI("Tap Timestamp %d", this.timestamps.get(i));
                boolean notToOld = minTime <= this.timestamps.get(i);
                boolean notToYoung = this.timestamps.get(i) <= maxTime;
                if (notToOld && notToYoung) {
                    long pause = 0;
                    if (i > 0) {
                        pause = this.timestamps.get(i) - this.timestamps.get(i - 1);
                    }
                    logI("Appending Tap %s %d", this.sides.get(i).name(), pause);
                    p.appendTap(this.sides.get(i), (int) pause);
                }
            }

            Message reply = Message.obtain(null, MSG_RESP_RECENT_TAPS);
            reply.setData(p.toBundle());
            return reply;
        } catch (Exception e) {
            return null;
        }
    }

    private void addSubscription(SubscriptionEntry s) {
        logI("Adding Subscription %s", s.toString());
        if (this.subscriptions.contains(s)) {
            return;
        }

        this.subscriptions.add(s);
        Collections.sort(this.subscriptions);
    }

    private synchronized Message handlePatternSubscription(Message msg) {
        if (null == msg.replyTo || null == msg.getData()) {
            return null;
        }

        TapPattern p = new TapPattern(msg.getData());
        if (0 == p.size()) {
            return null;
        }

        this.addSubscription(new SubscriptionEntry(msg.replyTo, p));
        return null;
    }

    @Override
    public void onDestroy() {
        logI("OnDestroy");
        super.onDestroy();
    }

    /**
     * Handler of incoming messages from clients.
     */
    class TapObserverHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Message reply;

            // Call the separate handlers to get the reply
            switch (msg.what) {
                case MSG_REQ_RECENT_TAPS:
                    reply = handleRecentTapsRequest(msg);
                    break;
                case MSG_SUB_PATTERN:
                    reply = handlePatternSubscription(msg);
                    break;
                default:
                    // If there is no handler, do nothing
                    logI("Dropping Message: %s", msg.toString());
                    super.handleMessage(msg);
                    return;
            }

            if (null != reply) {
                try {
                    msg.replyTo.send(reply);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * A simple tuple of Subscriber and pattern to manage subscriptions
     */
    private class SubscriptionEntry implements Comparable<SubscriptionEntry> {
        public Messenger subscriber;
        public TapPattern pattern;

        public SubscriptionEntry(Messenger subscriber, TapPattern pattern) {
            this.subscriber = subscriber;
            this.pattern = pattern;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SubscriptionEntry that = (SubscriptionEntry) o;

            if (!pattern.equals(that.pattern)) return false;
            if (!subscriber.equals(that.subscriber)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = subscriber.hashCode();
            result = 31 * result + pattern.hashCode();
            return result;
        }

        /**
         * Sort descending on pattern length
         *
         * @param subscriptionEntry the entry to be compared.
         * @return a positive integer, zero, or a negative integer as this pattern is less than,
         * equal to, or greater than the specified object.
         */
        @Override
        public int compareTo(SubscriptionEntry subscriptionEntry) {
            return subscriptionEntry.pattern.size() - this.pattern.size();
        }
    }
}
