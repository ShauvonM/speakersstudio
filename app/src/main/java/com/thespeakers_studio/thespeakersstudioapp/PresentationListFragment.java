package com.thespeakers_studio.thespeakersstudioapp;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;

/**
 * Created by smcgi_000 on 7/1/2016.
 */
public class PresentationListFragment extends Fragment implements
        View.OnClickListener,
        PresentationListViewHolder.OnPresentationCardClickedListener {

    private PresentationListFragmentHandler mListener;
    private RelativeLayout mView;
    private ArrayList<PresentationData> mPresentationList;
    private PresentationListAdapter mAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (PresentationListFragmentHandler) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement PresentationListFragmentHandler");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = (RelativeLayout) inflater.inflate(R.layout.fragment_presentation_list, container, false);

        RecyclerView list = (RecyclerView) mView.findViewById(R.id.presentation_list);
        list.setHasFixedSize(true);
        //LinearLayoutManager linearLayoutManager = new LinearLayoutManager(container.getContext());
        //linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        //list.setLayoutManager(linearLayoutManager);
        list.setLayoutManager(new StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL));

        mAdapter = new PresentationListAdapter(mPresentationList, this);
        list.setAdapter(mAdapter);
        list.addItemDecoration(new PresentationListSpanItemDecoration(
                container.getResources().getDimensionPixelSize(R.dimen.pres_list_card_padding)));

        mView.findViewById(R.id.fab).setOnClickListener(this);

        return mView;
    }

    public void refresh() {
        if (mAdapter != null) {
            if (mPresentationList.size() > 0) {

            }

            mAdapter.notifyDataSetChanged();
        }
    }

    public void setPresentationData(ArrayList<PresentationData> data) {
        mPresentationList = data;
    }

    @Override
    public void onPresentationSelected(String presentationId) {
        mListener.onOpenPresentation(presentationId);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab) {
            mListener.onCreateNewPresentation();
        }
    }

    public interface PresentationListFragmentHandler {
        public void onCreateNewPresentation();
        public void onOpenPresentation(String presentationId);
    }
}
