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

public class SensorListener extends Service {
    Context mContext;
    protected static SensorManager mSensorManager;
    private SensorReadingService sensorReadingService;
    private static Boolean running; // Boolean class to enable check for null
    static boolean DEBUG;
    private IntentFilter screenOffFilter;
    protected static ScreenOffBroadcastReceiver mScreenOffBroadcastReceiver ;

    public SensorListener() {
        DEBUG = MainActivity.DEBUG;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mContext = getApplicationContext();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorReadingService = new SensorReadingService(mSensorManager, this);
        mSensorManager.registerListener(sensorReadingService,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);
        setRunning();

        mScreenOffBroadcastReceiver = new ScreenOffBroadcastReceiver();
        screenOffFilter = new IntentFilter();
        screenOffFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenOffBroadcastReceiver, screenOffFilter);


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if(DEBUG) Log.d(MainActivity.TAG, "SensorListener is being destroyed");
        setNotRunning();
        mSensorManager.unregisterListener(sensorReadingService);
        unregisterReceiver(mScreenOffBroadcastReceiver);
        SensorReadingService.getInstance().lockDevice();
        super.onDestroy();
    }

    public static void setRunning() {
        running = Boolean.TRUE;
    }

    public static void setNotRunning() {
        running = Boolean.FALSE;
    }

    public static boolean isRunning() {
        return Boolean.TRUE.equals(running);
    }

    public static class ScreenOffBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
                if (!SensorListener.isRunning()) {
                    if(DEBUG) Log.d(MainActivity.TAG, "SensorListener is not running. Aborting.");
                    return;
                }
                Log.d(MainActivity.TAG, "Screen turned off. locking device");
                SensorReadingService.getInstance().lockDevice();
                mSensorManager.registerListener(SensorReadingService.getInstance(),
                        mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                        SensorManager.SENSOR_DELAY_FASTEST);
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
