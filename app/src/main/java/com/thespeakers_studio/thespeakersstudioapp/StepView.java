package com.thespeakers_studio.thespeakersstudioapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by smcgi_000 on 5/10/2016.
 */
public class StepView extends LinearLayout {
    private Paint mDropshadow;

    public StepView (Context context) {
        this(context, null);
    }
    public StepView (Context context, AttributeSet attrs) {
        super(context, attrs);

        mDropshadow = new Paint();
        ShadowHelper.setShadowLayer(mDropshadow, 2, context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

}
