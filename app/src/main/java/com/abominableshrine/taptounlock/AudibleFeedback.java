package com.abominableshrine.taptounlock;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * A class that for audible feedback by playing a short sound
 */
public class AudibleFeedback {
    private final static Boolean DEBUG = AppConstants.DEBUG;
    private MediaPlayer mp;

    /**
     * Create an object that can play a sound
     *
     * @param c The context from within the object is constructed
     */
    public AudibleFeedback(Context c) {
        mp = MediaPlayer.create(c, R.raw.drop);
    }

    /**
     * Makes a noise (duh)
     */
    public void makeASound() {
        mp.start();
    }
}
