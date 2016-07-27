package com.thespeakers_studio.thespeakersstudioapp.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;

import com.thespeakers_studio.thespeakersstudioapp.model.Outline;
import com.thespeakers_studio.thespeakersstudioapp.model.OutlineItem;

/**
 * Created by smcgi_000 on 7/20/2016.
 */
public class PresentationPracticeDialog extends DialogFragment implements View.OnClickListener {


    public interface PresentationPracticeDialogInterface {
        public void onDialogDismissed();
    }

    public static final String TAG = "timer";

    private PresentationPracticeDialogInterface mInterface;
    private Outline mOutline;
    private Dialog mDialog;

    private boolean mDelay;
    private boolean mDisplayTimer;
    private boolean mShowWarning;
    private boolean mTrack;
    private boolean mVibrate;

    private TextView mOutputMainView;
    private TextView mTimerView;
    private TextView mTimerTotalView;
    private TextView mOutputSubView;
    private TextView mWarningView;
    private ImageButton mButtonLeft;
    private ImageButton mButtonRight;

    private Handler mTimeHandler = new Handler();

    private long mOutlineDuration; // the duration of the whole presentation
    private long mCurrentExpiration; // the timer time, in milliseconds
    private long mStartTime = 0L;
    private long mElapsed = 0l;
    //private long mCurrentStartTime = 0L;

    private int mTopicIndex;
    private int mSubTopicIndex;

    private boolean mStarted;
    private boolean mPaused;

    private final int PULSE = 500;
    private final int PULSE_GAP = 200;
    private final int BUMP = 200;

    // TODO: make animation times and durations constants
    private final int DELAY_TIME = 5000;
    private final int INTERSTITIAL_DURATION = 1000;
    private final int ANIMATION_DURATION = 300;

    private final int WARNING_TIME = 300000;
    private final int WARNING_DURATION = 5000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mInterface != null) {
            mInterface.onDialogDismissed();
        }

        mTimeHandler.removeCallbacks(updateTimerThread);
    }

    public void setInterface(PresentationPracticeDialogInterface inter) {
        mInterface = inter;
    }

    public void setup (Outline outline, boolean delay, boolean displayTimer, boolean showWarning, boolean track, boolean vibrate) {
        mOutline = outline;
        mDelay = delay;
        mDisplayTimer = displayTimer;
        mShowWarning = showWarning;
        mTrack = track;
        mVibrate = vibrate;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mDialog = new Dialog(getActivity(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        mDialog.setContentView(R.layout.dialog_practice);
        mDialog.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        mTimerTotalView = (TextView) mDialog.findViewById(R.id.practice_timer_total);
        mOutputSubView = (TextView) mDialog.findViewById(R.id.practice_sub);
        mButtonLeft = (ImageButton) mDialog.findViewById(R.id.button_left);
        mButtonRight = (ImageButton) mDialog.findViewById(R.id.button_right);
        mOutputMainView = (TextView) mDialog.findViewById(R.id.practice_main);
        mTimerView = (TextView) mDialog.findViewById(R.id.practice_timer_current);
        mWarningView = (TextView) mDialog.findViewById(R.id.practice_interval_warning);

        mTimerView.setOnClickListener(this);
        mDialog.findViewById(R.id.button_next).setOnClickListener(this);

        return mDialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mDelay) {
            startDelay();
        } else {
            nextItem();
        }
    }

    public boolean showText(final TextView view, final String text) {
        String existing = view.getText().toString();
        if (existing.equals(text)) {
            return false;
        }
        boolean empty = existing.isEmpty();

        Animation outfade = new AlphaAnimation(1f, 0f);
        outfade.setDuration(empty ? 0 : ANIMATION_DURATION);
        Animation outslide = new TranslateAnimation(0f, -50f, 0f, 0f);
        outslide.setDuration(empty ? 0 : ANIMATION_DURATION);
        AnimationSet out = new AnimationSet(true);
        out.addAnimation(outfade);
        out.addAnimation(outslide);

        Animation infade = new AlphaAnimation(0f, 1f);
        infade.setDuration(ANIMATION_DURATION);
        Animation inslide = new TranslateAnimation(50f, 0f, 0, 0);
        inslide.setDuration((ANIMATION_DURATION));
        final AnimationSet in = new AnimationSet(true);
        in.addAnimation(infade);
        in.addAnimation(inslide);

        view.setVisibility(View.VISIBLE);
        view.clearAnimation();

        out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setText(text);
                view.startAnimation(in);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(out);
        return true;
    }
    public boolean showText(TextView view, int id) {
        return showText(view, getResources().getString(id));
    }

    public void startDelay() {
        mTimerTotalView.setVisibility(View.GONE);
        mOutputSubView.setVisibility(View.GONE);

        //mOutputMainView.setText(R.string.get_ready);
        showText(mOutputMainView, R.string.get_ready);
        mTimerView.setText("");

        mCurrentExpiration = DELAY_TIME;

        mStarted = false;
        mTimeHandler.postDelayed(updateTimerThread, ANIMATION_DURATION);
    }

    public void nextItem() {
        if (!mStarted) {
            // we are starting the presentation, so we should set everything up
            mStartTime = 0;
            mElapsed = 0;
            mOutlineDuration = mOutline.getDurationMillis();
            mTopicIndex = -1;
            mStarted = true;
        }

        mSubTopicIndex++;
        if (mTopicIndex < 0 || mOutline.getItem(mTopicIndex) == null || mSubTopicIndex >= mOutline.getItem(mTopicIndex).getSubItemCount()) {
            // move to the next topic
            mTopicIndex++;
            mSubTopicIndex = -1;
            if (mTopicIndex >= mOutline.getItemCount()) {
                // we're done!
                vibrate(new long[] {PULSE, PULSE_GAP, PULSE, PULSE_GAP, PULSE});
                return;
            } else {
                showText(mOutputMainView, mOutline.getItem(mTopicIndex).getText());
                // when showing a new topic, we'll highlight the topic name for a second
                mCurrentExpiration = mElapsed + INTERSTITIAL_DURATION;
                vibrate(PULSE);
            }

            hideButton(mButtonRight);
        } else {
            // show the next sub item
            showText(mOutputSubView, mOutline.getItem(mTopicIndex).getText());
            OutlineItem subitem = mOutline.getItem(mTopicIndex).getSubItem(mSubTopicIndex);
            showText(mOutputMainView, subitem.getText());

            if (mPaused) {
                // mCurrentExpiration is already set, because we will continue from where we left off
                mPaused = false;
            } else {
                // add any remaining time from the last item, in case the user skipped ahead
                // round the duration of this item to the nearest 1000 to make sure it doesn't
                // throw off the timer
                long thisDuration = Utils.roundToThousand(subitem.getDuration());
                long remainingTime = mCurrentExpiration - mElapsed;
                mCurrentExpiration = mElapsed + (remainingTime + thisDuration);

                if (mSubTopicIndex == 0) {
                    // since we wasted time showing the topic name, we need to account for that here
                    mCurrentExpiration -= INTERSTITIAL_DURATION;
                }
            }

            showButton(mButtonRight);
        }

        if (mTopicIndex > 0 || mSubTopicIndex > 0) {
            //showButton(mButtonLeft);
        }

        mTimeHandler.postDelayed(updateTimerThread, 0);
    }

    private void animateButton(final View button, boolean in) {
        if (button.getVisibility() == View.VISIBLE && in) {
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
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    button.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
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

    private long now() {
        return SystemClock.uptimeMillis();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.practice_timer_current:
                // tap the time to pause or resume the timer
                if (!mPaused) {
                    mPaused = true;
                    showText(mOutputMainView, R.string.paused);
                    mTimeHandler.removeCallbacks(updateTimerThread);

                    // save the time remaining so we can pick up where we left off
                    mOutlineDuration = mOutlineDuration - mElapsed; //getRemaining(mStartTime, mOutlineDuration);
                    if (mStarted) {
                        // if the presentation has started, save the current position there, too
                        mCurrentExpiration = mCurrentExpiration - mElapsed; //getRemaining(mCurrentStartTime, mCurrentExpiration);
                    }

                    Animation blink = new AlphaAnimation(0f, 1f);
                    blink.setDuration(50);
                    blink.setStartOffset(500);
                    blink.setRepeatMode(Animation.REVERSE);
                    blink.setRepeatCount(Animation.INFINITE);
                    mTimerView.setAnimation(blink);
                } else {
                    mStartTime = 0;

                    mTimerView.clearAnimation();
                    mSubTopicIndex--;
                    nextItem();
                }
                break;
            case R.id.button_next:
                // skip to next, but only if the presentation is running
                if (mStartTime > 0) {
                    vibrate(BUMP);
                    nextItem();
                }
                break;
        }
    }

    private void vibrate(long[] pattern) {
        if (pattern.length == 0) {
            return;
        }
        if (mVibrate) {
            Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(pattern, -1);
        }
    }
    private void vibrate(int pattern) {
        vibrate(new long[] {pattern});
    }

    private void setTimer(TextView timer, long time) {
        timer.setVisibility(View.VISIBLE);
        timer.setText(Utils.getTimeStringFromMillis(time, getResources()));
    }

    private Runnable updateTimerThread = new Runnable() {
        @Override
        public void run() {
            if (mStartTime == 0) {
                mStartTime = now();
            }

            mElapsed = now() - mStartTime;
            long totalRemaining = mOutlineDuration - mElapsed;
            long currentRemaining = mCurrentExpiration - mElapsed;

            if (currentRemaining > 0) {
                if (mStarted && mDisplayTimer) {
                    setTimer(mTimerTotalView, totalRemaining);
                }
                if (mSubTopicIndex > -1 && mDisplayTimer) {
                    if (mStarted) {
                        setTimer(mTimerView, currentRemaining);
                    } else {
                        showText(mTimerView, Utils.secondsFromMillis(currentRemaining));
                    }
                } else {
                    mTimerView.setText("");
                }
                mTimeHandler.postDelayed(this, 0);

                //Log.d("SS", "total remaining: " + totalRemaining + " - current remaining: " + currentRemaining);

            } else {
                //Log.d("SS", "total remaining: " + totalRemaining + " - current remaining: " + currentRemaining + " SKIPPED");

                nextItem();
            }

            if (mStarted && mShowWarning) {
                if (totalRemaining <= WARNING_DURATION && totalRemaining >= WARNING_DURATION - 100) {
                    // five minute warning
                    if (showText(mWarningView, R.string.five_minutes_left)) {
                        vibrate(new long[] {PULSE, PULSE_GAP, PULSE});
                    }
                } else if (totalRemaining <= (WARNING_DURATION - WARNING_TIME)
                        && totalRemaining >= (WARNING_DURATION - WARNING_TIME - 100)) {
                    // hide it after five seconds
                    showText(mWarningView, "");
                }
            }
        }
    };

}
