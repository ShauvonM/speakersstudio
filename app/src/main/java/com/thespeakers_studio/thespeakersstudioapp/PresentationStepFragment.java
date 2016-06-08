package com.thespeakers_studio.thespeakersstudioapp;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;

/**
 * Created by smcgi_000 on 5/10/2016.
 */
public class PresentationStepFragment extends Fragment {
    private View mView;
    private ActionBar mToolbar;

    private PromptListRecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private int mStep;
    private ArrayList<Prompt> mPromptData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.view_presentation_step, container, false);

        mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        mToolbar.setDisplayHomeAsUpEnabled(true);

        mRecyclerView = (PromptListRecyclerView) mView.findViewById(R.id.step_cards);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mStep = getArguments().getInt("step");
        mPromptData = ((PresentationMainActivity) getActivity()).getPresentation().getPromptsForStep(mStep);

        // set the step data
        String headerText;
        switch(mStep) {
            case 1:
                headerText = getString(R.string.details);
                break;
            case 2:
                headerText = getString(R.string.landscape);
                break;
            case 3:
                headerText = getString(R.string.specifics);
                break;
            case 4:
                headerText = getString(R.string.content);
                break;
            default:
                headerText = "";
                break;
        }
        // the 0 index item should be the header (hopefully)
        mPromptData.get(0).setProcessedText(headerText);
        mPromptData.get(0).setStep(mStep);

        Log.d("SS", "Step count: " + mPromptData.size());

        mAdapter = new PromptDataAdapter(mPromptData);
        mRecyclerView.setAdapter(mAdapter);

        return mView;
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

}
