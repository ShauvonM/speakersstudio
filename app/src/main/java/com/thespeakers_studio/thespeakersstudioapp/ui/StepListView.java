package com.thespeakers_studio.thespeakersstudioapp.ui;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.settings.SettingsUtils;
import com.thespeakers_studio.thespeakersstudioapp.utils.PaintUtils;

import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.LOGD;

/**
 * Created by smcgi_000 on 5/9/2016.
 */
public class StepListView extends LinearLayout {
    private static final String TAG = StepListView.class.getSimpleName();

    private final int mProgressColor;

    private Paint mBGPaint;
    private Paint mStepLinePaint;
    private Paint mStepProgressPaint;

    private Path mBGPath;
    private Path mProgressPath;

    private float mShadowWidth;
    private float mShadowY;

    private int mProgressStep;
    private float mProgressFactor;
    private boolean mProgressInvalid;

    private int mCurrentDisplayedProgressHeight;
    private int mGoToProgressHeight;
    private int mProgressHeightDelta;

    private int mPadding;
    private int mLeftY;
    private int mWidth;
    private int mHeight;
    private int mArrowHeight;

    private long mStartTime;

    private TextView[] mLabels;
    private Rect[] mLabelPositions;

    private OnProgressAnimationListener mProgressAnimationListener;

    private final int mStepLabelSize;
    private final int mStepTextSize;

    private Paint mLabelPaint;
    private Paint mLabelDonePaint;
    private Paint mTextPaint;
    private Paint mTextDonePaint;

    public interface OnProgressAnimationListener {
        public void onProgressAnimationFinished();
    }

    public StepListView(Context context) {
        this(context, null);
    }

    public StepListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setWillNotDraw(false);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.PromptList, 0, 0);

        mProgressColor = a.getColor(R.styleable.PromptList_progressColor,
                ContextCompat.getColor(context, R.color.colorPrimary));

        a.recycle();

        mShadowWidth = PaintUtils.getShadowBlur(2, context);
        mShadowY = PaintUtils.getShadowY(2, context);

        mBGPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        ColorDrawable bg = (ColorDrawable) getBackground();
        int colorValue = bg.getColor();

        setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));

        mBGPaint.setColor(colorValue);
        mBGPaint.setStyle(Paint.Style.FILL);
        PaintUtils.setShadowLayer(mBGPaint, 2, context); // TODO: base shadow layers off elevation values?

        mBGPath = new Path();

        mStepLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStepLinePaint.setColor(ContextCompat.getColor(context, R.color.textColorPrimary));
        mStepLinePaint.setAlpha(128);
        mStepLinePaint.setStyle(Paint.Style.STROKE);
        mStepLinePaint.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.step_list_line_width));
        PaintUtils.setShadowLayer(mStepLinePaint, 0, context);

        mStepProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStepProgressPaint.setColor(mProgressColor);
        mStepProgressPaint.setStyle(Paint.Style.FILL);

        mProgressPath = new Path();

        resetDimensions();

        mCurrentDisplayedProgressHeight = 0;
        mGoToProgressHeight = -1;
        mProgressHeightDelta = 0;

        mProgressStep = -1;
        mProgressFactor = -1;
        mProgressInvalid = false;

        mStepLabelSize = context.getResources()
                .getDimensionPixelSize(R.dimen.step_list_label_font_size);
        mStepTextSize = context.getResources()
                .getDimensionPixelSize(R.dimen.step_list_font_size);

        mLabelPaint = new Paint();
        mLabelPaint.setColor(mProgressColor);
        mLabelPaint.setStyle(Paint.Style.FILL);
        mLabelPaint.setTextSize(mStepLabelSize);

        mLabelDonePaint = new Paint();
        mLabelDonePaint.setColor(ContextCompat.getColor(getContext(), R.color.textColorPrimary));
        mLabelDonePaint.setStyle(Paint.Style.FILL);
        mLabelDonePaint.setTextSize(mStepLabelSize);

        mTextPaint = new Paint();
        mTextPaint.setColor(mProgressColor);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(mStepTextSize);
        mTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        mTextPaint.setShadowLayer(2, 0, 4, ContextCompat.getColor(getContext(), R.color.black_alpha_200));

        mTextDonePaint = new Paint();
        mTextDonePaint.setColor(ContextCompat.getColor(getContext(), R.color.textColorPrimary));
        mTextDonePaint.setStyle(Paint.Style.FILL);
        mTextDonePaint.setTextSize(mStepTextSize);
        mTextDonePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        mTextDonePaint.setShadowLayer(2, 0, 4, ContextCompat.getColor(getContext(), R.color.shadow));
    }

    public void setTopPadding(int top) {
        setPadding(0, top, 0, getPaddingBottom());
        invalidate();
    }

    public void setCurrentProgressHeight(int h) {
        mCurrentDisplayedProgressHeight = h;
    }

    public int getCurrentProgressHeight() {
        return mCurrentDisplayedProgressHeight;
    }

    private void resetDimensions() {
        mPadding = 0;
        mLeftY = 0;
        mWidth = 0;
        mHeight = 0;
        mArrowHeight = getResources().getDimensionPixelSize(R.dimen.step_list_item_padding_vertical);
        mStartTime = 0;
    }

    private void setupDimensions() {
        View lastStep = this.findViewById(R.id.step_4);
        RelativeLayout parent = (RelativeLayout) getParent();
        View button = parent.findViewById(R.id.button_outline);

        int lastItemWidth = lastStep.getMeasuredWidth();

        if ((float) lastItemWidth / (float) mWidth < 0.6) {
            lastItemWidth = (int)(mWidth * 0.6);
            //lastStep.setMinimumWidth(lastItemWidth);
        }
        int margin = 0;

        mPadding = ((mWidth - lastItemWidth) / 2) - margin;

        // set the outline button width to match the bottom of the com.thespeakers_studio.thespeakersstudioapp.view
        button.setMinimumWidth(lastItemWidth);

        mLeftY = (int) ((mHeight - mArrowHeight) - (mShadowWidth + mShadowY));

        // force the progress height to recalculate
        mProgressInvalid = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mLabels == null || mLabelPositions == null) {
            mLabels = new TextView[] {
                    (TextView) findViewById(R.id.step_1_label),
                    (TextView) findViewById(R.id.step_1_text),
                    (TextView) findViewById(R.id.step_2_label),
                    (TextView) findViewById(R.id.step_2_text),
                    (TextView) findViewById(R.id.step_3_label),
                    (TextView) findViewById(R.id.step_3_text),
                    (TextView) findViewById(R.id.step_4_label),
                    (TextView) findViewById(R.id.step_4_text)
            };

            // TODO: these should just be rendered into the view, we shouldn't be using views
            mLabelPositions = new Rect[8];
            Resources r = getContext().getResources();
            int prevBottom = 0;
            for(int x = 0; x < mLabels.length; x++) {
                LinearLayout parent = (LinearLayout) mLabels[x].getParent();

                int left = mLabels[x].getLeft() + parent.getLeft()
                        + r.getDimensionPixelSize(R.dimen.step_list_item_padding_horizontal);
                int top = mLabels[x].getTop() + parent.getTop()
                        + r.getDimensionPixelSize(R.dimen.step_list_item_padding_vertical);

                int right = left + mLabels[x].getWidth();
                int bottom = top + mLabels[x].getHeight();

                if (x % 2 == 0) {
                    prevBottom = mLabels[x].getHeight();
                } else {
                    top += prevBottom + 5;
                }

                mLabelPositions[x] = new Rect(left, top, right, bottom);
            }
        }
        if (mWidth == 0) {
            mWidth = canvas.getWidth();
        }
        if (mHeight == 0) {
            mHeight = canvas.getHeight();
        }

        if (mPadding == 0 || mLeftY == 0) {
            setupDimensions();
        }

        int shadow = 0; //(int) (mShadowWidth + mShadowY);
        int x0 = 0 - shadow;
        int x1 = mWidth + shadow;

        int leftX = mPadding;
        float leftM = (float) (leftX + shadow) / (float) mLeftY;
        float rightX = mWidth - mPadding;
        float rightY = mLeftY; //(height - arrowHeight) - (mShadowWidth + mShadowY);
        float rightM = (mPadding + shadow) / rightY;

        mBGPath.reset();
        mBGPath.moveTo(x0, 0);
        mBGPath.lineTo(x1, 0);
        mBGPath.lineTo(rightX, rightY);
        mBGPath.lineTo(mWidth / 2, mHeight - (mShadowWidth + mShadowY));
        mBGPath.lineTo(leftX, mLeftY);
        mBGPath.close();

        canvas.drawPath(mBGPath, mBGPaint);

        if (mProgressInvalid) {
            int prevStepBottom = mProgressStep > 1 ? getStepBottom(mProgressStep - 1) : 0;

            mGoToProgressHeight = prevStepBottom +
                    (int) ((getStepBottom(mProgressStep) - prevStepBottom) * mProgressFactor);

            mProgressHeightDelta = mGoToProgressHeight - mCurrentDisplayedProgressHeight;

            mStartTime = System.currentTimeMillis();
            mProgressInvalid = false;
        }

        // drawing the progress indicator
        //getProgressHeight();
        if (mGoToProgressHeight != mCurrentDisplayedProgressHeight) {
            // animate to there
            int duration = (int) ((SettingsUtils.PROMPT_PROGRESS_ANIMATION_DURATION * 2) * Math.abs((double)mProgressHeightDelta / (double)300));

            long elapsedTime = System.currentTimeMillis() - mStartTime;
            if (elapsedTime > duration) {
                elapsedTime = duration;
            }
            float interval = ((float) elapsedTime) / ((float) duration);

            //Log.d("SS", "Elapsed time " + elapsedTime + " " + mCurrentDisplayedProgressHeight + " " + mProgressHeightDelta + " " + interval);
            drawProgress(canvas, mCurrentDisplayedProgressHeight + (int)(mProgressHeightDelta * interval));

            if (elapsedTime < duration) {
                this.postInvalidateDelayed(1000 / PaintUtils.FRAMES_PER_SECOND);
            } else {
                mCurrentDisplayedProgressHeight = mGoToProgressHeight;
                mProgressHeightDelta = 0;
                mStartTime = System.currentTimeMillis();

                // delaying this one more time will give a bit of a pause before the "animation completed" event is triggered
                this.postInvalidateDelayed(800);
            }
        } else {
            // just draw it there
            drawProgress(canvas, mCurrentDisplayedProgressHeight);

            // if we have a start time but we don't need to animate, we should still have a bit of a delay
            if (mStartTime > 0) {
                long elapsedTime = System.currentTimeMillis() - mStartTime;
                if (elapsedTime > 700) {
                    mStartTime = 0;
                    if (mProgressAnimationListener != null) {
                        mProgressAnimationListener.onProgressAnimationFinished();
                    }
                } else {
                    this.postInvalidateDelayed(800);
                }
            }
        }

        // draw the lines between each thing
        drawStepLine(canvas, 0, mWidth, leftM, rightM);
        drawStepLine(canvas, 1, mWidth, leftM, rightM);
        drawStepLine(canvas, 2, mWidth, leftM, rightM);
        drawStepLine(canvas, 3, mWidth, leftM, rightM);
    }

    public void setProgressAnimationListener (OnProgressAnimationListener listener) {
        mProgressAnimationListener = listener;
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        mHeight = h;
        setupDimensions();
    }

    private void drawProgress(Canvas canvas, int progY) {
        int shadow = 0; //(int) (mShadowWidth + mShadowY);
        int x0 = 0 - shadow;
        int x1 = mWidth + shadow;

        int leftX = mPadding;
        float leftM = (float) (leftX + shadow) / (float) mLeftY;

        float progRatio = (float) progY / (float) mLeftY;

        int progLeftX = (int) (leftM * progY) - shadow; //(int) (mPadding * progRatio) - shadow;
        int progRightX = mWidth - progLeftX;

        mProgressPath.reset();
        mProgressPath.moveTo(x0, 0); // top left
        mProgressPath.lineTo(x1, 0); // top right

        mProgressPath.lineTo(progRightX, progY); // bottom right
        mProgressPath.lineTo(mWidth / 2, progY + (mArrowHeight * progRatio)); // bottom middle
        mProgressPath.lineTo(progLeftX, progY); // bottom left

        mProgressPath.close();

        canvas.drawPath(mProgressPath, mStepProgressPaint);

        int index = 0;
        boolean label = true;
        for (TextView labelView : mLabels) {
            String text = labelView.getText().toString();
            if (!label) {
                text = text.toUpperCase();
            }

            Rect pos = mLabelPositions[index];

            int x = pos.left;
            int y = pos.top;

            Paint paintToUse;
            if (progY > y) {
                paintToUse = label ? mLabelDonePaint : mTextDonePaint;
            } else {
                paintToUse = label ? mLabelPaint : mTextPaint;
            }

            canvas.drawText(text, x, y, paintToUse);

            index++;
            label = !label;
        }

        /*
        // change the color of the text labels
        for (int x = 0; x < mLabelPositions.length; x++) {
            int resId;
            if (progY > mLabelPositions[x]) {
                resId = (x & 1) == 0 ? R.style.StepListLabel : R.style.StepListText;
            } else {
                resId = (x & 1) == 0 ? R.style.StepListLabel_Inverted : R.style.StepListText_Inverted;
            }
            Utils.setTextAppearance(getContext(), mLabels[x], resId);
        }
        */
    }

    public void setProgressHeight(int step, float progress) {
        mProgressStep = step;
        mProgressFactor = progress;
        mProgressInvalid = true;

        invalidate();
    }

    public void resetProgressHeight() {
        mCurrentDisplayedProgressHeight = 0;
    }

    private void drawStepLine (Canvas canvas, int step, int width, float leftM, float rightM) {
        Path step1path = new Path();
        int y = getStepBottom(step);
        int shadow = 0; //(int) (mShadowWidth + mShadowY);

        int x1 = (int) (leftM * y) - shadow;
        int x2 = (int) (width - (rightM * y)) + shadow;

        step1path.moveTo(x1, y);
        step1path.lineTo(x2, y);
        canvas.drawPath(step1path, mStepLinePaint);
    }

    private int getStepBottom(int step) {
        if (step == 0) {
            return 1;
        }
        int viewId;
        switch (step) {
            case 2:
                viewId = R.id.step_2;
                break;
            case 3:
                viewId = R.id.step_3;
                break;
            case 4:
                viewId = R.id.step_4;
                break;
            default:
                viewId = R.id.step_1;
                break;
        }
        return findViewById(viewId).getBottom();
    }
}
