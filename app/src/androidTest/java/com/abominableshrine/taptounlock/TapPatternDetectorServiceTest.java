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
    protected void tearDown() throws Exception {
        if (this.binder != null) {
            this.binder.unlinkToDeath(this.deathRecipient, 0);
        }
        if (null != this.intent) {
            this.getContext().stopService(this.intent);
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

    public void testUsesGivenTapDetector() throws Exception {
        final DeviceSide side = DeviceSide.LEFT;
        final TapPattern p = new TapPattern().appendTap(side, 0);
        MockTapDetector.pattern = p;
        MockTapDetector.delay = 1000000000;
        this.setTapDetectorAndStartService(MockTapDetector.class);

        final MessengerTestThread t = new MessengerTestThread();
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
}
