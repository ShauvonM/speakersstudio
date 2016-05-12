package com.thespeakers_studio.thespeakersstudioapp;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by smcgi_000 on 5/9/2016.
 */
public class StepListView extends LinearLayout {
    private Paint mBGPaint;
    private Paint mStepLinePaint;
    private Path mBGPath;
    private float mShadowWidth;
    private float mShadowY;

    public StepListView(Context context) {
        this(context, null);
    }

    public StepListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setWillNotDraw(false);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        mShadowWidth = ShadowHelper.getShadowBlur(2, context);
        mShadowY = ShadowHelper.getShadowY(2, context);

        mBGPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBGPaint.setColor(ContextCompat.getColor(context, R.color.stepListBG));
        mBGPaint.setStyle(Paint.Style.FILL);
        ShadowHelper.setShadowLayer(mBGPaint, 2, context);

        mBGPath = new Path();

        mStepLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStepLinePaint.setColor(ContextCompat.getColor(context, R.color.textColorPrimary));
        mStepLinePaint.setAlpha(128);
        mStepLinePaint.setStyle(Paint.Style.STROKE);
        mStepLinePaint.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.step_list_line_width));
        ShadowHelper.setShadowLayer(mStepLinePaint, 0, context);
    }

    private static float dpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        View lastStep = this.findViewById(R.id.step_4);
        int lastItemWidth = lastStep.getMeasuredWidth();
        int margin = 0; //(int) getResources().getDimensionPixelSize(R.dimen.step_list_item_margin);
        int arrowHeight = (int) getResources().getDimensionPixelSize(R.dimen.step_list_item_padding_vertical);

        int padding = ((width - lastItemWidth) / 2) - margin;

        float leftX = padding;
        float leftY = (height - arrowHeight) - (mShadowWidth + mShadowY);
        float leftM = leftX / leftY;
        float rightX = width - padding;
        float rightY = (height - arrowHeight) - (mShadowWidth + mShadowY);
        float rightM = padding / rightY;

        mBGPath.reset();
        mBGPath.moveTo(0, 0);
        mBGPath.lineTo(width, 0);
        mBGPath.lineTo(rightX, rightY);
        mBGPath.lineTo(width / 2, height - (mShadowWidth + mShadowY));
        mBGPath.lineTo(leftX, leftY);
        mBGPath.close();

        canvas.drawPath(mBGPath, mBGPaint);

        drawStepLine(canvas, findViewById(R.id.step_1), width, leftM, rightM);
        drawStepLine(canvas, findViewById(R.id.step_2), width, leftM, rightM);
        drawStepLine(canvas, findViewById(R.id.step_3), width, leftM, rightM);

        RelativeLayout parent = (RelativeLayout) getParent();
        parent.findViewById(R.id.button_outline).setMinimumWidth(lastItemWidth);
    }

    private void drawStepLine (Canvas canvas, View view, int width, float leftM, float rightM) {
        Path step1path = new Path();
        int y = (int) view.getBottom();

        int x1 = (int) (leftM * y);
        int x2 = (int) (width - (rightM * y));

        step1path.moveTo(x1, y);
        step1path.lineTo(x2, y);
        canvas.drawPath(step1path, mStepLinePaint);
    }
}
