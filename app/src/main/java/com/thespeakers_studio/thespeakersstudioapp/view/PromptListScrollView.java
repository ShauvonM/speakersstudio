package com.thespeakers_studio.thespeakersstudioapp.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Created by smcgi_000 on 5/10/2016.
 */
public class PromptListScrollView extends ScrollView {
    private Paint mDropshadow;

    public PromptListScrollView(Context context) {
        this(context, null);
    }
    public PromptListScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //mDropshadow = new Paint();
        //Utils.setShadowLayer(mDropshadow, 2, context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

}
