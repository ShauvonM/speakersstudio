package com.thespeakers_studio.thespeakersstudioapp;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import org.w3c.dom.Attr;

/**
 * Created by smcgi_000 on 6/7/2016.
 */
public class PromptListRecyclerView extends RecyclerView {

    private PromptViewHolder openItem;

    public PromptListRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void onChildClicked() {
        if (openItem != null) {
            openItem.closePrompt();
        }
    }

    public void onChildOpened(PromptViewHolder vh) {
        openItem = vh;
    }

    public void onChildClosed() {
        openItem = null;
    }

    public void openNextChild(int position) {
        ((LinearLayoutManager) getLayoutManager()).scrollToPositionWithOffset(position + 1, 50);

        RelativeLayout nextChild = (RelativeLayout) getChildAt(position + 1);
        if (nextChild != null && nextChild.findViewById(R.id.card_view) != null) {
            nextChild.findViewById(R.id.card_view).callOnClick();
        } else {
            Log.d("SS", "For some reason I couldn't find a card after " + position);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        /*
        if (event.getAction() == MotionEvent.ACTION_UP) {
            View child = findChildViewUnder(event.getX(), event.getY());
            if (child != null) {
                Prompt data = (Prompt) child.getTag();
                if (data.getIsOpen()) {
                    openItem.closePrompt();
                }
            }
        }
        */
        return super.dispatchTouchEvent(event);
    }
}
