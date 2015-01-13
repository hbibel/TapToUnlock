package com.abominableshrine.taptounlock;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
    public static boolean DEBUG = true;
    Button startStopServiceButton;
    TextView serviceStatus;
    Intent serviceIntent;
    protected static MainActivity activity;
    protected static String TAG = "Knock2Unlock";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;

        if(DEBUG) Log.d(TAG, "Hello, dear developer");
        if(DEBUG) Log.d(TAG, "sdcard path: " + Environment.getExternalStorageDirectory().getPath());

        serviceIntent = new Intent(this, SensorListener.class);
        serviceStatus = (TextView) findViewById(R.id.service_status);
        serviceStatus.setText(SensorListener.isRunning() ? R.string.service_running : R.string.service_not_running);

        startStopServiceButton = (Button) findViewById(R.id.start_stop_service_button);
        startStopServiceButton.setText(SensorListener.isRunning() ? R.string.stop_service : R.string.start_service);
        startStopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startStopServiceButton == v) {
                    if (SensorListener.isRunning()) {
                        try {
                            Log.d(TAG, "attempting to stop the service.");
                            stopService(serviceIntent);
                        }
                        catch (SecurityException e) {
                            Log.e(TAG, "You don't have the permission to stop the service!");
                        }
                        startStopServiceButton.setText(R.string.start_service);
                        serviceStatus.setText(R.string.service_not_running);
                    }
                    else {
                        try {
                            startService(serviceIntent);
                        }
                        catch (SecurityException e) {
                            Log.e(TAG, "You don't have the permission to start the service!");
                        }
                        startStopServiceButton.setText(R.string.stop_service);
                        serviceStatus.setText(R.string.service_running);
                    }
                }
            }
        });
    }

}
