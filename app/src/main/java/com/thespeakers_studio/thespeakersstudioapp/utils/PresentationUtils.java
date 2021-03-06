package com.thespeakers_studio.thespeakersstudioapp.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;

import com.thebluealliance.spectrum.SpectrumDialog;
import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.model.PresentationData;

/**
 * Created by smcgi_000 on 8/11/2016.
 */
public class PresentationUtils {
    public static void showColorDialog(
            AppCompatActivity context, final PresentationData presentation,
            SpectrumDialog.OnColorSelectedListener callback) {

        SpectrumDialog dialog = new SpectrumDialog.Builder(context)
                .setColors(R.array.presentation_options)
                .setSelectedColor(presentation.getColor())
                .build();
        dialog.setOnColorSelectedListener(callback);
        dialog.show(context.getSupportFragmentManager(), "color_picker");
    }

    public static void resetPresentation(Context context,
                                         PresentationData presentation,
                                             View view,
                                         DialogInterface.OnClickListener callback) {
        if (presentation == null) {
            return;
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setMessage(context.getString(R.string.confirm_reset_message));

        if (view != null) {
            dialog.setView(view);
        }

        dialog.setPositiveButton(context.getString(R.string.yes), callback)
                .setNegativeButton(context.getString(R.string.no), null)
                .show();
    }

    public static String getStepNameFromId (Context context, int id) {
        String headerText;
        switch(id) {
            case 1:
                headerText = context.getString(R.string.details);
                break;
            case 2:
                headerText = context.getString(R.string.landscape);
                break;
            case 3:
                headerText = context.getString(R.string.specifics);
                break;
            case 4:
                headerText = context.getString(R.string.content);
                break;
            default:
                headerText = "";
                break;
        }
        return headerText;
    }

    public static String getStepLabelFromId (Context context, int id) {
        String label;
        switch(id) {
            case 1:
                label = context.getString(R.string.step_1);
                break;
            case 2:
                label = context.getString(R.string.step_2);
                break;
            case 3:
                label = context.getString(R.string.step_3);
                break;
            case 4:
                label = context.getString(R.string.step_4);
                break;
            default:
                label = "";
                break;
        }
        return label;
    }

    public static int getThemeForColor(Context context, int color) {
        if (color == ContextCompat.getColor(context, R.color.presentationColor1)) {
            return R.style.AppTheme_Inverted_EditPresentation_presentation1;
        } else if (color == ContextCompat.getColor(context, R.color.presentationColor2)) {
            return R.style.AppTheme_Inverted_EditPresentation_presentation2;
        } else if (color == ContextCompat.getColor(context, R.color.presentationColor3)) {
            return R.style.AppTheme_Inverted_EditPresentation_presentation3;
        } else if (color == ContextCompat.getColor(context, R.color.presentationColor4)) {
            return R.style.AppTheme_Inverted_EditPresentation_presentation4;
        } else if (color == ContextCompat.getColor(context, R.color.presentationColor5)) {
            return R.style.AppTheme_Inverted_EditPresentation_presentation5;
        } else if (color == ContextCompat.getColor(context, R.color.presentationColor6)) {
            return R.style.AppTheme_Inverted_EditPresentation_presentation6;
        } else if (color == ContextCompat.getColor(context, R.color.presentationColor7)) {
            return R.style.AppTheme_Inverted_EditPresentation_presentation7;
        } else if (color == ContextCompat.getColor(context, R.color.presentationColor8)) {
            return R.style.AppTheme_Inverted_EditPresentation_presentation8;
        } else if (color == ContextCompat.getColor(context, R.color.presentationColor9)) {
            return R.style.AppTheme_Inverted_EditPresentation_presentation9;
        } else if (color == ContextCompat.getColor(context, R.color.presentationColor10)) {
            return R.style.AppTheme_Inverted_EditPresentation_presentation10;
        } else if (color == ContextCompat.getColor(context, R.color.presentationColor11)) {
            return R.style.AppTheme_Inverted_EditPresentation_presentation11;
        } else if (color == ContextCompat.getColor(context, R.color.presentationColor12)) {
            return R.style.AppTheme_Inverted_EditPresentation_presentation12;
        } else if (color == ContextCompat.getColor(context, R.color.presentationColor13)) {
            return R.style.AppTheme_Inverted_EditPresentation_presentation13;
        } else if (color == ContextCompat.getColor(context, R.color.presentationColor14)) {
            return R.style.AppTheme_Inverted_EditPresentation_presentation14;
        } else if (color == ContextCompat.getColor(context, R.color.presentationColor15)) {
            return R.style.AppTheme_Inverted_EditPresentation_presentation15;
        } else if (color == ContextCompat.getColor(context, R.color.presentationColor16)) {
            return R.style.AppTheme_Inverted_EditPresentation_presentation16;
        } else if (color == ContextCompat.getColor(context, R.color.presentationColor17)) {
            return R.style.AppTheme_Inverted_EditPresentation_presentation17;
        } else {
            return 0;
        }
    }
}
