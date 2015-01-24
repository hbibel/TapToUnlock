package com.abominableshrine.taptounlock;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;

import java.util.ArrayList;

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
        if (fromTime >= toTime || toTime > 0) {
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
        throw new UnsupportedOperationException("Not Implemented");
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
        super.onCreate();
        this.timestamps = new ArrayList<>();
        this.sides = new ArrayList<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ITapDetector detector = null;
        if (intent.hasExtra(TapPatternDetectorService.KEY_TAP_DETECTOR_CLASS)) {
            Class<? extends ITapDetector> detectorClass = (Class<? extends ITapDetector>) intent.getSerializableExtra(TapPatternDetectorService.KEY_TAP_DETECTOR_CLASS);
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
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public synchronized void onTap(long timestamp, long now, DeviceSide side) {
        this.timestamps.add(timestamp);
        this.sides.add(side);
    }

    private synchronized Message handleRecentTapsRequest(Message req) {
        try {
            Long timeFrame[] = (Long[]) req.obj;
            if (timeFrame[0] >= timeFrame[1]) {
                return null;
            }

            long now = SystemClock.elapsedRealtimeNanos();
            long minTime = now + timeFrame[0];
            long maxTime = now + timeFrame[1];
            TapPattern p = new TapPattern();
            for (int i = 0; i < this.sides.size(); i++) {
                boolean notToOld = minTime <= this.timestamps.get(i);
                boolean notToYoung = this.timestamps.get(i) <= maxTime;
                if (notToOld && notToYoung) {
                    long pause = 0;
                    if (i > 0) {
                        pause = this.timestamps.get(i) - this.timestamps.get(i - 1);
                    }
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
                default:
                    // If there is no handler, do nothing
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
}
