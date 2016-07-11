package com.thespeakers_studio.thespeakersstudioapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by smcgi_000 on 7/7/2016.
 */
public class PresentationOutlineFragment extends Fragment {

    private View mView;

    private PresentationData mPresentation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_outline, container, false);

        return mView;
    }

    public void setPresentation (PresentationData pres) {
        mPresentation = pres;
    }
}
