package com.abominableshrine.taptounlock;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

/* The Activity from within a user can record a tap pattern */
public class RecordPatternActivity extends Activity {
    private Button mRecordPatternButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_pattern);

        mRecordPatternButton = (Button) findViewById(R.id.record_pattern_button);
        mRecordPatternButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });
    }

    // Note to self: Think about what happens when someone presses the back button while recording
}
