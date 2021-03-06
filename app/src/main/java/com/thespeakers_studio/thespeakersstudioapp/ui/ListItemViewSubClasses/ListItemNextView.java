package com.thespeakers_studio.thespeakersstudioapp.ui.ListItemViewSubClasses;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.model.Prompt;
import com.thespeakers_studio.thespeakersstudioapp.ui.ListItemView;

/**
 * Created by smcgi_000 on 6/9/2016.
 */
@SuppressLint("ViewConstructor")
public class ListItemNextView extends ListItemView {
    public ListItemNextView(Context context, Prompt prompt) {
        super(context, prompt);
    }

    @Override
    protected void inflateView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.presentation_next_card, this, true);
    }

    @Override
    protected void renderViews() {
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
