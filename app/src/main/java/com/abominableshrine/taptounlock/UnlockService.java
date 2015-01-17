package com.abominableshrine.taptounlock;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
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
    private Messenger mMessenger = new Messenger (new MessageHandler());

    protected static final int MSG_UNLOCK = 1;
    protected static final int MSG_LOCK = 2;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UNLOCK:
                    unlock();
                    break;
                case MSG_LOCK:
                    lock();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
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
        String[] shellCommands = {
                "cd /data/system",
                "mv password.key passwordtemp.key",
                "mv gesture.key gesturetemp.key",
                "input keyevent " + Integer.toString(KeyEvent.KEYCODE_POWER)
        };
        runAsRoot(shellCommands);
        if (DEBUG) Log.d(AppConstants.TAG, "Device unlocked");
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
}
