package com.thespeakers_studio.thespeakersstudioapp.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.thespeakers_studio.thespeakersstudioapp.model.Outline;
import com.thespeakers_studio.thespeakersstudioapp.R;

/**
 * Created by smcgi_000 on 7/20/2016.
 */
public class PresentationPracticeFragment extends Fragment implements View.OnClickListener {

    public interface PracticeInterface {
        public void onStartPractice(Outline outline, boolean delay, boolean displayTimer, boolean showWarning, boolean track, boolean vibrate, boolean recordVideo, boolean disablePhone);
    }

    public static final String TAG = "presentation_practice";

    private View mView;
    private Outline mOutline;
    private PracticeInterface mInterface;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mInterface = (PracticeInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement PracticeInterface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_practice, container, false);

        ((TextView) mView.findViewById(R.id.presentation_name)).setText(mOutline.getTitle());
        ((TextView) mView.findViewById(R.id.presentation_duration)).setText(mOutline.getDuration());

        mView.findViewById(R.id.button_start_practice).setOnClickListener(this);

        mView.findViewById(R.id.checkbox_record).setOnClickListener(this);
        mView.findViewById(R.id.checkbox_track).setOnClickListener(this);
        mView.findViewById(R.id.checkbox_disable_phone).setOnClickListener(this);

        return mView;
    }

    public void setOutline(Outline outline) {
        mOutline = outline;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_start_practice:
                boolean delay = ((CheckBox) mView.findViewById(R.id.checkbox_delay)).isChecked();
                boolean displayTimer = ((CheckBox) mView.findViewById(R.id.checkbox_show_timer)).isChecked();
                boolean showWarning = ((CheckBox) mView.findViewById(R.id.checkbox_show_warning)).isChecked();
                boolean track = ((CheckBox) mView.findViewById(R.id.checkbox_track)).isChecked();
                boolean vibrate = ((CheckBox) mView.findViewById(R.id.checkbox_vibrate)).isChecked();
                boolean recordVideo = ((CheckBox) mView.findViewById(R.id.checkbox_record)).isChecked();
                boolean disablePhone = ((CheckBox) mView.findViewById(R.id.checkbox_disable_phone)).isChecked();

                startPractice(delay, displayTimer, showWarning, track, vibrate, recordVideo, disablePhone);
            break;
            case R.id.checkbox_record:
                Toast.makeText(getContext(), "Recording isn't implemented yet", Toast.LENGTH_SHORT).show();
                ((CheckBox) mView.findViewById(R.id.checkbox_record)).setChecked(false);
                break;
            case R.id.checkbox_track:
                Toast.makeText(getContext(), "Tracking isn't implemented yet", Toast.LENGTH_SHORT).show();
                ((CheckBox) mView.findViewById(R.id.checkbox_track)).setChecked(false);
                break;
            case R.id.checkbox_disable_phone:
                Toast.makeText(getContext(), "Disabling phone isn't implemented yet", Toast.LENGTH_SHORT).show();
                ((CheckBox) mView.findViewById(R.id.checkbox_disable_phone)).setChecked(false);
                break;
        }
    }

    private void startPractice(boolean delay, boolean displayTimer, boolean showWarning, boolean track, boolean vibrate, boolean recordVideo, boolean disablePhone) {
        //Dialog dialog = new Dialog(getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        //dialog.show();
        mInterface.onStartPractice(mOutline, delay, displayTimer, showWarning, track, vibrate, recordVideo, disablePhone);
    }
}
