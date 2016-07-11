package com.thespeakers_studio.thespeakersstudioapp;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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

    private void checkCompletion() {
        if (mPresentation == null) {
            return;
        }

        float completion = mPresentation.getCompletionPercentage();
        if (completion == 1) {
            mView.findViewById(R.id.button_outline).setEnabled(true);
            mView.findViewById(R.id.button_outline).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.completedPromptBG));
        } else {
            mView.findViewById(R.id.button_outline).setEnabled(false);
            mView.findViewById(R.id.button_outline).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.common_google_signin_btn_text_dark_disabled));
        }
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
        checkCompletion();

        int currentStep = mPresentation.getCurrentStep();
        float currentStepProgress = mPresentation.getCompletionPercentage(currentStep);

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
                mStepSelectedListener.onStepSelected(5);
                break;
        }
    }
}
