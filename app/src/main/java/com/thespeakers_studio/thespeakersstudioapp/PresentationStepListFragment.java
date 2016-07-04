package com.thespeakers_studio.thespeakersstudioapp;

import android.support.v7.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by smcgi_000 on 5/9/2016.
 */
public class PresentationStepListFragment extends Fragment implements View.OnClickListener,
        StepListView.OnProgressAnimationListener {
    private View mView;
    private ActionBar mToolbar;
    OnStepSelectedListener mStepSelectedListener;
    StepListView.OnProgressAnimationListener mProgressAnimationListener;
    private PresentationData mPresentation;

    public interface OnStepSelectedListener {
        public void onStepSelected(int step);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mStepSelectedListener = (OnStepSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_step_list, container, false);

        mView.findViewById(R.id.step_1).setOnClickListener(this);
        mView.findViewById(R.id.step_2).setOnClickListener(this);
        mView.findViewById(R.id.step_3).setOnClickListener(this);
        mView.findViewById(R.id.step_4).setOnClickListener(this);
        mView.findViewById(R.id.button_outline).setOnClickListener(this);

        ((StepListView) mView.findViewById(R.id.step_list)).setProgressAnimationListener(this);

        return mView;
    }

    public void setPresentation(PresentationData presentation) {
        mPresentation = presentation;
    }

    public void setOnProgressAnimationListener(StepListView.OnProgressAnimationListener listener) {
        mProgressAnimationListener = listener;
    }

    public void clearOnProgressAnimationListener() {
        mProgressAnimationListener = null;
    }

    @Override
    public void onProgressAnimationFinished() {
        if (mProgressAnimationListener != null) {
            mProgressAnimationListener.onProgressAnimationFinished();
        }
    }

    public void resetProgress() {
        if (mView != null) {
            ((StepListView) mView.findViewById(R.id.step_list)).resetProgressHeight();
        }
    }

    public void animateProgressHeight() {
        int currentStep = 0;
        float currentStepProgress = 0;
        for (int step = 1; step < 5; step++) {
            if (mPresentation.getCompletionPercentage(step) < 1) {
                currentStep = step;
                currentStepProgress = mPresentation.getCompletionPercentage(step);
                step = 5;
            }
        }

        ((StepListView) mView.findViewById(R.id.step_list)).setProgressHeight(currentStep, currentStepProgress);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case (R.id.step_1):
                mStepSelectedListener.onStepSelected(1);
                break;
            case (R.id.step_2):
                mStepSelectedListener.onStepSelected(2);
                break;
            case (R.id.step_3):
                mStepSelectedListener.onStepSelected(3);
                break;
            case (R.id.step_4):
                mStepSelectedListener.onStepSelected(4);
                break;
            case (R.id.button_outline):
                Log.d("SS", "Outline button clicked");
                break;
        }
    }
}
