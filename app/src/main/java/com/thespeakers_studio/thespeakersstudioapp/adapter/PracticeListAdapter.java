package com.thespeakers_studio.thespeakersstudioapp.adapter;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.data.OutlineDbHelper;
import com.thespeakers_studio.thespeakersstudioapp.model.OutlineItem;
import com.thespeakers_studio.thespeakersstudioapp.model.Practice;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;

import java.util.ArrayList;

import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.LOGD;

/**
 * Created by smcgi_000 on 8/16/2016.
 */
public class PracticeListAdapter extends RecyclerView.Adapter<PracticeListAdapter.PracticeListViewHolder> {

    private ArrayList<Practice> mPractices;

    public PracticeListAdapter(ArrayList<Practice> practices) {
        mPractices = practices;
    }

    private int mTopMargin;

    private final int VIEW_TYPE_HEADER = 0;
    private final int VIEW_TYPE_LOGO = 2;
    private final int VIEW_TYPE_NORMAL = 1;

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_HEADER;
        } else if (position == mPractices.size() + 1) {
            return VIEW_TYPE_LOGO;
        } else {
            return VIEW_TYPE_NORMAL;
        }
    }

    @Override
    public PracticeListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int id;
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                id = R.layout.listview_header;
                break;
            case VIEW_TYPE_LOGO:
                id = R.layout.ss_logo_imageview;
                break;
            default:
                id = R.layout.practice_list_item;
                break;
        }

        View v = LayoutInflater.from(parent.getContext())
                    .inflate(id, parent, false);

        PracticeListViewHolder vh = new PracticeListViewHolder(v);
        return vh;
    }

    public void setTopMargin(int top) {
        mTopMargin = top;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(PracticeListViewHolder holder, int position) {
        if (position == 0) {
            holder.setMargin(mTopMargin);
        } else if (position - 1 < mPractices.size()) {
            holder.setPractice(mPractices.get(position - 1));
        }
    }

    @Override
    public int getItemCount() {
        return mPractices.size() + 2;
    }

    public class PracticeListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Practice mPractice;

        public PracticeListViewHolder(View itemView) {
            super(itemView);
            if (itemView.findViewById(R.id.listview_header) != null) {
                ((TextView) itemView.findViewById(R.id.listview_header)).setText(
                        itemView.getContext().getString(R.string.saved_practices_header)
                );
            }
        }

        public void setMargin(int topMargin) {
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams)
                itemView.getLayoutParams();
            if (mlp.topMargin != topMargin) {
                mlp.topMargin = topMargin;
                itemView.setLayoutParams(mlp);
            }
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

            if (mPractice.getOutlineItems().size() > 0) {
                itemView.findViewById(R.id.practice_contains_outline_items).setVisibility(View.VISIBLE);
            } else {
                itemView.findViewById(R.id.practice_contains_outline_items).setVisibility(View.GONE);
            }

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            AppCompatActivity context = (AppCompatActivity) v.getContext();
            OutlineDbHelper dbHelper = new OutlineDbHelper(context);

            // show user's thoughts on this practice
            AlertDialog.Builder thoughtsDialogBuilder = new AlertDialog.Builder(context);

            View practiceResponseDialogContents =
                    context.getLayoutInflater().inflate(R.layout.dialog_practice_response_view, null);

            thoughtsDialogBuilder.setView(practiceResponseDialogContents);

            TextView timestamp = (TextView) practiceResponseDialogContents
                    .findViewById(R.id.practice_timestamp);
            TextView messageView = (TextView) practiceResponseDialogContents
                    .findViewById(R.id.practice_response_notes);
            AppCompatRatingBar ratingBar = (AppCompatRatingBar) practiceResponseDialogContents
                    .findViewById(R.id.practice_rating);
            TextView outlineItemsCount = (TextView) practiceResponseDialogContents
                    .findViewById(R.id.outline_items_count);
            View asterisk = practiceResponseDialogContents.findViewById(R.id.practice_contains_outline_items);

            messageView.setText(mPractice.getMessage());

            timestamp.setText(String.format(
                            itemView.getContext().getString(R.string.practice_timestamp),
                            Utils.formatDateTime(itemView.getContext(), mPractice.getModifiedDate())
                    ));

            ratingBar.setRating(mPractice.getRating());

            if (mPractice.getOutlineItems().size() > 0) {
                outlineItemsCount.setText(String.format(
                        itemView.getContext().getString(R.string.practice_response_outline_items),
                        mPractice.getOutlineItems().size()
                ));
            } else {
                outlineItemsCount.setVisibility(View.GONE);
                asterisk.setVisibility(View.GONE);
            }

            thoughtsDialogBuilder
                    .setCancelable(true)
                    .setIcon(R.drawable.ic_record_voice_over_white_24dp)
                    .setTitle(R.string.practice_response_view_title);

            AlertDialog dialog = thoughtsDialogBuilder.create();
            dialog.show();
        }

    }
}
