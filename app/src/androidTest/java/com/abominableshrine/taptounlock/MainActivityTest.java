package com.abominableshrine.taptounlock;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public MainActivityTest() {
        super(MainActivity.class);
    }

    private void stopServiceIfRunning(Class<?> serviceClass) {
        if (isServiceRunning(serviceClass)) {
            Intent i = new Intent(getActivity().getApplicationContext(), serviceClass);
            getActivity().stopService(i);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        this.stopServiceIfRunning(UnlockService.class);
        this.stopServiceIfRunning(TapPatternDetectorService.class);

        super.tearDown();
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void testTapDetectionServiceOffAtStart() {
        assertFalse(isServiceRunning(TapPatternDetectorService.class));
    }

    public void testTapDetectionServiceStartOnButtonPress() {
        pressServiceStartStopButton();

        assertTrue(isServiceRunning(TapPatternDetectorService.class));
    }

    private void pressServiceStartStopButton() {
        final Button b = (Button) this.getActivity().findViewById(R.id.start_stop_service_button);
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                b.performClick();
            }
        });

        // Wait until the application is idle. This gives the service time to start
        this.getInstrumentation().waitForIdleSync();
    }

    public void testStopTapDetectionServiceOnSecondPress() {
        // Run the start test to ensure service is running before the second button press
        this.testTapDetectionServiceStartOnButtonPress();

        pressServiceStartStopButton();

        assertFalse(isServiceRunning(TapPatternDetectorService.class));
    }
}
