package com.abominableshrine.taptounlock;

import android.app.Activity;
import android.os.Bundle;

public class RecordPatternActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_pattern);

        TouchBlockerView mTouchBlockerView = (TouchBlockerView) findViewById(R.id.touch_blocker_view);
        mTouchBlockerView.disable_touch(false);
    }

    // Note to self: Think about what happens when someone presses the back button while recording
}
