package com.thespeakers_studio.thespeakersstudioapp.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.thespeakers_studio.thespeakersstudioapp.model.Outline;
import com.thespeakers_studio.thespeakersstudioapp.utils.OutlineHelper;
import com.thespeakers_studio.thespeakersstudioapp.model.OutlineItem;
import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;

import java.util.ArrayList;

/**
 * Created by smcgi_000 on 7/7/2016.
 */
public class PresentationOutlineFragment extends Fragment implements View.OnClickListener {

    public interface OutlineInterface {
        public void onPracticeClicked(Outline outline);
    }

    public static final String TAG = "presentation_outline";

    private View mView;
    private Outline mOutline;
    private OutlineHelper mHelper;
    private OutlineInterface mInterface;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mInterface = (OutlineInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_outline, container, false);

        if (mOutline != null) {
            render();
        }

        return mView;
    }

    public void setOutline (Outline outline) {
        mOutline = outline;
    }

    public void render () {
        mHelper = new OutlineHelper();

        ((TextView) mView.findViewById(R.id.outline_title)).setText(mOutline.getTitle());
        ((TextView) mView.findViewById(R.id.outline_duration)).setText(mOutline.getDuration());
        ((TextView) mView.findViewById(R.id.outline_date)).setText(mOutline.getDate());

        mView.findViewById(R.id.fab).setOnClickListener(this);

        LinearLayout listWrapper = (LinearLayout) mView.findViewById(R.id.outline_list);
        listWrapper.removeAllViews();

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
            long duration = item.getDuration();
            TextView timeView = (TextView) itemLayout.findViewById(R.id.list_duration);
            if (duration > 0) {
                timeView.setText(Utils.getTimeStringFromMillis(duration, getResources()));
                //timeView.setText("" + duration);
            } else {
                timeView.setText("");
            }

            renderList(item.getSubItems(), (LinearLayout) itemLayout.findViewById(R.id.outline_sub_item_wrapper), level == 3 ? 1 : level + 1);

            wrapper.addView(itemLayout);

            index++;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab) {
            mInterface.onPracticeClicked(mOutline);
        }
    }
}
