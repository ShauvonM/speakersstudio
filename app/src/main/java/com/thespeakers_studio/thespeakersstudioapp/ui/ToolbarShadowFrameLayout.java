package com.thespeakers_studio.thespeakersstudioapp.ui;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Property;
import android.widget.FrameLayout;

import com.thespeakers_studio.thespeakersstudioapp.R;

/**
 * Created by smcgi_000 on 8/1/2016.
 *
 * Adapted from the open source Google I/O app (boy is it great when Google actually shows us how apps should be made)
 */
public class ToolbarShadowFrameLayout extends FrameLayout {
    private Drawable mShadowDrawable;
    private NinePatchDrawable mShadowNinePatchDrawable;
    private int mShadowTopOffset;
    private boolean mShadowVisible;
    private int mWidth, mHeight;
    private ObjectAnimator mAnimator;
    private float mAlpha = 1f;

    public ToolbarShadowFrameLayout(Context context) {
        this(context, null, 0);
    }

    public ToolbarShadowFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ToolbarShadowFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ToolbarShadowFrameLayout, 0, 0);

        mShadowDrawable = a.getDrawable(R.styleable.ToolbarShadowFrameLayout_shadowDrawable);
        if (mShadowDrawable != null) {
            mShadowDrawable.setCallback(this);
            if (mShadowDrawable instanceof NinePatchDrawable) {
                mShadowNinePatchDrawable = (NinePatchDrawable) mShadowDrawable;
            }
        }

        mShadowVisible = a.getBoolean(R.styleable.ToolbarShadowFrameLayout_shadowVisible, true);
        setWillNotDraw(!mShadowVisible || mShadowDrawable == null);

        a.recycle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = w;
        updateShadowBounds();
    }

    private void updateShadowBounds() {
        if (mShadowDrawable != null) {
            mShadowDrawable.setBounds(0, mShadowTopOffset, mWidth, mHeight);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mShadowDrawable != null && mShadowVisible) {
            if (mShadowNinePatchDrawable != null) {
                mShadowNinePatchDrawable.getPaint().setAlpha((int) (255 * mAlpha));
            }
            mShadowDrawable.draw(canvas);
        }
    }

    public void setShadowTopOffset(int shadowTopOffset) {
        this.mShadowTopOffset = shadowTopOffset;
        updateShadowBounds();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void setShadowVisible(boolean shadowVisible, boolean animate) {
        this.mShadowVisible = shadowVisible;
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator = null;
        }

        if (animate && mShadowDrawable != null) {
            mAnimator = ObjectAnimator.ofFloat(this, SHADOW_ALPHA,
                    shadowVisible ? 0f : 1f,
                    shadowVisible ? 1f : 0f);
            mAnimator.setDuration(1000);
            mAnimator.start();
        }

        ViewCompat.postInvalidateOnAnimation(this);
        setWillNotDraw(!mShadowVisible || mShadowDrawable == null);
    }

    private static Property<ToolbarShadowFrameLayout, Float> SHADOW_ALPHA =
            new Property<ToolbarShadowFrameLayout, Float>(Float.class, "shadowAlpha") {
        @Override
        public Float get(ToolbarShadowFrameLayout object) {
            return object.mAlpha;
        }

        @Override
        public void set(ToolbarShadowFrameLayout object, Float value) {
            object.mAlpha = value;
            ViewCompat.postInvalidateOnAnimation(object);
        }
    };
}
