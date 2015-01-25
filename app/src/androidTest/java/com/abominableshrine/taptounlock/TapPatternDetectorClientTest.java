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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.test.AndroidTestCase;
import android.util.Log;

import com.abominableshrine.taptounlock.mocks.MockTapDetector;
import com.abominableshrine.taptounlock.mocks.MockTapPatternDetectorClient;

public class TapPatternDetectorClientTest extends AndroidTestCase {

    private MockTapPatternDetectorClient client;
    private Intent intent;
    private ServiceConnection mConnection;
    private IBinder binder;
    private Looper looper;
    private Thread clientThread;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        MockTapDetector.isAsync = true;
        MockTapDetector.delay = 1000000000;
        MockTapDetector.now = SystemClock.elapsedRealtimeNanos();

        this.intent = new Intent(this.getContext(), TapPatternDetectorService.class);
        this.intent.putExtra(TapPatternDetectorService.KEY_TAP_DETECTOR_CLASS, MockTapDetector.class);
        this.getContext().startService(this.intent);

        this.mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                binder = iBinder;
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
            }
        };
        this.getContext().bindService(this.intent, this.mConnection, Context.BIND_AUTO_CREATE);
        while (this.binder == null) {
            Thread.sleep(100);
        }

        this.clientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                looper = Looper.myLooper();
                client = new MockTapPatternDetectorClient(binder);
                Log.d("TapPatternDetectorClientTest", "Running loop");
                Looper.loop();
            }
        });
        this.clientThread.setDaemon(true);
        this.clientThread.start();

        while (null == this.client) {
            Thread.sleep(100);
        }
    }

    @Override
    public void tearDown() throws Exception {
        if (null != this.binder) {
            this.getContext().unbindService(this.mConnection);
            this.binder = null;
        }
        if (null != this.intent) {
            this.getContext().stopService(this.intent);
            this.intent = null;
        }
        while (Utils.isServiceRunning(getContext(), TapPatternDetectorService.class)) {
            Thread.sleep(100);
        }

        this.looper.quit();
        this.clientThread.interrupt();

        super.tearDown();
    }

    private void assertRequestRecentTapsThrowsException(final long fromTime, final long toTime, Class<? extends Exception> e) {
        try {
            client.requestRecentTaps(fromTime, toTime);
        } catch (Exception ex) {
            if (e.equals(ex.getClass())) {
                return;
            }
        }
        fail();
    }

    private void assertSubscribeThrowsException(final TapPattern pattern, Class<? extends Exception> e) {
        try {
            client.subscribe(pattern);
        } catch (Exception ex) {
            if (e.equals(ex.getClass())) {
                return;
            }
        }
        fail();
    }

    public void testRequestRecentTapsArgumentValidation() {
        this.assertRequestRecentTapsThrowsException(-1, 0, IllegalArgumentException.class);
        this.assertRequestRecentTapsThrowsException(-1, -1, IllegalArgumentException.class);
        this.assertRequestRecentTapsThrowsException(-1, -2, IllegalArgumentException.class);
    }

    public void testSubscribeArgumentValidation() {
        this.assertSubscribeThrowsException(null, IllegalArgumentException.class);
        this.assertSubscribeThrowsException(new TapPattern(), IllegalArgumentException.class);
    }

    public void testPatternMatchCallback() throws RemoteException {
        TapPattern subscription = new TapPattern().appendTap(DeviceSide.LEFT, 0);
        MockTapDetector.pattern = subscription;

        this.client.subscribe(subscription);

        try {
            Thread.sleep(100);
            MockTapDetector.sendTaps();
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }

        assertEquals(subscription, this.client.lastMatch);
    }
}
