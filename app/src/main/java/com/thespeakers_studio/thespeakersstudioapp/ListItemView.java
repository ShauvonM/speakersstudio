package com.thespeakers_studio.thespeakersstudioapp;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by smcgi_000 on 6/8/2016.
 */
abstract class ListItemView extends RelativeLayout implements View.OnClickListener {
    protected Prompt mPrompt;
    protected ListItemListener mOpenListener;

    protected Paint mLinePaint;
    protected Paint mFinishPaint;
    protected Path mLine;
    protected Path mFinishLine;

    private int mFinishLineBottom;

    protected boolean mAnimateFinish;
    protected boolean mFinishShown;

    private long mStartTime;

    private boolean mEnabled;

    public ListItemView(Context context, Prompt prompt) {
        super(context);

        // set up the globals
        mPrompt = prompt;
        mOpenListener = null;

        // set up the layout parameter stuff
        setClipChildren(false);
        setClipToPadding(false);
        setGravity(Gravity.CENTER);
        setLayoutTransition(new LayoutTransition());
        setPadding(getResources().getDimensionPixelOffset(R.dimen.activity_horizontal_margin), 0,
                getResources().getDimensionPixelOffset(R.dimen.activity_horizontal_margin), 0);


        // set up the view - these are set up in the various child classes
        inflateView();
        renderViews();

        // this ensures onDraw is fired
        setWillNotDraw(false);

        // set up the paint objects for the timeline line
        if (mPrompt.getType() != PresentationData.HEADER) {
            mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mLinePaint.setColor(ContextCompat.getColor(context, R.color.promptLine));
            mLinePaint.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.prompt_list_line_width));
            mLinePaint.setStyle(Paint.Style.STROKE);

            mFinishPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mFinishPaint.setColor(ContextCompat.getColor(context, R.color.completedPromptBG));
            mFinishPaint.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.prompt_list_line_width));
            mFinishPaint.setStyle(Paint.Style.STROKE);

            mLine = new Path();
            mFinishLine = new Path();

            mAnimateFinish = false;
            mFinishShown = false; //mPrompt.getAnswer().size() > 0;
            mFinishLineBottom = 0;

            mEnabled = true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mPrompt.getType() != PresentationData.HEADER) {
            // draw the timeline line in the background
            int top = 0;
            int bottom = getChildAt(0).getTop() + 10; //mPrompt.getType() == PresentationData.NEXT ? canvas.getHeight() / 2 : canvas.getHeight();
            int x = getWidth() / 2;

            mLine.reset();
            mLine.moveTo(x, top);
            mLine.lineTo(x, bottom - 10);
            canvas.drawPath(mLine, mFinishShown ? mFinishPaint : mLinePaint);

            // we can delay starting the animation by setting a startTime in the future
            long currentTime = System.currentTimeMillis();
            if (mStartTime > currentTime) {
                postInvalidateDelayed(mStartTime - currentTime);
                return;
            }

            if (mAnimateFinish) {
                long elapsedTime = currentTime - mStartTime;
                if (elapsedTime > Utils.PROMPT_PROGRESS_ANIMATION_DURATION) {
                    elapsedTime = Utils.PROMPT_PROGRESS_ANIMATION_DURATION;
                }

                float interval = ((float) elapsedTime) / ((float) Utils.PROMPT_PROGRESS_ANIMATION_DURATION);
                mFinishLine.reset();
                mFinishLine.moveTo(x, top);
                mFinishLine.lineTo(x, bottom * interval);
                canvas.drawPath(mFinishLine, mFinishPaint);

                if (elapsedTime < Utils.PROMPT_PROGRESS_ANIMATION_DURATION) {
                    this.postInvalidateDelayed(1000 / Utils.FRAMES_PER_SECOND);
                } else {
                    mAnimateFinish = false;
                    mFinishShown = true;
                }
            }
        }
    }

    public void enable() {
        this.mEnabled = true;
    };

    public void disable() {
        this.mEnabled = false;
    }

    public void setContiguous() {
        if (!mFinishShown) {
            mAnimateFinish = true;
            mStartTime = System.currentTimeMillis() + (Utils.PROMPT_PROGRESS_ANIMATION_DURATION * (mPrompt.getOrder() - 1));
        }
        enable();
    }

    protected void animateLineTop (int delay) {
        if (!mFinishShown) {
            mAnimateFinish = true;
            mStartTime = System.currentTimeMillis() + delay;
            postInvalidate();
        }
        enable();
    }
    protected void animateLineTop () {
        animateLineTop(0);
    }

    public boolean isFinishShown() {
        return mFinishShown;
    }

    abstract void inflateView();

    abstract void renderViews();

    @Override
    public void onClick (View v) {
        if (mOpenListener != null) {
            mOpenListener.onListItemDefaultClick(this);
        }
    }

    public void setListItemOpenListener (ListItemListener listener) {
        this.mOpenListener = listener;
    }

    protected void fireItemOpen () {
        if (mOpenListener != null) {
            mOpenListener.onListItemOpen(this);
        }
    }

    protected void fireItemClosed () {
        if (mOpenListener != null) {
            mOpenListener.onListItemClosed(this);
        }
    }

    protected void goToNext() {
        if (mOpenListener != null) {
            int position = mPrompt.getOrder();
            mOpenListener.onGoToNext(position);
        }
    }

    public interface ListItemListener {
        public void onListItemOpen(ListItemView item);
        public void onListItemClosed(ListItemView item);
        public void onListItemDefaultClick(ListItemView item);
        public void onGoToNext(int position);
        public void onSaveItem(Prompt prompt);
    }
}