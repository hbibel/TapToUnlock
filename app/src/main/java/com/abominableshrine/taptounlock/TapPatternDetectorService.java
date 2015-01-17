package com.abominableshrine.taptounlock;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.widget.Toast;

public class TapPatternDetectorService extends Service {

    /**
     * Command to the service to display a message
     */
    static final int MSG_SAY_HELLO = 1;
    /**
     * Target we publish for clients to send messages to TapDetectorHandler.
     */
    final Messenger mMessenger = new Messenger(new TapObserverHandler());

    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();
        return mMessenger.getBinder();
    }

    /**
     * Handler of incoming messages from clients.
     */
    class TapObserverHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SAY_HELLO:
                    Toast.makeText(getApplicationContext(), "hello!", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
