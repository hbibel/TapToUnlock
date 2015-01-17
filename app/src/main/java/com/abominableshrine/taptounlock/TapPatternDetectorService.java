package com.abominableshrine.taptounlock;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class TapPatternDetectorService extends Service {

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
    /**
     * Target we publish for clients to send messages to TapDetectorHandler
     */
    final Messenger mMessenger = new Messenger(new TapObserverHandler());

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
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    /**
     * Handler of incoming messages from clients.
     */
    class TapObserverHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(AppConstants.TAG, "Got Message");
            switch (msg.what) {
                case MSG_REQ_RECENT_TAPS:
                    Log.d(AppConstants.TAG, "Got recent taps request");
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
