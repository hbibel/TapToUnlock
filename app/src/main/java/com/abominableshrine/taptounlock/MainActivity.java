package com.abominableshrine.taptounlock;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/* The UI Activity from within the user can start and stop the SensorListenerService. */
public class MainActivity extends ActionBarActivity {
    private static boolean DEBUG;
    private Button startStopServiceButton;
    private TextView serviceStatus;
    private Intent UnlockServiceIntent;
    private static Class unlockServiceClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DEBUG = AppConstants.DEBUG;
        setContentView(R.layout.activity_main);

        if (DEBUG) Log.d(AppConstants.TAG, "MainActivity created. Debug mode enabled.");

        rootOrNot();

        try {
            unlockServiceClass = Class.forName("com.abominableshrine.taptounlock.UnlockService");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            if(DEBUG) Log.e(AppConstants.TAG, e.toString());
        }

        // Initialize the button that toggles the service on or off
        serviceStatus = (TextView) findViewById(R.id.service_status);
        serviceStatus.setText(Utils.isServiceRunning(getApplicationContext(), unlockServiceClass) ? R.string.service_running : R.string.service_not_running);
        UnlockServiceIntent = new Intent(this, UnlockService.class);
        final Intent tapDetectionIntent = new Intent(this, TapPatternDetectorService.class);
        startStopServiceButton = (Button) findViewById(R.id.start_stop_service_button);
        startStopServiceButton.setText(Utils.isServiceRunning(getApplicationContext(), unlockServiceClass) ? R.string.stop_service : R.string.start_service);
        startStopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startStopServiceButton == v) {
                    if (Utils.isServiceRunning(getApplicationContext(), unlockServiceClass)) {
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

    /*
     * Executes the 'which su' to determine if the device is rooted or not. If not, the 'which'
     * command does not seem to exist, hence the comparison to "which: not found"
     */
    private void rootOrNot() {
        Process localProcess;
        String line;
        try {
            localProcess = Runtime.getRuntime().exec("which su");
            BufferedReader in = new BufferedReader(new InputStreamReader(localProcess.getInputStream()));
            line = in.readLine();
        } catch (IOException e) {
            Log.e(AppConstants.TAG, e.toString());
            return;
        }
        if(DEBUG) Log.d(AppConstants.TAG, "Response to \'which su\' command: " + line);
        String text = (line.equals("/system/bin/sh: which: not found") ?
                getResources().getText(R.string.device_not_rooted).toString() :
                getResources().getText(R.string.device_rooted).toString());

        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
        toast.show();
    }

    public void goToRecordPatternActivity (View v) {
        Intent i = new Intent(this, RecordPatternActivity.class);
        startActivity(i);
    }
}
