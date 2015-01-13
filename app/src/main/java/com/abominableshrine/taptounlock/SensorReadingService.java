package com.abominableshrine.taptounlock;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.KeyEvent;

import java.io.DataOutputStream;
import java.io.IOException;

public class SensorReadingService implements SensorEventListener {
    boolean DEBUG;
    private SensorManager mSensorManager;
    private static SensorReadingService instance;
    private SensorListener mSensorListener;


    public SensorReadingService(SensorManager sm, SensorListener sl) {
        DEBUG = MainActivity.DEBUG;
        mSensorManager = sm;
        mSensorListener = sl;
        instance = this;
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Nothing to do here
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (Math.abs(vectorLength(event.values[0], event.values[1], event.values[2])) > 16.0f) {
            mSensorListener.stopReceiver();
            mSensorManager.unregisterListener(this);
            if(DEBUG) Log.d(MainActivity.TAG, "significant sensor event detected. Unlocking device");
            handleUnlock();
            mSensorListener.startReceiver();
        }
    }

    private void handleUnlock() {
        String[] shellCommands = {
                //"mv /data/system/password.key " + tempPath, // does not work. No idea why
                //"mv /data/system/gesture.key " + tempPath,
                "cd /data/system",
                "mv password.key passwordtemp.key",
                "mv gesture.key gesturetemp.key",
                "input keyevent " + Integer.toString(KeyEvent.KEYCODE_POWER)
                //"input keyevent 82"
        };
        runAsRoot(shellCommands);
    }

    protected void lockDevice() {
        String[] shellCommands = {
                "cd /data/system",
                "mv passwordtemp.key password.key",
                "mv gesturetemp.key gesture.key"
        };
        runAsRoot(shellCommands);
        if(DEBUG) Log.d(MainActivity.TAG, "Device locked.");
    }

    protected static SensorReadingService getInstance() {
        return instance;
    }

    // Thanks to http://stackoverflow.com/questions/6882248/running-shell-commands-though-java-code-on-android
    public void runAsRoot(String[] cmds){
        Process p;
        try {
            p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            for (String tmpCmd : cmds) {
                os.writeBytes(tmpCmd+"\n");
                os.flush();
            }
            os.writeBytes("exit\n");
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private float vectorLength(float x, float y, float z) {
        float result = x*x+y*y+z*z;
        result = (float) Math.sqrt((double) result);
        return result;
    }
}