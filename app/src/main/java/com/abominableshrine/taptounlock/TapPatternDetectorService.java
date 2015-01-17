package com.abominableshrine.taptounlock;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

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
        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();
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
        bindService(new Intent(this, UnlockService.class), mUnlockServiceConnection, Context.BIND_AUTO_CREATE);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Unbind from the service
        if (mBound) {
            unbindService(mUnlockServiceConnection);
            mBound = false;
        }
        super.onDestroy();
    }

    private ServiceConnection mUnlockServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mUnlockServiceMessenger = new Messenger(service);
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            mUnlockServiceMessenger = null;
            mBound = false;
        }
    };


}
