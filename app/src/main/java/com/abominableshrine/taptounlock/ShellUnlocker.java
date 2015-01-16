package com.abominableshrine.taptounlock;

import android.util.Log;
import android.view.KeyEvent;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Lock and Unlock the device based on series of shell command executed as Root
 */
class ShellUnlocker {
    private static boolean DEBUG = AppConstants.DEBUG;

    /**
     * This method locks the device again with a pattern or password.
     *
     * @see #unlock()
     */
    static void lock() {
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
    static void unlock() {
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
