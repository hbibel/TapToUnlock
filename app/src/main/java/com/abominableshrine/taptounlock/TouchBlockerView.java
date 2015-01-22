package com.abominableshrine.taptounlock;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class TouchBlockerView extends View {
    private boolean touch_disabled=true;

    public TouchBlockerView(Context context) {
        super(context);
    }

    public TouchBlockerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchBlockerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent e) {
        return touch_disabled;
    }

    public void disable_touch(boolean b) {
        touch_disabled=b;
    }
}
