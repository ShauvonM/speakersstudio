package com.thespeakers_studio.thespeakersstudioapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by smcgi_000 on 7/7/2016.
 */
public class PresentationOutlineFragment extends Fragment {

    private View mView;
    private Outline mOutline;
    private OutlineHelper mHelper;

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

    public void setOutline (Outline outline) {
        //mPresentation = pres;
        //mOutline = Outline.fromPresentation(getContext(), pres);
        mOutline = outline;
    }

    public void render () {
        mHelper = new OutlineHelper();

        ((TextView) mView.findViewById(R.id.outline_title)).setText(mOutline.getTitle());
        ((TextView) mView.findViewById(R.id.outline_duration)).setText(mOutline.getDuration());
        ((TextView) mView.findViewById(R.id.outline_date)).setText(mOutline.getDate());

        LinearLayout listWrapper = (LinearLayout) mView.findViewById(R.id.outline_list);
        renderList(mOutline.getItems(), listWrapper, 1);
    }

    private void renderList (ArrayList<OutlineItem> items, LinearLayout wrapper, int level) {
        if (items.size() == 0) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        int index = 0;

        for (OutlineItem item : items) {
            RelativeLayout itemLayout = (RelativeLayout) inflater.inflate(R.layout.outline_item, wrapper, false);

            // set the icon
            ((TextView) itemLayout.findViewById(R.id.list_main_bullet)).setText(mHelper.getBullet(level, index + 1));
            // set the text for this thing
            ((TextView) itemLayout.findViewById(R.id.list_topic)).setText(item.getText());
            // set the duration for this thing
            ((TextView) itemLayout.findViewById(R.id.list_duration)).setText(
                    item.getDuration() > 0 ?
                    String.format(
                        getResources().getString(R.string.outline_duration), item.getDuration())
                            : ""
            );

            renderList(item.getSubItems(), (LinearLayout) itemLayout.findViewById(R.id.outline_sub_item_wrapper), level == 3 ? 1 : level + 1);

            wrapper.addView(itemLayout);

            index++;
        }
    }
}
