package com.thespeakers_studio.thespeakersstudioapp;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
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

    private boolean mIsTwoColumn;
    private StaggeredGridLayoutManager mTwoColumnManager;
    private LinearLayoutManager mOneColumnManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

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

        mTwoColumnManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mOneColumnManager = new LinearLayoutManager(container.getContext(),
                LinearLayoutManager.VERTICAL, false);

        SharedPreferences sp = container.getContext().getSharedPreferences("presentation_list", 0);
        mIsTwoColumn = sp.getBoolean("presentation_list_view_type", false);

        RecyclerView list = (RecyclerView) mView.findViewById(R.id.presentation_list);
        list.setHasFixedSize(true);
        mAdapter = new PresentationListAdapter(mPresentationList, this);

        toggleView(mIsTwoColumn);

        list.setAdapter(mAdapter);
        list.addItemDecoration(new PresentationListSpanItemDecoration(
                container.getResources().getDimensionPixelSize(R.dimen.pres_list_card_padding)));

        mView.findViewById(R.id.fab).setOnClickListener(this);

        showMessageIfEmpty();

        return mView;
    }

    public void toggleView(boolean set) {
        RecyclerView list = (RecyclerView) mView.findViewById(R.id.presentation_list);
        if (!set) {
            mAdapter.setIsTwoColumn(false);
            list.setLayoutManager(mOneColumnManager);
            mIsTwoColumn = false;
        } else {
            mAdapter.setIsTwoColumn(true);
            list.setLayoutManager(mTwoColumnManager);
            mIsTwoColumn = true;
        }
    }
    public boolean toggleView() {
        toggleView(!mIsTwoColumn);
        return mIsTwoColumn;
    }

    public void refresh() {
        if (mAdapter != null) {
            if (mPresentationList.size() > 0) {
                // TODO: re-sort the list of presentations by modified date
            }

            mAdapter.notifyDataSetChanged();

            showMessageIfEmpty();
        }
    }

    public void showMessageIfEmpty() {
        if (mPresentationList.size() == 0) {
            mView.findViewById(R.id.no_presentations).setVisibility(View.VISIBLE);
        } else {
            mView.findViewById(R.id.no_presentations).setVisibility(View.GONE);
        }
    }

    public void setPresentationData(ArrayList<PresentationData> data) {
        mPresentationList = data;
    }
    public ArrayList<PresentationData> getPresentationData() {
        return mPresentationList;
    }

    public void deselectAll() {
        mAdapter.deselectAll();
        for (PresentationData pres : mPresentationList) {
            pres.deselect();
        }
        refresh();
    }

    public int getSelectedCount() {
        return mAdapter.getSelectedCount();
    }

    @Override
    public void onPresentationSelected(String presentationId) {
        mListener.onSelectPresentation(presentationId);
    }

    @Override
    public void onPresentationDeselected(String presentationId) {
        mListener.onDeselectPresentation(presentationId);
    }

    @Override
    public boolean onPresentationOpened(String presentationId) {
        mListener.onOpenPresentation(presentationId);
        return true;
    }

    @Override
    public void onPresentationPracticeSelected(String presentationId) {
        mListener.onPracticePresentation(presentationId);
    }

    @Override
    public void onPresentationDeleteSelected(String presentationId) {
        mListener.onDeletePresentation(presentationId);
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
        public void onSelectPresentation(String presentationId);
        public void onDeselectPresentation(String presentationId);
        public void onPracticePresentation(String presentationId);
        public void onDeletePresentation(String presentationId);
    }
}
