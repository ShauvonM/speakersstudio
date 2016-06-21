package com.thespeakers_studio.thespeakersstudioapp;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by smcgi_000 on 6/8/2016.
 */
public class ListItemListPromptView extends ListItemPromptView implements TextWatcher {

    private LinearLayout mListWrapper;

    public ListItemListPromptView(Context context, Prompt prompt) {
        super(context, prompt);
    }

    @Override
    void inflateView() {
        super.inflateView();
    }

    @Override
    protected void inflateEditContents(LayoutInflater inflater, LinearLayout wrapper) {
        inflater.inflate(R.layout.presentation_prompt_card_list, wrapper, true);
    }

    @Override
    void renderViews() {
        mListWrapper = (LinearLayout) findViewById(R.id.list_prompt_item_wrapper);

        ArrayList<PromptAnswer> answers = mPrompt.getAnswer();
        if (answers.size() > 0) {
            for (PromptAnswer answer : answers) {
                if (!answer.getValue().isEmpty()) {
                    inflateListItem(answer.getValue(), answer.getId());
                }
            }
        } else {
            // default to three list items
            inflateListItem();
            inflateListItem();
            inflateListItem();
        }

        findViewById(R.id.list_prompt_add).setOnClickListener(this);

        super.renderViews();
    }

    @Override
    protected void promptSaved() {
        // set the IDs where they need to be
        for(int cnt = 0; cnt < mListWrapper.getChildCount(); cnt++) {
            RelativeLayout layout = (RelativeLayout) mListWrapper.getChildAt(cnt);
            //String id = (String) layout.getTag();
            PromptAnswer answer = mPrompt.getAnswerByKey("list_" + cnt);
            layout.setTag(answer.getId());
        }
    }

    private void inflateListItem (String text, String id) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        inflater.inflate(R.layout.presentation_prompt_card_list_item, mListWrapper, true);

        RelativeLayout layout = (RelativeLayout) mListWrapper.getChildAt(mListWrapper.getChildCount() - 1);
        EditText input = (EditText) layout.findViewById(R.id.prompt_input);

        // store the ID so it stays tied to this list item even if it changes position
        layout.setTag(id);

        // the remove button
        layout.findViewById(R.id.list_prompt_remove).setOnClickListener(this);

        // set the text in the input element
        if (!text.isEmpty()) {
            input.setText(text);
        }

        // set up the text listener
        input.addTextChangedListener(this);

        // add a hint to the item
        //input.setHint("Item " + mListWrapper.getChildCount());

        // impose the character limit
        if (mPrompt.getCharLimit() > 0) {
            InputFilter[] fa = new InputFilter[1];
            fa[0] = new InputFilter.LengthFilter(mPrompt.getCharLimit());
            input.setFilters(fa);
        }
    }
    private void inflateListItem() {
        inflateListItem("", "");
    }

    private void removeListItem(View item) {
        mListWrapper.removeView(item);
        if (mListWrapper.getChildCount() == 0) {
            // we can't have no items in the list, so add a new blank one now
            inflateListItem();
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.list_prompt_add:
                inflateListItem();
                setSaveButtonIcon();
                break;
            case R.id.list_prompt_remove:
                removeListItem((RelativeLayout) v.getParent());
                setSaveButtonIcon();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    protected String processAnswer() {
        ArrayList<PromptAnswer> answers = mPrompt.getAnswer();
        String text = "";
        for(int cnt = 0; cnt < answers.size(); cnt++) {
            PromptAnswer answer = answers.get(cnt);

            if (!answer.getValue().isEmpty()) {
                if (cnt > 0) {
                    text += ", ";
                    if (cnt == answers.size() - 1) {
                        text += getResources().getString(R.string.list_and) + " ";
                    }
                }
                text += answer.getValue();
            }
        }
        return text;
    }

    @Override
    protected ArrayList<PromptAnswer> getUserInput() {
        ArrayList<PromptAnswer> answers = new ArrayList<>();
        for(int cnt = 0; cnt < mListWrapper.getChildCount(); cnt++) {
            RelativeLayout layout = (RelativeLayout) mListWrapper.getChildAt(cnt);
            EditText input = (EditText) layout.findViewById(R.id.prompt_input);
            String id = (String) layout.getTag();
            String text = input.getText().toString().trim();
            if (!text.isEmpty()) {
                answers.add(new PromptAnswer (id, "list_" + cnt, text, mPrompt.getId(), "", "", "", ""));
            }
        }
        return answers;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        setSaveButtonIcon();
    }

}
