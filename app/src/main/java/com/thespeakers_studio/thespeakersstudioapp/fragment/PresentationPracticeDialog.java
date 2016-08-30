package com.thespeakers_studio.thespeakersstudioapp.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.runnable.UpdateTimerThread;
import com.thespeakers_studio.thespeakersstudioapp.settings.SettingsUtils;
import com.thespeakers_studio.thespeakersstudioapp.utils.OutlineHelper;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;

import com.thespeakers_studio.thespeakersstudioapp.model.Outline;
import com.thespeakers_studio.thespeakersstudioapp.model.OutlineItem;

import java.util.ArrayList;

import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.LOGD;

/**
 * Created by smcgi_000 on 7/20/2016.
 */
public class PresentationPracticeDialog extends DialogFragment implements
        View.OnClickListener, UpdateTimerThread.UpdateTimerInterface {


    public interface PresentationPracticeDialogInterface {
        public void onDialogDismissed(Outline outline, boolean finished);
    }

    public static final String TAG = "timer";

    private PresentationPracticeDialogInterface mInterface;
    private Outline mOutline;
    private OutlineHelper mOutlineHelper;
    private Dialog mDialog;

    private int mDuration = 0;

    private boolean mIsPractice;

    private boolean mDelay;
    private boolean mDisplayTimer;
    private boolean mShowWarning;

    private TextView mOutputMainView;
    private TextView mTimerView;
    private TextView mTimerTotalView;
    private TextView mOutputSubView;
    private TextView mWarningView;
    private ImageButton mButtonLeft;
    private ImageButton mButtonRight;
    private ImageButton mButtonDone;
    private LinearLayout mBulletListWrapper;
    private TextView mBulletListHeader;
    private LinearLayout mBulletList;

    //private Handler mTimeHandler = new Handler();
    private UpdateTimerThread mTimeThread;

    //private int mOutlineItemIndex;
    private String mTopicText = "";

    // TODO: make animation times and durations constants
    private final int INTERSTITIAL_DURATION = 1000;
    private final int ANIMATION_DURATION = 300;

    private final int WARNING_TIME = 300000;
    private final int WARNING_DURATION = 5000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mDelay = SettingsUtils.getTimerWait(getContext());
        mDisplayTimer = SettingsUtils.getTimerShow(getContext());
        mShowWarning = SettingsUtils.getTimerWarning(getContext());

        mOutlineHelper = new OutlineHelper();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mInterface != null) {
            mInterface.onDialogDismissed(mOutline, mTimeThread.isFinished());
        }

        mTimeThread.stop();
    }

    public void setInterface(PresentationPracticeDialogInterface inter) {
        mInterface = inter;
    }

    public void setup (Outline outline, int duration) {
        if (outline != null) {
            mOutline = outline;
            mIsPractice = true;
        } else {
            mIsPractice = false;
            mDuration = duration * 60 * 1000; // we'll be getting the duration in minutes
        }
    }
    public void setup (int duration) {
        setup(null, duration);
    }
    public void setup (Outline outline) {
        setup(outline, 0);
    }

    public UpdateTimerThread getTimerThread() {
        return mTimeThread;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mDialog = new Dialog(getActivity(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        mDialog.setContentView(R.layout.dialog_practice);
        mDialog.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        mTimerTotalView = (TextView) mDialog.findViewById(R.id.practice_timer_total);
        mOutputSubView = (TextView) mDialog.findViewById(R.id.practice_sub);
        mButtonLeft = (ImageButton) mDialog.findViewById(R.id.button_left);
        mButtonRight = (ImageButton) mDialog.findViewById(R.id.button_right);
        mButtonDone = (ImageButton) mDialog.findViewById(R.id.button_done);
        mOutputMainView = (TextView) mDialog.findViewById(R.id.practice_main);
        mTimerView = (TextView) mDialog.findViewById(R.id.practice_timer_current);
        mWarningView = (TextView) mDialog.findViewById(R.id.practice_interval_warning);

        mBulletListWrapper = (LinearLayout) mDialog.findViewById(R.id.practice_main_bullet_list);
        mBulletListHeader = (TextView) mDialog.findViewById(R.id.practice_main_bullet_header);
        mBulletList = (LinearLayout) mDialog.findViewById(R.id.practice_main_bullet_list);

        mTimerView.setOnClickListener(this);
        mDialog.findViewById(R.id.button_next).setOnClickListener(this);

        hideAllTheThings();

        Bundle args = getArguments();
        mTimeThread = new UpdateTimerThread(getContext(), args);

        return mDialog;
    }

    private void hideAllTheThings() {
        mTimerTotalView.setText("");
        mTimerView.setText("");
        mOutputMainView.setText("");
        mOutputSubView.setText("");
        mBulletListHeader.setText("");
        mWarningView.setText("");

        mButtonDone.setVisibility(View.GONE);
        mButtonLeft.setVisibility(View.GONE);
        mButtonRight.setVisibility(View.GONE);

        mBulletList.removeAllViews();
    }

    @Override
    public void onStart() {
        super.onStart();

        startTimer();
    }

    private void startTimer() {
        if (mOutline != null || mDuration > 0) {
            mTimeThread.setup(this, mOutline, mDuration);
            mTimeThread.start(mDelay);
            // mTimeThread might be paused off the bat if the activity was paused while it was paused
            // perhaps the user paused the timer and rotated the screen or checked another app
            // TODO: or got a phone call?
            if (mTimeThread.isPaused()) {
                pause();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mTimeThread.stop();
    }

    private interface showViewInterface {
        public void onReadyToShow(View v);
    }

    public boolean showView (final View view, final boolean show, final String text,
                             final showViewInterface callback) {
        boolean needToHide;
        if (view instanceof TextView) {
            String existing = ((TextView) view).getText().toString();
            if (existing.equals(text)) {
                return false;
            }
            needToHide = !existing.isEmpty();
        } else {
            needToHide = view.getVisibility() == View.VISIBLE;
        }

        Animation outfade = new AlphaAnimation(1f, 0f);
        outfade.setDuration(needToHide ? ANIMATION_DURATION : 0);
        Animation outslide = new TranslateAnimation(0f, -50f, 0f, 0f);
        outslide.setDuration(needToHide ? ANIMATION_DURATION : 0);
        AnimationSet out = new AnimationSet(true);
        out.addAnimation(outfade);
        out.addAnimation(outslide);

        view.setVisibility(View.VISIBLE);
        view.clearAnimation();

        out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (view instanceof TextView) {
                    ((TextView) view).setText(text);
                }

                if (callback != null) {
                    callback.onReadyToShow(view);
                }

                if (show) {
                    Animation infade = new AlphaAnimation(0f, 1f);
                    infade.setDuration(ANIMATION_DURATION);
                    Animation inslide = new TranslateAnimation(50f, 0f, 0, 0);
                    inslide.setDuration((ANIMATION_DURATION));
                    final AnimationSet in = new AnimationSet(true);
                    in.addAnimation(infade);
                    in.addAnimation(inslide);

                    view.startAnimation(in);
                } else {
                    view.setVisibility(View.GONE);
                    if (view instanceof LinearLayout) {
                        ((LinearLayout) view).removeAllViews();
                    }
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(out);

        return true;
    }
    public boolean showText(TextView view, String text) {
        return showView(view, !text.isEmpty(), text, null);
    }
    public boolean showText(TextView view, int id) {
        return showText(view, getResources().getString(id));
    }
    public void showView(View view) {
        showView(view, true, "", null);
    }
    public void hideView(View view) {
        showView(view, false, "", null);
    }

    public void prepareDelay() {
        mTimerTotalView.setVisibility(View.GONE);
        mOutputSubView.setVisibility(View.GONE);

        showText(mOutputMainView, R.string.get_ready);
        //mTimerView.setText("");
    }

    @Override
    public void finish() {
        showText(mOutputSubView, "");
        showText(mOutputMainView, R.string.done);

        hideButton(mButtonLeft);
        hideButton(mButtonRight);
        showButton(mButtonDone);

        blink(mTimerTotalView);
    }

    @Override
    public void updateTime(long currentRemaining, long totalRemaining, long elapsed) {
        if (mTimeThread.isStarted() && mDisplayTimer && mIsPractice) {
            // show the time for the whole presentation in smaller output
            setTimer(mTimerTotalView, totalRemaining);
        }
        if (!mTimeThread.isFinished()) {
            if (mDisplayTimer) { // && mTopicText.isEmpty()) {
                if (mTimeThread.isStarted()) {
                    // show the current remaining time in 0:00 format
                    setTimer(mTimerView, currentRemaining);
                } else {
                    prepareDelay();
                    // show the 5, 4, 3, 2, 1 countdown
                    showText(mTimerView, Utils.secondsFromMillis(currentRemaining));
                }
            } else {
                // if we don't want to show the timer, don't show the timer
                // if mTopicText is not empty, we are showing the topic name for a second
                mTimerView.setText("");
            }
        } else {
            // we are totally done, so we can begin counting up!
            mTimerView.setTextColor(ContextCompat.getColor(getActivity(), R.color.success));
            setTimer(mTimerView, -elapsed);
        }

        if (mTimeThread.isStarted() && mShowWarning && !mTimeThread.isFinished()) {
            if (totalRemaining <= WARNING_TIME && totalRemaining >= WARNING_TIME - 100) {
                // five minute warning
                if (showText(mWarningView, R.string.five_minutes_left)) {
                    mTimeThread.vibrate(new long[] {Utils.VIBRATE_PULSE,
                            Utils.VIBRATE_PULSE_GAP, Utils.VIBRATE_PULSE});
                }
            } else if (totalRemaining <= (WARNING_TIME - WARNING_DURATION)
                    && totalRemaining >= (WARNING_TIME - WARNING_DURATION - 100)) {
                // hide it after five seconds
                showText(mWarningView, "");
            }
        }
    }

    @Override
    public void outlineItem(final OutlineItem item, long remainingTime) {
        if (item.getParentId().equals(OutlineItem.NO_PARENT)) {
            if (remainingTime + item.getDuration() > INTERSTITIAL_DURATION) {
                // this is a top level item, so we will show it for a second and then move on
                // to the first sub-item
                showText(mOutputMainView, "");
                hideView(mBulletList);
                showText(mBulletListHeader, "");
                showText(mOutputSubView, "");
                showText(mOutputMainView, item.getText());

                hideButton(mButtonLeft);
                hideButton(mButtonRight);
            }

            // save the text so that the next item can use it
            mTopicText = item.getText();
        } else {
            //this is a sub-item

            if (mTopicText.isEmpty()) {
                showOutlineItem(item);
            } else {
                // delay showing the next item for a second
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showText(mOutputSubView, mTopicText);
                        mTopicText = "";
                        showOutlineItem(item);
                    }
                }, INTERSTITIAL_DURATION);
            }
        }
    }

    private void showOutlineItem(OutlineItem item) {
        // see if this item has children, which we will show as a bulleted list
        final ArrayList<OutlineItem> children = mOutline.getItemsByParentId(item.getId());

        // if we have children, show the item as a bulleted list, otherwise just show the thing
        if (children.size() > 0) {

            showText(mOutputMainView, "");
            mBulletListWrapper.setVisibility(View.VISIBLE);

            showText(mBulletListHeader, item.getText());

            showView(mBulletList, true, "", new showViewInterface() {
                @Override
                public void onReadyToShow(View v) {
                    mBulletList.removeAllViews();
                    int cnt = 1;
                    for (OutlineItem child : children) {
                        TextView tv = new TextView(getContext());
                        ViewGroup.MarginLayoutParams mlp = new ViewGroup.MarginLayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        int m = getContext().getResources().getDimensionPixelSize(R.dimen.spacing_8);
                        mlp.setMargins(m, m, m, m);
                        tv.setLayoutParams(mlp);
                        tv.setTextSize(getContext().getResources()
                                .getDimension(R.dimen.practice_bullet_list_font_size));
                        mBulletList.addView(tv);

                        tv.setText(mOutlineHelper.getBullet(3, cnt)
                                + " " + child.getText());
                        cnt++;
                    }
                }
            });

        } else {

            // show the text for this item
            showText(mOutputMainView, item.getText());
            // hide the bulleted list
            showText(mBulletListHeader, "");
            hideView(mBulletList);

        }

        //showButton(mButtonLeft);
        showButton(mButtonRight);
    }

    private void animateButton(final View button, boolean in) {
        if (button.getVisibility() == View.VISIBLE && in) {
            return;
        } else if (button.getVisibility() == View.GONE && !in) {
            return;
        }
        button.setVisibility(View.VISIBLE);
        //button.setAlpha(0f);

        Animation infade = new AlphaAnimation(in ? 0f : 1f, in ? 1f : 0f);
        infade.setDuration(ANIMATION_DURATION);
        Animation inslide = new TranslateAnimation(in ? 50f : 0f, in ? 0f : 50f, 0, 0);
        inslide.setDuration(ANIMATION_DURATION);

        AnimationSet set = new AnimationSet(true);
        set.addAnimation(infade);
        set.addAnimation(inslide);

        if (!in) {
            set.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    button.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
        }

        button.startAnimation(set);
    }
    private void showButton(View button) {
        animateButton(button, true);
    }
    private void hideButton(View button) {
        animateButton(button, false);
    }

    private void blink(View v) {
        Animation blink = new AlphaAnimation(0f, 1f);
        blink.setDuration(50);
        blink.setStartOffset(500);
        blink.setRepeatMode(Animation.REVERSE);
        blink.setRepeatCount(Animation.INFINITE);
        v.setAnimation(blink);
    }

    private void pause() {
        showText(mOutputMainView, R.string.paused);
        blink(mTimerView);
        hideView(mButtonRight);
        //hideView(mButtonLeft);
    }
    private void resume() {
        mTimerView.clearAnimation();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.practice_timer_current:
                // tap the time to pause or resume the timer
                if (mTimeThread.togglePause()) {
                    pause();
                } else {
                    resume();
                }
                break;
            case R.id.button_next:
                if (mTimeThread.isPaused()) {
                    return;
                }
                // skip to next, but only if the presentation is running
                if (mTimeThread.getStartTime() > 0) {
                    mTimeThread.vibrate(Utils.VIBRATE_BUMP);
                    if (!mTimeThread.isFinished()) {
                        mTimeThread.skipAhead();
                    } else {
                        // we are done, so close the thing
                        dismiss();
                    }
                }
                break;
        }
    }

    private void setTimer(TextView timer, long time) {
        timer.setVisibility(View.VISIBLE);
        timer.setText(Utils.getTimeStringFromMillis(time, getResources()));
    }

}
