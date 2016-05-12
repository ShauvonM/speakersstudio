package com.thespeakers_studio.thespeakersstudioapp;

import android.support.v7.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toolbar;

/**
 * Created by smcgi_000 on 5/9/2016.
 */
public class PresentationStepListFragment extends Fragment implements View.OnClickListener {
    private View mView;
    private ActionBar mToolbar;
    OnStepSelectedListener mCallback;

    public interface OnStepSelectedListener {
        public void onStepSelected(int step);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (OnStepSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.view_presentation_step_list, container, false);

        mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        mToolbar.setDisplayHomeAsUpEnabled(false);

        mView.findViewById(R.id.step_1).setOnClickListener(this);
        mView.findViewById(R.id.step_2).setOnClickListener(this);
        mView.findViewById(R.id.step_3).setOnClickListener(this);
        mView.findViewById(R.id.step_4).setOnClickListener(this);
        mView.findViewById(R.id.button_outline).setOnClickListener(this);

        return mView;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case (R.id.step_1):
                mCallback.onStepSelected(1);
                break;
            case (R.id.step_2):
                mCallback.onStepSelected(2);
                break;
            case (R.id.step_3):
                mCallback.onStepSelected(3);
                break;
            case (R.id.step_4):
                mCallback.onStepSelected(4);
                break;
            case (R.id.button_outline):
                mCallback.onStepSelected(5);
                break;
        }
    }
}
