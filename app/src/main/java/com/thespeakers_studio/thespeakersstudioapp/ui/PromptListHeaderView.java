package com.thespeakers_studio.thespeakersstudioapp.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.settings.SettingsUtils;
import com.thespeakers_studio.thespeakersstudioapp.utils.PaintUtils;

import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.LOGD;

/**
 * Created by smcgi_000 on 8/4/2016.
 */
public class PromptListHeaderView extends LinearLayout{

    private int mSmallHeight;

    private Paint mFillPaint;
    private Path mFillPath;

    private float mFillFactor;
    private float mCurrentFillFactor;

    private long mStartTime;

    int mDuration;

    public PromptListHeaderView(Context context) {
        this(context, null);
    }

    public PromptListHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PromptListHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setWillNotDraw(false);

        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.PromptListHeaderView, 0, 0);

        mSmallHeight = a.getDimensionPixelSize(R.styleable.PromptListHeaderView_minimumHeight, 0);

        mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFillPaint.setColor(a.getColor(R.styleable.PromptListHeaderView_progressColor, 0));
        //mFillPaint.setColor(ContextCompat.getColor(context, R.color.progressBG));
        mFillPaint.setStyle(Paint.Style.FILL);

        mFillPath = new Path();

        mCurrentFillFactor = 0;
        mFillFactor = 0;
        mStartTime = 0;

        a.recycle();
    }

    public int getMinHeight() {
        return mSmallHeight;
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
                this.postInvalidateDelayed(1000 / PaintUtils.FRAMES_PER_SECOND);
            } else {
                mCurrentFillFactor = mFillFactor;
            }
        } else {
            drawFill(canvas, 0, mCurrentFillFactor, 0);
        }
    }

    private void drawFill (Canvas canvas, float startFactor, float endFactor, float interval) {
        int dimenDifference;
        int currentDimen;
        int gotoDimen;

        // show progress as a horizontal progress bar if we are approaching the minimum height
        boolean full = getHeight() > mSmallHeight * 1.5;

        int dimen = full ? getHeight() : getWidth();

        currentDimen = (int) (dimen * startFactor);
        gotoDimen = (int) (dimen * endFactor);
        dimenDifference = gotoDimen - currentDimen;

        int top = 0;
        int left = 0;

        int var;
        if (interval == 0) {
            var = dimenDifference;
        } else {
            var = currentDimen + (int) (dimenDifference * interval);
        }

        int right = full ? getWidth() : var;
        int bottom = full ? var : getHeight();

        mFillPath.reset();
        mFillPath.moveTo(left, top);
        mFillPath.lineTo(right, top);
        mFillPath.lineTo(right, bottom);

        //mFillPath.arcTo(new RectF(left, bottom, right, bottom + arcHeight), 0, 180);
        if (full) {
            float exactCurrentFactor = (float)bottom / (float)dimen;
            float arcFactor = (float)Math.round(Math.sin(Math.PI * exactCurrentFactor) * 100) / (float)100;
            int arcHeight = (int) (arcFactor * (getHeight() / 4));

            mFillPath.lineTo(right / 2, bottom + arcHeight);
        }
        mFillPath.lineTo(left, bottom);

        mFillPath.close();

        canvas.drawPath(mFillPath, mFillPaint);
    }

    public void animateFillFactor(float fillFactor) {
        if (fillFactor != mFillFactor && fillFactor > 0) {
            mDuration = SettingsUtils.PROMPT_PROGRESS_ANIMATION_DURATION * (int)((fillFactor - mFillFactor) / 0.1);
            mFillFactor = fillFactor;
            mStartTime = System.currentTimeMillis();
            postInvalidate();
        }
    }

    public void setFillFactor(float fillFactor) {
        mCurrentFillFactor = fillFactor;
        mFillFactor = fillFactor;
    }

    public void reset() {
        mCurrentFillFactor = 0;
        mFillFactor = 0;
    }
}
