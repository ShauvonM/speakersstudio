package com.thespeakers_studio.thespeakersstudioapp.view;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.model.Prompt;

/**
 * Created by smcgi_000 on 6/8/2016.
 */
public class ListItemHeaderView extends ListItemView {

    public ListItemHeaderView(Context context, Prompt prompt) {
        super(context, prompt);
    }

    public void setFillFactor(float fillFactor) {
        ((PromptHeaderCardView) findViewById(R.id.header_card)).setFillFactor(fillFactor);
    }

    public void animateFillFactor(float fillFactor) {
        ((PromptHeaderCardView) findViewById(R.id.header_card)).animateFillFactor(fillFactor);
    }

    @Override
    protected void inflateView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.presentation_header_card, this, true);
    }

    @Override
    protected void renderViews() {
        String headerText;
        String label;
        switch(mPrompt.getStep()) {
            case 1:
                headerText = getContext().getString(R.string.details);
                label = getContext().getString(R.string.step_1);
                break;
            case 2:
                headerText = getContext().getString(R.string.landscape);
                label = getContext().getString(R.string.step_2);
                break;
            case 3:
                headerText = getContext().getString(R.string.specifics);
                label = getContext().getString(R.string.step_3);
                break;
            case 4:
                headerText = getContext().getString(R.string.content);
                label = getContext().getString(R.string.step_4);
                break;
            default:
                headerText = "";
                label = "";
                break;
        }

        ((TextView) findViewById(R.id.card_label)).setText(label);
        findViewById(R.id.header_card).setOnClickListener(this);
        ((TextView) findViewById(R.id.card_text)).setText(headerText);
    }

    @Override
    public void onClick(View v) {
        Log.d("SS", "Header clicked!");
    }
}
