package com.thespeakers_studio.thespeakersstudioapp.ui.ListItemViewSubClasses;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;
import com.thespeakers_studio.thespeakersstudioapp.model.Prompt;
import com.thespeakers_studio.thespeakersstudioapp.model.PromptAnswer;

/**
 * Created by smcgi_000 on 6/8/2016.
 */
public class ListItemListPromptView extends ListItemPromptView implements View.OnFocusChangeListener {

    private LinearLayout mListWrapper;
    // default to three list items
    private final int DEFAULT_LIST_COUNT = 3;

    public ListItemListPromptView(Context context, Prompt prompt) {
        super(context, prompt);
    }

    @Override
    protected void inflateView() {
        super.inflateView();
    }

    @Override
    protected void inflateEditContents(LayoutInflater inflater, LinearLayout wrapper) {
        inflater.inflate(R.layout.presentation_prompt_card_list, wrapper, true);
    }

    @Override
    protected void renderViews() {
        mListWrapper = (LinearLayout) findViewById(R.id.list_prompt_item_wrapper);

        ArrayList<PromptAnswer> answers = mPrompt.getAnswer();
        if (answers.size() > 0) {
            for (PromptAnswer answer : answers) {
                if (!answer.getValue().isEmpty()) {
                    inflateListItem(answer.getValue(), answer.getId());
                }
            }
        } else {
            for(int startingCount = 0; startingCount < DEFAULT_LIST_COUNT; startingCount++) {
                inflateListItem();
            }
        }

        findViewById(R.id.list_prompt_add).setOnClickListener(this);

        super.renderViews();
    }

    private void inflateListItem (String text, String id) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        inflater.inflate(R.layout.presentation_prompt_card_list_item, mListWrapper, true);

        final RelativeLayout layout = (RelativeLayout) mListWrapper.getChildAt(mListWrapper.getChildCount() - 1);
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
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setSaveButtonIcon();
                //((TextView) layout.findViewById(R.id.prompt_char_count)).setText(String.valueOf(s.length()));
            }
        });
        //input.setOnFocusChangeListener(this);

        // add a hint to the item
        //input.setHint("Item " + mListWrapper.getChildCount());

        // impose the character limit
        if (mPrompt.getCharLimit() > 0) {
            InputFilter[] fa = new InputFilter[1];
            fa[0] = new InputFilter.LengthFilter(mPrompt.getCharLimit());
            input.setFilters(fa);

            /*
            // set the character count
            ((TextView) layout.findViewById(R.id.prompt_char_count)).setText(String.valueOf(text.length()));
            // set the character limit
            ((TextView) layout.findViewById(R.id.prompt_char_max)).setText(String.valueOf(mPrompt.getCharLimit()));
            */
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
        return Utils.processAnswerList(answers, getResources());
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
                answers.add(new PromptAnswer (id, "list_" + cnt, text, mPrompt.getId()));
            }
        }
        return answers;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        /*
        final View output = ((RelativeLayout) v.getParent()).findViewById(R.id.list_item_char_readout);
        if (hasFocus && mPrompt.getCharLimit() > 0) {
            output.clearAnimation();
            output.setAlpha(0);

            output.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            final int h = output.getMeasuredHeight();

            Animation showCharCountAnimation = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    if (interpolatedTime < 1) {
                        output.setAlpha(interpolatedTime);
                        output.getLayoutParams().height = (int) (h * interpolatedTime);
                    } else {
                        output.setAlpha(1);
                        output.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    }
                }

                @Override
                public boolean willChangeBounds() {
                    return true;
                }
            };
            showCharCountAnimation.setDuration(400);
            output.setAnimation(showCharCountAnimation);
        } else {
            output.clearAnimation();
            output.setAlpha(1);
            final int h = output.getMeasuredHeight();
            Animation hideCharCountAnimation = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    if (interpolatedTime < 1) {
                        output.setAlpha(1 - interpolatedTime);
                        output.getLayoutParams().height = h - ((int) (h * interpolatedTime));
                    } else {
                        output.setAlpha(0);
                        output.getLayoutParams().height = 0;
                    }
                }

                @Override
                public boolean willChangeBounds() {
                    return true;
                }
            };
            hideCharCountAnimation.setDuration(400);
            output.setAnimation(hideCharCountAnimation);
        }
        */
    }
}
