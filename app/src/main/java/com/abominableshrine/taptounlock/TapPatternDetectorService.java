package com.abominableshrine.taptounlock;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class TapPatternDetectorService extends Service {

    private static boolean DEBUG = AppConstants.DEBUG;
    static final int MSG_UNLOCK = UnlockService.MSG_UNLOCK;
    static final int MSG_LOCK = UnlockService.MSG_LOCK;
    private boolean mBound;
    private Messenger mUnlockServiceMessenger = null;

    /**
     * Target we publish for clients to send messages to TapDetectorHandler.
     */
    final Messenger mMessenger = new Messenger(new TapObserverHandler());

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
            // We have not defined any messages from the UnlockService to this yet
            switch (msg.what) {
                case MSG_UNLOCK:
                    String pattern = msg.getData().getString("Pattern"); // Keep in mind that this does not have to be a string!
                    // You might want to store the msg object, so you can reply later
                    if (DEBUG) Log.d(AppConstants.TAG, "received the following message: (MSG_UNLOCK, " + pattern + ")");
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void onPatternDetected() {
        if (!mBound) {
            if (DEBUG) Log.e(AppConstants.TAG, "Pattern detected, but the UnlockService is not running!");
            return;
        }
        Message msg = Message.obtain(null, MSG_UNLOCK, 0, 0);
        try {
            mUnlockServiceMessenger.send(msg);
        } catch (RemoteException e) {
            if (DEBUG) Log.e(AppConstants.TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        return START_STICKY;
    }


}
