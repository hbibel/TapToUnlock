package com.abominableshrine.taptounlock;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/* The UI Activity from within the user can start and stop the SensorListenerService. */
public class MainActivity extends ActionBarActivity {
    private static boolean DEBUG;
    private Button startStopServiceButton;
    private TextView serviceStatus;
    private Intent UnlockServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DEBUG = AppConstants.DEBUG;
        setContentView(R.layout.activity_main);

        if (DEBUG) Log.d(AppConstants.TAG, "MainActivity created. Debug mode enabled.");

        // Initialize the button that toggles the service on or off
        serviceStatus = (TextView) findViewById(R.id.service_status);
        serviceStatus.setText(UnlockService.isRunning() ? R.string.service_running : R.string.service_not_running);
        UnlockServiceIntent = new Intent(this, UnlockService.class);
        final Intent tapDetectionIntent = new Intent(this, TapPatternDetectorService.class);
        startStopServiceButton = (Button) findViewById(R.id.start_stop_service_button);
        startStopServiceButton.setText(UnlockService.isRunning() ? R.string.stop_service : R.string.start_service);
        startStopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startStopServiceButton == v) {
                    if (UnlockService.isRunning()) {
                        try {
                            if (DEBUG) Log.d(AppConstants.TAG, "Stopping SensorListenerService.");
                            stopService(tapDetectionIntent);
                            stopService(UnlockServiceIntent);
                        } catch (SecurityException e) {
                            Log.e(AppConstants.TAG, "You don't have the permission to stop the service!");
                        }
                        startStopServiceButton.setText(R.string.start_service);
                        serviceStatus.setText(R.string.service_not_running);
                    } else {
                        try {
                            startService(tapDetectionIntent);
                            startService(UnlockServiceIntent);
                        } catch (SecurityException e) {
                            Log.e(AppConstants.TAG, "You don't have the permission to start the service!");
                        }
                        startStopServiceButton.setText(R.string.stop_service);
                        serviceStatus.setText(R.string.service_running);
                    }
                }
            }
        });
    }
}
