package com.abominableshrine.taptounlock;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;

/* The Activity from within a user can record a tap pattern.
 * State machine based UI. */
public class RecordPatternActivity extends Activity {
    private boolean DEBUG = AppConstants.DEBUG;

    private ActivityState currentActivityState;
    private long fromTime = 0L;
    private static final long CUT_OFF_TIME = 150000000L;
    private TextView recordingText;
    private TextView explanationText;
    private Button proceedButton;
    private Button recordPatternButton;
    private Button retryButton;
    private RPAOnClickListener mOnClickListener = new RPAOnClickListener();

    private IBinder mIBinder;
    private Messenger mUnlockServiceMessenger = null;
    private RecordPatternActivityTapPatternDetectorClient mRecordPatternActivityTapPatternDetectorClient;
    private ServiceConnection mRecordServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mUnlockServiceMessenger = new Messenger(service);
            mIBinder = mUnlockServiceMessenger.getBinder();
            mRecordPatternActivityTapPatternDetectorClient = new RecordPatternActivityTapPatternDetectorClient(mIBinder);
        }

        public void onServiceDisconnected(ComponentName className) {
            mUnlockServiceMessenger = null;
            mIBinder = null;
            mRecordPatternActivityTapPatternDetectorClient = null;
        }
    };

    private TapPattern mTapPattern;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_pattern);
        currentActivityState = ActivityState.INIT;

        proceedButton = (Button) findViewById(R.id.proceed_button);
        proceedButton.setEnabled(false); // Initially, the button is disabled.
        proceedButton.setOnClickListener(mOnClickListener);

        retryButton = (Button) findViewById(R.id.retry_button);
        retryButton.setOnClickListener(mOnClickListener);
        retryButton.setEnabled(false);

        recordPatternButton = (Button) findViewById(R.id.record_pattern_button);
        recordPatternButton.setOnClickListener(mOnClickListener);

        recordingText = (TextView) findViewById(R.id.recording_text);
        Animation blinkingAnimation = new AlphaAnimation(0.0f, 1.0f);
        blinkingAnimation.setDuration(500);
        blinkingAnimation.setRepeatMode(Animation.REVERSE);
        blinkingAnimation.setRepeatCount(Animation.INFINITE);
        recordingText.startAnimation(blinkingAnimation);

        explanationText = (TextView) findViewById(R.id.explanation_text);

        Intent tapPatternDetectorServiceIntent = new Intent(this, TapPatternDetectorService.class);
        bindService(tapPatternDetectorServiceIntent, mRecordServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private class RecordPatternActivityTapPatternDetectorClient extends TapPatternDetectorClient {
        public RecordPatternActivityTapPatternDetectorClient(IBinder binder) {
            super(binder);
        }

        @Override
        void onRecentTapsResponse(TapPattern pattern) {
            if(DEBUG) Log.d(AppConstants.TAG, "Record Pattern Activity received Tap Pattern.");
            mTapPattern = pattern;
        }

        @Override
        void onPatternMatch(TapPattern pattern) { // In confirming state
            mOnClickListener.toFinalState();
        }
    }

    private class RPAOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.record_pattern_button:
                    if(currentActivityState == ActivityState.INIT) {
                        // Take the current time, let the TapPatterDetectorService do the rest
                        fromTime = SystemClock.elapsedRealtimeNanos();
                        if(DEBUG) Log.d(AppConstants.TAG, "Recording pattern");
                        toRecordingState();
                    }
                    else { // currentActivityState == ActivityState.PATTERN_RECORDED
                        mRecordPatternActivityTapPatternDetectorClient.subscribe(mTapPattern);
                        if(DEBUG) Log.d(AppConstants.TAG, "Waiting for pattern to be confirmed");
                        toConfirmingState();
                        // TODO: Wait until pattern time has been exceeded, then show error dialog
                    }
                    break;
                case R.id.proceed_button:
                    if (currentActivityState == ActivityState.RECORDING) {
                        // Get the recorded pattern from the TapPatternDetectorService
                        fromTime = SystemClock.elapsedRealtimeNanos() - fromTime; // Calculates the time span
                        mRecordPatternActivityTapPatternDetectorClient.requestRecentTaps(fromTime, CUT_OFF_TIME);
                        toPatternRecordedState();
                    }
                    else { // currentActivityState == ActivityState.FINAL
                        // TODO:
                        // second pattern has been confirmed, now set it globally,
                        // unsubscribe to it from here and go back to the MainActivity
                    }
                    break;
                case R.id.retry_button:
                    if (currentActivityState == ActivityState.CONFIRMING) toPatternRecordedState();
                    else toInitState();
                    break;
                default:
                    Log.e(AppConstants.TAG, "Unknown view clicked, this should not happen!");
            }
        }

        private void toInitState() {
            recordingText.setText("");
            proceedButton.setText(R.string.proceed);
            proceedButton.setEnabled(false);
            recordPatternButton.setText(R.string.record_pattern);
            recordPatternButton.setEnabled(true);
            retryButton.setText(R.string.retry);
            retryButton.setEnabled(false);
            explanationText.setText(R.string.record_pattern_explanation);
            currentActivityState = ActivityState.INIT;
        }

        private void toRecordingState() {
            recordingText.setText(R.string.recording);
            proceedButton.setText(R.string.proceed);
            proceedButton.setEnabled(true);
            recordPatternButton.setText(R.string.record_pattern);
            recordPatternButton.setEnabled(false);
            retryButton.setText(R.string.retry);
            retryButton.setEnabled(true);
            explanationText.setText(R.string.record_pattern_explanation);
            currentActivityState = ActivityState.RECORDING;
        }

        private void toPatternRecordedState() {
            recordingText.setText("");
            proceedButton.setText(R.string.proceed);
            proceedButton.setEnabled(false);
            recordPatternButton.setText(R.string.record_pattern);
            recordPatternButton.setEnabled(true);
            retryButton.setText(R.string.retry);
            retryButton.setEnabled(true);
            explanationText.setText(R.string.record_pattern_second_explanation);
            currentActivityState = ActivityState.PATTERN_RECORDED;
        }

        private void toConfirmingState() {
            recordingText.setText(R.string.recording);
            proceedButton.setText(R.string.proceed);
            proceedButton.setEnabled(false);
            recordPatternButton.setText(R.string.record_pattern);
            recordPatternButton.setEnabled(false);
            retryButton.setText(R.string.retry);
            retryButton.setEnabled(true);
            explanationText.setText(R.string.record_pattern_second_explanation);
            currentActivityState = ActivityState.CONFIRMING;
        }

        private void toFinalState() {
            recordingText.setText("");
            proceedButton.setText(R.string.finish);
            proceedButton.setEnabled(true);
            recordPatternButton.setText(R.string.record_pattern);
            recordPatternButton.setEnabled(false);
            retryButton.setText(R.string.start_over);
            retryButton.setEnabled(true);
            explanationText.setText(R.string.set_pattern_or_not);
            currentActivityState = ActivityState.FINAL;
        }
    }

    private enum ActivityState { INIT, RECORDING, PATTERN_RECORDED, CONFIRMING, FINAL }

    // Note to self: Think about what happens when someone presses the back button while recording
}
