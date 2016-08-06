package com.thespeakers_studio.thespeakersstudioapp.adapter;

import android.app.ActionBar;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Layout;
import android.view.View;

import com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils;

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

        if (view.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams p =
                    (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();

            int spanIndex = p.getSpanIndex();

            outRect.top = 0;
            outRect.left = 0;
            outRect.bottom = space;
            outRect.right = spanIndex == 0 ? space : 0;
        } else {
            outRect.bottom = space;
        }
    }
}
