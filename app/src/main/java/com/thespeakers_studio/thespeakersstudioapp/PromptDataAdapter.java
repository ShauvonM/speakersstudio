package com.thespeakers_studio.thespeakersstudioapp;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Created by smcgi_000 on 5/10/2016.
 */
public class PromptDataAdapter extends RecyclerView.Adapter<PromptViewHolder> {
    private ArrayList<Prompt> mData;

    public PromptDataAdapter(ArrayList<Prompt> data) {
        mData = data;
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position).getType();
    }

    @Override
    public PromptViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        PromptViewHolder vh;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v;

        switch (viewType) {
            case PresentationData.HEADER:
                v = inflater.inflate(R.layout.presentation_header_card, parent, false);
                break;
            case PresentationData.NEXT:
                v = inflater.inflate(R.layout.presentation_next_card, parent, false);
                break;
            case PresentationData.TEXT:
                v = inflater.inflate(R.layout.presentation_prompt_card_text, parent, false);
                break;
            default:
                v = inflater.inflate(R.layout.presentation_prompt_card, parent, false);
                break;
        }
        vh = new PromptViewHolder(v, parent);
        return vh;
    }

    @Override
    public void onBindViewHolder(PromptViewHolder holder, int position) {
        holder.setData(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
