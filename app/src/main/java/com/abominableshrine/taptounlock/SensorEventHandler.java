package com.abominableshrine.taptounlock;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.KeyEvent;

import java.io.DataOutputStream;
import java.io.IOException;

/*  */
public class SensorEventHandler implements SensorEventListener {
    private static boolean DEBUG;
    private SensorManager mSensorManager;
    private SensorListenerService mSensorListenerService;
    private static SensorEventHandler instance;

    /**
     * The SensorEventHandler recognizes any sensor event and decides whether to unlock the device
     * or not.
     *
     * @param sm The SensorManager that registered the SensorEventHandler. It is used by the
     *           SensorEventHandler to unregister itself again.
     * @param sl The SensorListenerService that holds the SensorEventHandler.
     */
    public SensorEventHandler(SensorManager sm, SensorListenerService sl) {
        DEBUG = AppConstants.DEBUG;
        mSensorManager = sm;
        mSensorListenerService = sl;
        instance = this;
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Nothing to do here
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // This is where the pattern analysis should happen. Right now, we only check if the device
        // measured an acceleration of more than 16.0 m/s^2.
        if (Math.abs(vectorLength(event.values[0], event.values[1], event.values[2])) > 16.0f) {
            // When the device is being unlocked, the ScreenOffBroadcastReceiver should first be
            // stopped so it does not disturb the unlocking process. Afterwards, we turn it on
            // again.
            mSensorListenerService.stopReceiver();

            // There is no need to listen to the sensors while the phone is unlocked, so we
            // unregister this listener until the phone is locked again.
            mSensorManager.unregisterListener(this);

            if (DEBUG) Log.d(AppConstants.TAG, "Pattern match detected. Unlocking device");
            handleUnlock();
            mSensorListenerService.startReceiver();
        }
    }

    /**
     * If the device is locked by pattern or password, the Keyguard will accept any password or
     * pattern if it does not find the gesture.key resp. password.key file. So, to unlock, we
     * simply rename the password.key and gesture.key file. When the device is locked again, the
     * files are named back.
     *
     * @see #handleLock()
     */
    private void handleUnlock() {
        String[] shellCommands = {
                "cd /data/system",
                "mv password.key passwordtemp.key",
                "mv gesture.key gesturetemp.key",
                "input keyevent " + Integer.toString(KeyEvent.KEYCODE_POWER)
        };
        runAsRoot(shellCommands);
    }

    protected static void lockDevice() {
        instance.handleLock();
    }

    /**
     * This method locks the device again with a pattern or password.
     *
     * @see #handleUnlock()
     */
    private void handleLock() {
        String[] shellCommands = {
                "cd /data/system",
                "mv passwordtemp.key password.key",
                "mv gesturetemp.key gesture.key"
        };
        runAsRoot(shellCommands);

        // After locking, start listening for unlock events again.
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);
        if (DEBUG) Log.d(AppConstants.TAG, "Device locked.");
    }

    /**
     * This method takes a series of shell commands and executes them as superuser.
     * Thanks to http://stackoverflow.com/questions/6882248/running-shell-commands-though-java-code-on-android
     *
     * @param commands The shell commands to be executed. One string for each command.
     */
    private void runAsRoot(String[] commands) {
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

    // Calculating the absolute vector length of a three float vector.
    private float vectorLength(float x, float y, float z) {
        float result = x * x + y * y + z * z;
        result = (float) Math.sqrt((double) result);
        return result;
    }
}
