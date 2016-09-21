package com.thespeakers_studio.thespeakersstudioapp.fragment;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.transition.Transition;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thespeakers_studio.thespeakersstudioapp.activity.PresentationPromptListActivity;
import com.thespeakers_studio.thespeakersstudioapp.model.PresentationData;
import com.thespeakers_studio.thespeakersstudioapp.model.Prompt;
import com.thespeakers_studio.thespeakersstudioapp.ui.PromptListView;
import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;

import java.util.ArrayList;

import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.LOGD;
import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.makeLogTag;

/**
 * Created by smcgi_000 on 5/10/2016.
 */
public class PresentationPromptListFragment extends Fragment implements PromptListView.PromptListListener {

    public static final String TAG = makeLogTag(PresentationPromptListFragment.class);

    private View mView;
    private PromptSaveListener mPromptSaveListener;

    private PresentationData mPresentation;
    private ArrayList<Prompt> mPromptData;

    private int mStep;

    private PromptListView mPromptList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mPromptSaveListener = (PromptSaveListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement PromptSaveListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_prompt_list, container, false);

        mPromptList = (PromptListView) findViewById(R.id.prompt_list);
        mPromptList.setPromptListListener(this);

        if (mStep > 0) {
            setStep(mStep);
        }



        return mView;
    }

    private View findViewById(int id) {
        if (mView == null) {
            return null;
        }
        return mView.findViewById(id);
    }

    public void setMargin(int margin) {
        if (mPromptList == null) {
            return;
        }

        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams)
                mPromptList.getLayoutParams();
        if (mlp.topMargin != margin) {
            mlp.topMargin = margin;
            mPromptList.setLayoutParams(mlp);
        }
    }

    public void setPresentation(PresentationData presentation) {
        mPresentation = presentation;
    }

    public void setStep (int step) {
        resetStep();
        resetPrompts();
        mStep = step;
        mPromptData = mPresentation.getPromptsForStep(step);
        Log.d("SS", "Step count: " + mPromptData.size());
        mPromptList.setData(mPromptData);
    }

    public void showMorePrompts() {
        mPromptList.showMorePrompts();
    }

    public void hideInvisiblePrompts() {
        if (isVisible() && mStep == 4) {
            // only step four causes any problems
            mPromptList.hideInvisiblePrompts();
        }
    }

    private void resetStep() {
        if (mPromptList == null) {
            return;
        }
        mPromptList.clearData();
    }

    public int getStep() {
        return mStep;
    }

    public void resetPrompts() {
        if (mPromptData != null) {
            // make sure all of the prompts are set to "closed"
            for (Prompt p : mPromptData) {
                if (p.getIsOpen()) {
                    p.toggleOpen();
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        resetPrompts();
    }

    @Override
    public void onSaveItem(Prompt prompt) {
        mPromptSaveListener.onPromptSave(prompt);
    }

    @Override
    public void onNextStep(int step) {
        mPromptSaveListener.onNextStep(step);
    }

    public interface PromptSaveListener {
        public void onPromptSave (Prompt prompt);
        public void onNextStep (int step);
    }
}
