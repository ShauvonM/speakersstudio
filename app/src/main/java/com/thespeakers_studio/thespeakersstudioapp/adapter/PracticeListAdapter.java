package com.thespeakers_studio.thespeakersstudioapp.adapter;

import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.model.Practice;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;

import java.util.ArrayList;

/**
 * Created by smcgi_000 on 8/16/2016.
 */
public class PracticeListAdapter extends RecyclerView.Adapter<PracticeListAdapter.PracticeListViewHolder> {

    private ArrayList<Practice> mPractices;

    public PracticeListAdapter(ArrayList<Practice> practices) {
        mPractices = practices;
    }


    @Override
    public PracticeListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.practice_list_item, parent, false);
        PracticeListViewHolder vh = new PracticeListViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(PracticeListViewHolder holder, int position) {
        holder.setPractice(mPractices.get(position));
    }

    @Override
    public int getItemCount() {
        return mPractices.size();
    }

    public class PracticeListViewHolder extends RecyclerView.ViewHolder {

        private Practice mPractice;
        private View mView;

        public PracticeListViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setPractice(Practice p) {
            mPractice = p;
            ((AppCompatRatingBar) itemView.findViewById(R.id.practice_rating))
                    .setRating(p.getRating());
            ((TextView) itemView.findViewById(R.id.practice_timestamp))
                    .setText(String.format(
                            itemView.getContext().getString(R.string.practice_timestamp),
                            Utils.formatDateTime(itemView.getContext(), p.getModifiedDate())
                    ));
        }

    }
}
