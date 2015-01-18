package com.abominableshrine.taptounlock;

import android.os.IBinder;

/**
* Created by vsaw on 18/01/15.
*/
class CountingDeathRecipient implements IBinder.DeathRecipient {

    /**
     * The count how often binderDied was called
     */
    public int count = 0;

    @Override
    public void binderDied() {
        count++;
    }
}
