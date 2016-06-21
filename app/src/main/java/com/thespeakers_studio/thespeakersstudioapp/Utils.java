package com.thespeakers_studio.thespeakersstudioapp;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;

/**
 * Created by smcgi_000 on 5/10/2016.
 */
public class Utils {

    public static final int PROMPT_PROGRESS_ANIMATION_DURATION = 300;
    public static final int FRAMES_PER_SECOND = 60;

    public static float dpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static int getShadowY(int level, Context context) {
        int shadowY = -1;

        switch(level) {
            case 1:
                shadowY = 1;
                break;
            case 2:
                shadowY = 3;
                break;
            case 3:
                shadowY = 10;
                break;
            case 4:
                shadowY = 14;
                break;
            case 5:
                shadowY = 19;
                break;
        }
        return (int) dpToPixel(shadowY, context);
    }

    public static int getShadowBlur(int level, Context context) {
        int shadowBlur = 1;

        switch(level) {
            case 1:
                shadowBlur = 3;
                break;
            case 2:
                shadowBlur = 6;
                break;
            case 3:
                shadowBlur = 20;
                break;
            case 4:
                shadowBlur = 28;
                break;
            case 5:
                shadowBlur = 38;
                break;
        }
        return (int) dpToPixel(shadowBlur, context);
    }

    public static Paint setShadowLayer(Paint paint, int level, Context context) {
        int shadowY = getShadowY(level, context);
        int shadowBlur = getShadowBlur(level, context);

        paint.setShadowLayer(shadowBlur, 0, shadowY, ContextCompat.getColor(context, R.color.shadow));

        return paint;
    }
}
