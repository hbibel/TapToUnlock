/*
 * Copyright 2015 Hannes Bibel, Valentin Sawadski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
