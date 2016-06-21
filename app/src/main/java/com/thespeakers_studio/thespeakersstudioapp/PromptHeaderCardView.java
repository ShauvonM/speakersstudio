package com.thespeakers_studio.thespeakersstudioapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by smcgi_000 on 6/17/2016.
 */
public class PromptHeaderCardView extends CardView {

    private Paint mFillPaint;
    private Path mFillPath;

    private float mFillFactor;
    private float mCurrentFillFactor;

    private long mStartTime;

    int mDuration;

    public PromptHeaderCardView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setWillNotDraw(false);

        mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFillPaint.setColor(ContextCompat.getColor(context, R.color.progressBG));
        mFillPaint.setStyle(Paint.Style.FILL);

        mFillPath = new Path();

        mCurrentFillFactor = 0;
        mFillFactor = 0;
        mStartTime = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mFillFactor > mCurrentFillFactor) {
            // animate the fill to the given amount
            int duration = mDuration; //Utils.PROMPT_PROGRESS_ANIMATION_DURATION;

            long elapsedTime = System.currentTimeMillis() - mStartTime;
            if (elapsedTime > duration) {
                elapsedTime = duration;
            }
            float interval = ((float) elapsedTime) / ((float) duration);
            drawFill(canvas, mCurrentFillFactor, mFillFactor, interval);

            if (elapsedTime < duration) {
                this.postInvalidateDelayed(1000 / Utils.FRAMES_PER_SECOND);
            } else {
                mCurrentFillFactor = mFillFactor;
            }
        } else {
            drawFill(canvas, 0, mCurrentFillFactor, 0);
        }
    }

    private void drawFill (Canvas canvas, float startFactor, float endFactor, float interval) {
        int heightDifference;
        int currentHeight;
        int gotoHeight;

        currentHeight = (int) (getHeight() * startFactor);
        gotoHeight = (int) (getHeight() * endFactor);
        heightDifference = gotoHeight - currentHeight;

        int top = 0;
        int left = 0;
        int right = getWidth();
        int bottom;
        if (interval == 0) {
            bottom = heightDifference;
        } else {
            bottom = currentHeight + (int) (heightDifference * interval);
        }

        float exactCurrentFactor = (float)bottom / (float)getHeight();
        float arcFactor = (float)Math.round(Math.sin(Math.PI * exactCurrentFactor) * 100) / (float)100;
        int arcHeight = (int) (arcFactor * (getHeight() / 4));

        mFillPath.reset();
        mFillPath.moveTo(left, top);
        mFillPath.lineTo(right, top);
        mFillPath.lineTo(right, bottom);

        //mFillPath.arcTo(new RectF(left, bottom, right, bottom + arcHeight), 0, 180);
        mFillPath.lineTo(right / 2, bottom + arcHeight);
        mFillPath.lineTo(left, bottom);

        mFillPath.close();

        canvas.drawPath(mFillPath, mFillPaint);
    }

    public void animateFillFactor(float fillFactor) {
        if (fillFactor != mFillFactor && fillFactor > 0) {
            mDuration = Utils.PROMPT_PROGRESS_ANIMATION_DURATION * (int)((fillFactor - mFillFactor) / 0.1);
            mFillFactor = fillFactor;
            mStartTime = System.currentTimeMillis();
            postInvalidate();
        }
    }

    public void setFillFactor(float fillFactor) {
        mCurrentFillFactor = fillFactor;
        mFillFactor = fillFactor;
    }

}
