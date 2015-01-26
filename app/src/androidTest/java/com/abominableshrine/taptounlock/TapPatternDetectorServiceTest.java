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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.test.ServiceTestCase;

import com.abominableshrine.taptounlock.mocks.CountingDeathRecipient;
import com.abominableshrine.taptounlock.mocks.MessengerTestThread;
import com.abominableshrine.taptounlock.mocks.MockTapDetector;

public class TapPatternDetectorServiceTest extends ServiceTestCase<TapPatternDetectorService> {

    private Intent intent;
    private IBinder binder;
    private Messenger txMessenger;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            binder = service;
            txMessenger = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            binder = null;
            txMessenger = null;
        }
    };
    private CountingDeathRecipient deathRecipient = new CountingDeathRecipient();

    public TapPatternDetectorServiceTest() {
        super(TapPatternDetectorService.class);
    }

    private void setTapDetectorAndStartService() throws Exception {
        this.setTapDetectorAndStartService(TapDetector.class);
    }

    private void setTapDetectorAndStartService(Class<? extends ITapDetector> cls) throws Exception {
        this.intent = new Intent(this.getContext(), TapPatternDetectorService.class);
        this.intent.putExtra(TapPatternDetectorService.KEY_TAP_DETECTOR_CLASS, cls);
        this.getContext().startService(this.intent);
        this.getContext().bindService(this.intent, this.mConnection, Context.BIND_AUTO_CREATE);
        while (this.txMessenger == null || this.binder == null) {
            Thread.sleep(100);
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockTapDetector.delay = 1000000000;
        MockTapDetector.isAsync = true;
        MockTapDetector.now = System.currentTimeMillis() * 1000000L;
    }

    @Override
    protected void tearDown() throws Exception {
        if (this.binder != null) {
            this.binder.unlinkToDeath(this.deathRecipient, 0);
            this.getContext().unbindService(this.mConnection);
        }
        if (null != this.intent) {
            this.getContext().stopService(this.intent);
        }
        while (Utils.isServiceRunning(getContext(), TapPatternDetectorService.class)) {
            Thread.sleep(100);
        }

        this.intent = null;
        this.binder = null;
        this.txMessenger = null;
        super.tearDown();
    }

    public void testCanBindToService() throws Exception {
        this.setTapDetectorAndStartService();
        assertNotNull(this.binder);
        assertEquals(0, this.deathRecipient.count);
    }

    public void testCreateRecentTapRequestMsg() throws Exception {
        this.setTapDetectorAndStartService();
        Messenger m = new Messenger(new Handler());
        assertNull(TapPatternDetectorService.createRecentTapsRequestMsg(m, -1000000000L, -1000000000));
        assertNull(TapPatternDetectorService.createRecentTapsRequestMsg(m, -100000000L, -1000000000));
        assertNull(TapPatternDetectorService.createRecentTapsRequestMsg(m, 5000000000L, -1000000000));
        assertNull(TapPatternDetectorService.createRecentTapsRequestMsg(m, -5000000000L, 1000000000));
        assertNull(TapPatternDetectorService.createRecentTapsRequestMsg(null, -5000000000L, -1000000000));
        assertNotNull(TapPatternDetectorService.createRecentTapsRequestMsg(m, -5000000000L, -1000000000));
    }

    public void testCreateSubscribeMsg() throws Exception {
        this.setTapDetectorAndStartService();
        Messenger m = new Messenger(new Handler());
        assertNull(TapPatternDetectorService.createSubscribeMsg(null, null));
        assertNull(TapPatternDetectorService.createSubscribeMsg(null, new TapPattern()));
        assertNull(TapPatternDetectorService.createSubscribeMsg(null, new TapPattern().appendTap(DeviceSide.ANY, 0)));
        assertNull(TapPatternDetectorService.createSubscribeMsg(m, null));
        assertNull(TapPatternDetectorService.createSubscribeMsg(m, new TapPattern()));
        assertNotNull(TapPatternDetectorService.createSubscribeMsg(m, new TapPattern().appendTap(DeviceSide.ANY, 0)));
    }

    public void testGetTapsForLastSecondsMessage() throws Exception {
        this.setTapDetectorAndStartService();
        final MessengerTestThread t = new MessengerTestThread();
        t.test(1000, new Runnable() {
            @Override
            public void run() {
                Message m = TapPatternDetectorService.createRecentTapsRequestMsg(t.messenger, -5000000000L, -1000000000);
                try {
                    txMessenger.send(m);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    fail();
                }
            }
        }, new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                assertNotNull(message);
                assertEquals(TapPatternDetectorService.MSG_RESP_RECENT_TAPS, message.what);
                assertEquals(new TapPattern(), new TapPattern(message.getData()));
                t.reportSuccess();
                return false;
            }
        });
    }

    public void testServiceDoesNotReturnOutdatedTaps() throws Exception {
        MockTapDetector.pattern = new TapPattern().appendTap(DeviceSide.LEFT, 0);
        MockTapDetector.isAsync = false;
        final MessengerTestThread t = new MessengerTestThread();

        this.setTapDetectorAndStartService(MockTapDetector.class);
        t.test(1000, new Runnable() {
            @Override
            public void run() {
                Message m = TapPatternDetectorService.createRecentTapsRequestMsg(t.messenger, -5000000, -1000000);
                try {
                    txMessenger.send(m);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    fail();
                }
            }
        }, new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                assertNotNull(message);
                assertEquals(TapPatternDetectorService.MSG_RESP_RECENT_TAPS, message.what);
                assertEquals(new TapPattern(), new TapPattern(message.getData()));
                t.reportSuccess();
                return false;
            }
        });
    }

    public void testUsesGivenTapDetector() throws Exception {
        final DeviceSide side = DeviceSide.LEFT;
        final TapPattern p = new TapPattern().appendTap(side, 0);
        MockTapDetector.pattern = p;
        MockTapDetector.isAsync = false;
        final MessengerTestThread t = new MessengerTestThread();

        this.setTapDetectorAndStartService(MockTapDetector.class);
        t.test(1000, new Runnable() {
            @Override
            public void run() {
                Message m = TapPatternDetectorService.createRecentTapsRequestMsg(t.messenger, -5000000000L, -1000000);
                try {
                    txMessenger.send(m);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    fail();
                }
            }
        }, new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                assertNotNull(message);
                assertEquals(TapPatternDetectorService.MSG_RESP_RECENT_TAPS, message.what);
                assertEquals(p, new TapPattern(message.getData()));
                t.reportSuccess();
                return false;
            }
        });
    }

    public void testSingleTapTapPatternSubscription() throws Exception {
        final DeviceSide side = DeviceSide.LEFT;
        final TapPattern p = new TapPattern().appendTap(side, 0);
        MockTapDetector.pattern = p;
        final MessengerTestThread t = new MessengerTestThread();
        this.setTapDetectorAndStartService(MockTapDetector.class);

        t.test(1000, new Runnable() {
            @Override
            public void run() {
                Message m = TapPatternDetectorService.createSubscribeMsg(t.messenger, p);
                try {
                    txMessenger.send(m);
                    Thread.sleep(100);
                    MockTapDetector.sendTaps();
                } catch (Exception e) {
                    e.printStackTrace();
                    fail();
                }
            }
        }, new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                assertNotNull(message);
                assertEquals(TapPatternDetectorService.MSG_PUB_PATTERN_MATCH, message.what);
                assertEquals(p, new TapPattern(message.getData()));
                t.reportSuccess();
                return false;
            }
        });
    }

    public void testTwoSubscriptionsSameClient() throws Exception {
        final TapPattern pattern1 = new TapPattern().appendTap(DeviceSide.LEFT, 0);
        final TapPattern pattern2 = new TapPattern().appendTap(DeviceSide.RIGHT, 0);
        final TapPattern sentPattern = new TapPattern().appendTap(DeviceSide.BACK, 0).appendTap(pattern1.getSide(0), 500000000);
        MockTapDetector.pattern = sentPattern;
        final MessengerTestThread t = new MessengerTestThread();
        this.setTapDetectorAndStartService(MockTapDetector.class);

        t.test(1000, new Runnable() {
            @Override
            public void run() {
                Message sub1 = TapPatternDetectorService.createSubscribeMsg(t.messenger, pattern1);
                Message sub2 = TapPatternDetectorService.createSubscribeMsg(t.messenger, pattern2);
                try {
                    txMessenger.send(sub1);
                    txMessenger.send(sub2);
                    Thread.sleep(100);
                    MockTapDetector.sendTaps();
                } catch (Exception e) {
                    e.printStackTrace();
                    fail();
                }
            }
        }, new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                assertNotNull(message);
                assertEquals(TapPatternDetectorService.MSG_PUB_PATTERN_MATCH, message.what);
                assertEquals(pattern1, new TapPattern(message.getData()));
                t.reportSuccess();
                return false;
            }
        });
    }
}
