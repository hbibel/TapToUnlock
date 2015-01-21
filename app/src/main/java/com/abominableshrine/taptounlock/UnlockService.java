package com.abominableshrine.taptounlock;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * The Service that can lock and unlock the device. It does so if it receives a message from
 * a client that tells it to do so.
 * As of now, it can not respond to the client.
 */
public class UnlockService extends Service {
    private final static Boolean DEBUG = AppConstants.DEBUG;
    private static UnlockService instance;

    private boolean mBound;
    private static Boolean running; // Boolean class to enable check for null
    private Messenger mUnlockServiceMessenger = null;
    private static ScreenOffBroadcastReceiver mScreenOffBroadcastReceiver;
    private IntentFilter screenOffFilter;
    private final Intent tapPatternDetectorServiceIntent = new Intent(this, TapPatternDetectorService.class);
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

    private class UnlockServiceTapPatternDetectorClient extends TapPatternDetectorClient{
        public UnlockServiceTapPatternDetectorClient (IBinder binder) {
            super(binder);
        }

        @Override
        void onRecentTapsResponse(TapPattern pattern) {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        void onPatternMatch(TapPattern pattern) {
            throw new UnsupportedOperationException("Not Implemented");
        }
    }

    protected static final int MSG_UNLOCK = 1;
    protected static final int MSG_LOCK = 2;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        instance = this;
        running = true;
        bindService(tapPatternDetectorServiceIntent, mUnlockServiceConnection, Context.BIND_AUTO_CREATE);

        // initialization of the ScreenOffBroadcastReceiver
        mScreenOffBroadcastReceiver = new ScreenOffBroadcastReceiver();
        screenOffFilter = new IntentFilter();
        screenOffFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenOffBroadcastReceiver, screenOffFilter);

        // Tell the TapPatternDetectorService what pattern to search for
        Message mMessage = Message.obtain(null, MSG_UNLOCK);
        Bundle messageData = new Bundle();
        messageData.putString("UnlockPattern", "This could be any parcelable data type, a string or even a byte[]");
        mMessage.setData(messageData);
        try {
            mUnlockServiceMessenger.send(mMessage);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("UnlockService.onBind(Intent intent) not implemented!");
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mScreenOffBroadcastReceiver);
        lock();

        // Unbind from the service
        if (mBound) {
            unbindService(mUnlockServiceConnection);
            mBound = false;
        }

        running = false;
        super.onDestroy();
    }

    /**
     * This method locks the device again with a pattern or password.
     *
     * @see #unlock()
     */
    private static void lock() {

        String[] shellCommands = {
                "cd /data/system",
                "mv passwordtemp.key password.key",
                "mv gesturetemp.key gesture.key"
        };
        runAsRoot(shellCommands);
        if (DEBUG) Log.d(AppConstants.TAG, "Device locked");
    }

    /**
     * If the device is locked by pattern or password, the Keyguard will accept any password or
     * pattern if it does not find the gesture.key resp. password.key file. So, to unlock, we
     * simply rename the password.key and gesture.key file. When the device is locked again, the
     * files are named back.
     *
     * @see #lock()
     */
    private static void unlock() {
        // When the device is being unlocked, the ScreenOffBroadcastReceiver should first be
        // stopped so it does not disturb the unlocking process. Afterwards, we turn it on
        // again.
        instance.stopReceiver();

        String[] shellCommands = {
                "cd /data/system",
                "mv password.key passwordtemp.key",
                "mv gesture.key gesturetemp.key",
                "input keyevent " + Integer.toString(KeyEvent.KEYCODE_POWER)
        };
        runAsRoot(shellCommands);
        if (DEBUG) Log.d(AppConstants.TAG, "Device unlocked");

        // TODO: Test if this is the right place to start the receiver again.
        instance.startReceiver();
    }

    /**
     * This method takes a series of shell commands and executes them as superuser.
     * Thanks to http://stackoverflow.com/questions/6882248/running-shell-commands-though-java-code-on-android
     *
     * @param commands The shell commands to be executed. One string for each command.
     */
    private static void runAsRoot(String[] commands) {
        Process p;
        try {
            p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            for (String tmpCmd : commands) {
                os.writeBytes(tmpCmd + "\n");
                os.flush();
            }
            os.writeBytes("exit\n");
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* This BroadcastReceiver should detect when the screen of the device is turned off. If so,
     * the device has to be locked again. */
    public static class ScreenOffBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                if (!isRunning()) {
                    if (DEBUG) Log.d(AppConstants.TAG, "Screen is turned off but the " +
                            "UnlockService is not running. Aborting.");
                    return;
                }
                Log.d(AppConstants.TAG, "Screen turned off. locking device");
                SensorEventHandler.lockDevice();
            }
        }
    }

    public void stopReceiver() {
        unregisterReceiver(mScreenOffBroadcastReceiver);
    }

    public void startReceiver() {
        registerReceiver(mScreenOffBroadcastReceiver, screenOffFilter);
    }

    public static boolean isRunning() {
        return Boolean.TRUE.equals(running);
    }

}
