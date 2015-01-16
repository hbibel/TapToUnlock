package com.abominableshrine.taptounlock;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

/* The background service that contains the sensor event listener */
public class SensorListenerService extends Service {
    private static boolean DEBUG;
    private static SensorManager mSensorManager;
    private SensorEventHandler mSensorEventHandler;
    private static Boolean running; // Boolean class to enable check for null
    private IntentFilter screenOffFilter;
    private static ScreenOffBroadcastReceiver mScreenOffBroadcastReceiver ;

    public SensorListenerService() {
        DEBUG = AppConstants.DEBUG;
    }

    /* This service should not be bound to any activity, so this method is not implemented. */
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // initialization of the SensorEventHandler
        // We might want to test if slower sensor delays are sufficient, too
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorEventHandler = new SensorEventHandler(mSensorManager, this);
        mSensorManager.registerListener(mSensorEventHandler,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);

        // initialization of the ScreenOffBroadcastReceiver
        mScreenOffBroadcastReceiver = new ScreenOffBroadcastReceiver();
        screenOffFilter = new IntentFilter();
        screenOffFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenOffBroadcastReceiver, screenOffFilter);

        running = Boolean.TRUE;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if(DEBUG) Log.d(AppConstants.TAG, "SensorListenerService is being destroyed");
        SensorEventHandler.lockDevice();
        mSensorManager.unregisterListener(mSensorEventHandler);
        unregisterReceiver(mScreenOffBroadcastReceiver);
        running = Boolean.FALSE;
        super.onDestroy();
    }

    public static boolean isRunning() {
        return Boolean.TRUE.equals(running);
    }

    /* This BroadcastReceiver should detect when the screen of the device is turned off. If so,
     * the device has to be locked again. */
    public static class ScreenOffBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
                if (!SensorListenerService.isRunning()) {
                    if(DEBUG) Log.d(AppConstants.TAG, "Screen is turned off but the " +
                            "SensorListenerService is not running. Aborting.");
                    return;
                }
                Log.d(AppConstants.TAG, "Screen turned off. locking device");
                SensorEventHandler.lockDevice();
            }
        }
    }

    public void stopReceiver () {
        unregisterReceiver(mScreenOffBroadcastReceiver);
    }

    public void startReceiver () {
        registerReceiver(mScreenOffBroadcastReceiver, screenOffFilter);
    }
}
