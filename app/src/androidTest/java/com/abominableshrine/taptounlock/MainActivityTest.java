package com.abominableshrine.taptounlock;

import android.app.Service;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public MainActivityTest() {
        super(MainActivity.class);
    }

    private void stopServiceIfRunning(Class<? extends Service> serviceClass) {
        if (Utils.isServiceRunning(getActivity().getApplicationContext(), TapPatternDetectorService.class)) {
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

    public void testTapDetectionServiceOffAtStart() {
        assertFalse(Utils.isServiceRunning(getActivity().getApplicationContext(), TapPatternDetectorService.class));
    }

    public void testTapDetectionServiceStartOnButtonPress() {
        pressServiceStartStopButton();

        assertTrue(Utils.isServiceRunning(getActivity().getApplicationContext(), TapPatternDetectorService.class));
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

        assertFalse(Utils.isServiceRunning(getActivity().getApplicationContext(), TapPatternDetectorService.class));
    }
}
