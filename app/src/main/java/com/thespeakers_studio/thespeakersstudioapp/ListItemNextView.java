package com.thespeakers_studio.thespeakersstudioapp;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by smcgi_000 on 6/9/2016.
 */
public class ListItemNextView extends ListItemView {
    public ListItemNextView(Context context, Prompt prompt) {
        super(context, prompt);
    }

    @Override
    void inflateView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.presentation_next_card, this, true);
    }

    @Override
    void renderViews() {
        FloatingActionButton btn = (FloatingActionButton) findViewById(R.id.next_action_button);
        btn.setOnClickListener(this);
        btn.setEnabled(false);
        btn.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.prompt_list_fab_states));
    }

    @Override
    public void enable() {
        super.enable();
        FloatingActionButton btn = (FloatingActionButton) findViewById(R.id.next_action_button);
        btn.setEnabled(true);
        btn.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.prompt_list_fab_enabled));
        btn.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_keyboard_arrow_right_white_24dp));
    }

    @Override
    public void onClick(View v) {
        Log.d("SS", "Next was clicked.");
        goToNext();
    }
}
