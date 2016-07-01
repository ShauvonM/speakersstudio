package com.thespeakers_studio.thespeakersstudioapp;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

/**
 * Created by smcgi_000 on 7/1/2016.
 */
public class PresentationListSpanItemDecoration extends RecyclerView.ItemDecoration {

    private int space;

    public PresentationListSpanItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        int spanIndex = ((StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams()).getSpanIndex();

        outRect.top = 0;
        outRect.left = 0;
        outRect.bottom = space;
        outRect.right = spanIndex == 0 ? space : 0;
    }
}
