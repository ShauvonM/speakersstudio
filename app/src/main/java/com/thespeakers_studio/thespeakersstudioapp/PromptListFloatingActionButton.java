package com.thespeakers_studio.thespeakersstudioapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageButton;

/**
 * Created by smcgi_000 on 6/19/2016.
 */
public class PromptListFloatingActionButton extends FloatingActionButton {
    private Paint mDefaultPaint;

    public PromptListFloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        //setWillNotDraw(false);

        /*
        mDefaultPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDefaultPaint.setColor(ContextCompat.getColor(getContext(), R.color.promptLine));
        mDefaultPaint.setStyle(Paint.Style.FILL);
        mDefaultPaint.setAntiAlias(true);
        Utils.setShadowLayer(mDefaultPaint, 3, getContext());
        */
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //canvas.drawCircle(getLeft() + (getWidth() / 2), getTop() + (getHeight() / 2), getHeight() / 2, mDefaultPaint);
    }
}
