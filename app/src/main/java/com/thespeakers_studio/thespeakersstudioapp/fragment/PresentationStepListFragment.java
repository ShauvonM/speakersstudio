package com.thespeakers_studio.thespeakersstudioapp.fragment;

import android.graphics.drawable.TransitionDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thespeakers_studio.thespeakersstudioapp.model.PresentationData;
import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.settings.SettingsUtils;
import com.thespeakers_studio.thespeakersstudioapp.ui.StepListView;
import com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils;

/**
 * Created by smcgi_000 on 5/9/2016.
 */
public class PresentationStepListFragment extends Fragment implements View.OnClickListener,
        StepListView.OnProgressAnimationListener {

    public static final String TAG = LogUtils.makeLogTag(PresentationStepListFragment.class);

    private View mView;
    private StepListView mStepListView;
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
            throw new ClassCastException(context.toString() + " must implement OnStepSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_step_list, container, false);

        mStepListView = (StepListView) findViewById(R.id.step_list);

        setButtonClick(R.id.step_1);
        setButtonClick(R.id.step_2);
        setButtonClick(R.id.step_3);
        setButtonClick(R.id.step_4);
        setButtonClick(R.id.button_outline);

        mStepListView.setProgressAnimationListener(this);

        return mView;
    }

    private View findViewById(int id) {
        if (mView == null) {
            return null;
        }
        return mView.findViewById(id);
    }

    private void setButtonClick(int id) {
        View v = findViewById(id);
        if (v != null) {
            v.setOnClickListener(this);
        }
    }

    private void checkCompletion() {
        if (mPresentation == null) {
            return;
        }

        float completion = mPresentation.getCompletionPercentage();
        View button = findViewById(R.id.button_outline);
        TransitionDrawable transition = (TransitionDrawable) button.getBackground();
        if (button != null) {
            if (completion == 1) {
                button.setEnabled(true);
                //button.setBackgroundColor(ContextCompat.getColor(this, R.color.outlineBG));
                transition.startTransition(SettingsUtils.OUTLINE_BUTTON_TRANSITION_DURATION);
            } else {
                button.setEnabled(false);
                //button.setBackgroundColor(ContextCompat.getColor(this, R.color.common_google_signin_btn_text_dark_disabled));
                //transition.reverseTransition(SettingsUtils.OUTLINE_BUTTON_TRANSITION_DURATION);
            }
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
        if (mStepListView != null) {
            mStepListView.resetProgressHeight();
        }
    }

    public void animateProgressHeight() {
        checkCompletion();

        int currentStep = mPresentation.getCurrentStep();
        float currentStepProgress = mPresentation.getCompletionPercentage(currentStep);

        mStepListView.setProgressHeight(currentStep, currentStepProgress);
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
