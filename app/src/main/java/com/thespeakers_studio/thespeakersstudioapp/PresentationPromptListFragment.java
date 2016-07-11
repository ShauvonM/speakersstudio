package com.thespeakers_studio.thespeakersstudioapp;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by smcgi_000 on 5/10/2016.
 */
public class PresentationPromptListFragment extends Fragment implements PromptListView.PromptListListener {
    private View mView;
    private ActionBar mToolbar;
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

        mPromptList = (PromptListView) mView.findViewById(R.id.prompt_list);
        mPromptList.setPromptListListener(this);

        if (mStep > 0) {
            setStep(mStep);
        }

        return mView;
    }

    public void setPresentation(PresentationData presentation) {
        mPresentation = presentation;
    }

    public void setStep (int step) {
        mStep = step;
        mPromptData = mPresentation.getPromptsForStep(step);
        Log.d("SS", "Step count: " + mPromptData.size());
        mPromptList.setData(mPromptData);
    }

    public int getStep() {
        return mStep;
    }

    public void clearStep() {
        mPromptData = null;
        mPromptList.clearData();
        mView.findViewById(R.id.prompt_list_wrapper).scrollTo(0, 0);
    }

    @Override
    public void onPause() {
        super.onPause();

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
