package com.thespeakers_studio.thespeakersstudioapp;

import android.support.v7.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
    private PromptSaveListener mListener;

    private int mStep;
    private ArrayList<Prompt> mPromptData;

    private PromptListView mPromptList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_prompt_list, container, false);

        mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        mToolbar.setDisplayHomeAsUpEnabled(true);

        // gather up the prompt data
        mStep = getArguments().getInt("step");
        mPromptData = ((PresentationMainActivity) getActivity()).getPresentation().getPromptsForStep(mStep);

        Log.d("SS", "Step count: " + mPromptData.size());

        mPromptList = (PromptListView) mView.findViewById(R.id.prompt_list);
        mPromptList.setPromptListListener(this);
        mPromptList.setData(mPromptData);

        return mView;
    }

    public void setOnPromptSaveListener (PromptSaveListener listener) {
        mListener = listener;
    }

    @Override
    public void onPause() {
        super.onPause();

        // make sure all of the prompts are set to "closed"
        for (Prompt p : mPromptData) {
            if (p.getIsOpen()){
                p.toggleOpen();
            }
        }
    }

    @Override
    public void onSaveItem(Prompt prompt) {
        if (mListener != null) {
            mListener.onPromptSave(prompt);
        }
    }

    public interface PromptSaveListener {
        public void onPromptSave (Prompt prompt);
    }
}
