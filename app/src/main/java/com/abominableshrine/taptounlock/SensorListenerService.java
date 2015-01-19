package com.abominableshrine.taptounlock;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

/* The background service that contains the sensor event listener */
public class SensorListenerService extends Service {
    private static boolean DEBUG;
    private static SensorManager mSensorManager;
    private SensorEventHandler mSensorEventHandler;

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

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(AppConstants.TAG, "SensorListenerService is being destroyed");
        SensorEventHandler.lockDevice();
        mSensorManager.unregisterListener(mSensorEventHandler);
        super.onDestroy();
    }



}
