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

package com.abominableshrine.taptounlock.mocks;

import android.os.Handler;
import android.os.Looper;
import android.os.Messenger;

import junit.framework.Assert;

/**
 * Helper Thread to test message exchange between an service and a receiver without the trouble of
 * dealing with multi threading directly in the tests
 * <p/>
 * An example on how to use the helper is
 * <pre>
 * {@code
 * final MessengerTestThread t = new MessengerTestThread();
 * t.test(1000, new Runnable() {
 *     @literal @Override
 *      public void run() {
 *          Message m = createMessageFoo();
 *          // Set the tests messenger as replyTo to receive messages in the callback
 *          m.replyTo = t.messenger;
 *          try {
 *              yourRegularOutsideMessenger.send(m);
 *          } catch (RemoteException e) {
 *              e.printStackTrace();
 *              fail();
 *          }
 *      }
 * }, new Handler.Callback() {
 *     @literal @Override
 *      public boolean handleMessage(Message message) {
 *          assertNotNull(message); // Do other asserts as necessary
 *          // Report success, if this is not invoked a timeout will occur
 *          t.reportSuccess();
 *          return false;
 *      }
 * });
 * }
 * </pre>
 */
public class MessengerTestThread extends Thread {

    /**
     * The messenger to pass to the service if a callback is desired
     */
    public Messenger messenger;
    private Runnable runner;
    private Looper looper;
    private int timeoutMillis;
    private Handler.Callback callback;
    private Handler handler;
    private Thread timeoutClock;
    private Thread self;

    /**
     * Perform a test
     * <p/>
     * This call block until the test has finished or timed out.
     *
     * @param timeoutMillis Milliseconds the test should be finished.
     * @param runnable      The setup procedure to send the first message to the service under test
     * @param callback      The callback to be called when messages from the service have been received
     * @throws InterruptedException
     */
    public void test(int timeoutMillis, Runnable runnable, Handler.Callback callback) {
        this.runner = runnable;
        this.timeoutMillis = timeoutMillis;
        this.callback = callback;
        this.self = this;
        this.start();
        try {
            this.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Assert.fail("Failed joining Test Thread");
        }
    }

    @Override
    public void run() {
        if (this.runner == null || this.callback == null) {
            return;
        }

        timeoutClock = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(timeoutMillis);
                    Assert.fail("Timeout");
                    self.interrupt();
                } catch (InterruptedException e) {
                    // Do nothing, this is expected if the tests does not time out.
                }
            }
        });
        timeoutClock.setDaemon(true);
        timeoutClock.start();

        Looper.prepare();
        this.looper = Looper.myLooper();

        this.handler = new Handler(callback);
        this.messenger = new Messenger(this.handler);

        this.runner.run();

        Looper.loop();
    }

    @Override
    public void interrupt() {
        this.timeoutClock.interrupt();
        this.looper.quit();
        super.interrupt();
    }

    /**
     * Make the test a success to be called from inside the test thread.
     */
    public void reportSuccess() {
        this.interrupt();
    }
}
