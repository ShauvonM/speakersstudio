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
public class PresentationPracticeDialog extends DialogFragment implements View.OnClickListener {


    public interface PresentationPracticeDialogInterface {
        public void onDialogDismissed(Outline outline, boolean finished);
    }

    public static final String TAG = "timer";

    private PresentationPracticeDialogInterface mInterface;
    private Outline mOutline;
    private OutlineHelper mOutlineHelper;
    private Dialog mDialog;

    private int mDuration;

    private boolean mIsPractice;

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
    private ImageButton mButtonDone;
    private LinearLayout mBulletListWrapper;
    private TextView mBulletListHeader;
    private LinearLayout mBulletList;

    private Handler mTimeHandler = new Handler();

    private long mOutlineDuration; // the duration of the whole presentation
    private long mCurrentExpiration; // the timer time, in milliseconds
    private long mStartTime = 0L;
    private long mElapsed = 0l;
    private long mCurrentItemDuration = 0l; // for tracking purposes, so that we can track how long each thing takes

    private int mOutlineItemIndex;
    private String mTopicText = "";

    private boolean mStarted;
    private boolean mPaused;
    private boolean mFinished;

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

        mDelay = SettingsUtils.getTimerWait(getContext());
        mDisplayTimer = SettingsUtils.getTimerShow(getContext());
        mShowWarning = SettingsUtils.getTimerWarning(getContext());
        mTrack = SettingsUtils.getTimerTrack(getContext());
        mVibrate = SettingsUtils.getTimerVibrate(getContext());

        mOutlineHelper = new OutlineHelper();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mInterface != null) {
            mInterface.onDialogDismissed(mOutline, mFinished);
        }

        mTimeHandler.removeCallbacks(updateTimerThread);
    }

    public void setInterface(PresentationPracticeDialogInterface inter) {
        mInterface = inter;
    }

    public void setup (Outline outline) {
        if (outline != null) {
            mOutline = outline;
            mIsPractice = true;
        } else {
            mIsPractice = false;
        }
    }
    public void setup (int duration) {
        mIsPractice = false;
        mDuration = duration * 60 * 1000; // we'll be getting the duration in minutes
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

        if (mDelay) {
            startDelay();
        } else {
            start();
        }
        mTimeHandler.postDelayed(updateTimerThread, ANIMATION_DURATION);
    }

    @Override
    public void onStop() {
        super.onStop();
        mTimeHandler.removeCallbacks(updateTimerThread);
    }

    public void startDelay() {
        mTimerTotalView.setVisibility(View.GONE);
        mOutputSubView.setVisibility(View.GONE);

        //mOutputMainView.setText(R.string.get_ready);
        showText(mOutputMainView, R.string.get_ready);
        mTimerView.setText("");

        mCurrentExpiration = DELAY_TIME;

        mStarted = false;
    }

    private void start() {
        mStarted = true;
        mStartTime = 0;
        mElapsed = 0;
        mOutlineItemIndex = -1;
        mOutlineDuration = mIsPractice ? mOutline.getDurationMillis() : mDuration;
        mCurrentExpiration = 0;
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


    public void nextItem() {
        if (mIsPractice) {
            if (!mStarted) {
                start();
            }
            mOutlineItemIndex++;
            if (mOutlineItemIndex < mOutline.getItemCount()) {
                OutlineItem item = mOutline.getItem(mOutlineItemIndex);
                // skip items with 0 duration
                if (item.getDuration() == 0) {
                    nextItem();
                    return;
                }

                if (item.getParentId().equals(OutlineItem.NO_PARENT)) {
                    // this is a top level item, so we will show it for a second and then move on
                    // to the first sub-item
                    showText(mOutputMainView, "");
                    hideView(mBulletList);
                    showText(mBulletListHeader, "");
                    showText(mOutputSubView, "");
                    showText(mOutputMainView, item.getText());

                    // save the text so that the next item can use it
                    mTopicText = item.getText();

                    // we'll only show top level items for a second
                    mCurrentExpiration = mElapsed + INTERSTITIAL_DURATION;

                    vibrate(PULSE);
                    hideButton(mButtonLeft);
                    hideButton(mButtonRight);
                } else {
                    //this is a sub-item

                    long thisDuration = item.getDuration();
                    // collect any time left over from the last item
                    // (which will be > 0 if the user skipped to the next item)
                    long remainingTime = mCurrentExpiration - mElapsed;

                    // set the timer value - this is what the timer is counting down to
                    mCurrentExpiration = mElapsed + (remainingTime + thisDuration);

                    // store this item's duration, so we can know how long it takes
                    //  (if the user skips it early)
                    mCurrentItemDuration = remainingTime + thisDuration;

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

                        showText(mOutputMainView, item.getText());
                        showText(mBulletListHeader, "");
                        hideView(mBulletList);

                    }

                    if (!mTopicText.isEmpty()) {
                        // mTopicText will hold the topic name, so that we can put it
                        // in the topic text view
                        showText(mOutputSubView, mTopicText);
                        mTopicText = "";
                        // remove time from the current item for the time we wasted looking
                        // at the topic name
                        mCurrentExpiration -= INTERSTITIAL_DURATION;
                    }

                    //showButton(mButtonLeft);
                    showButton(mButtonRight);
                }
            } else {
                // we're done!
                finish();
            }
        } else {
            // this is a timer, so just show a timer
            showText(mOutputSubView, R.string.timer);
            showText(mOutputMainView, "");
            if (!mStarted) {
                start();
                mCurrentExpiration = mDuration;
            } else if (mPaused) {
                mPaused = false;
            } else {
                finish();
            }
        }

        //mTimeHandler.postDelayed(updateTimerThread, 0);
    }

    private void finish() {
        mFinished = true;

        vibrate(new long[]{PULSE, PULSE_GAP, PULSE, PULSE_GAP, PULSE});
        mOutlineDuration = 0;
        mCurrentExpiration = 0;
        mStartTime = 0;

        showText(mOutputSubView, "");
        showText(mOutputMainView, R.string.done);

        hideButton(mButtonLeft);
        hideButton(mButtonRight);
        showButton(mButtonDone);

        blink(mTimerTotalView);
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
                    mOutlineDuration = mOutlineDuration - mElapsed;
                    if (mStarted) {
                        // if the presentation has started, save the current position there, too
                        mCurrentExpiration = mCurrentExpiration - mElapsed;
                    }

                    blink(mTimerView);
                } else {
                    mStartTime = 0;
                    mTimerView.clearAnimation();
                    mOutlineItemIndex--;
                    nextItem();

                    mTimeHandler.postDelayed(updateTimerThread, 0);
                }
                break;
            case R.id.button_next:
                // skip to next, but only if the presentation is running
                if (mStartTime > 0) {
                    vibrate(BUMP);
                    if (!mFinished) {
                        if (mTrack) {
                            // store the duration this thing actually took
                            OutlineItem currentItem = mOutline.getItem(mOutlineItemIndex);
                            long currentRemaining = mCurrentExpiration - mElapsed;
                            currentItem.setTimedDuration(mCurrentItemDuration - currentRemaining);
                        }
                        nextItem();
                    } else {
                        // we are done, so close the thing
                        dismiss();
                    }
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

            mElapsed = Utils.roundToThousand(now() - mStartTime);
            long totalRemaining = mOutlineDuration - mElapsed;
            long currentRemaining = mCurrentExpiration - mElapsed;

            //LOGD(TAG, "elapsed time: " + mElapsed + " " + totalRemaining + " " + currentRemaining);

            if (currentRemaining <= 0 && !mFinished) {
                nextItem();
            } else {
                if (!mFinished) {
                    if (mStarted && mDisplayTimer && mIsPractice) {
                        setTimer(mTimerTotalView, totalRemaining);
                    }
                    if (mDisplayTimer && mTopicText.isEmpty()) {
                        if (mStarted) {
                            // show the current remaining time in 0:00 format
                            setTimer(mTimerView, currentRemaining);
                        } else {
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
                    setTimer(mTimerView, -mElapsed);
                }
            }

            mTimeHandler.postDelayed(this, 100);

            if (mStarted && mShowWarning && !mFinished) {
                if (totalRemaining <= WARNING_TIME && totalRemaining >= WARNING_TIME - 100) {
                    // five minute warning
                    if (showText(mWarningView, R.string.five_minutes_left)) {
                        vibrate(new long[] {PULSE, PULSE_GAP, PULSE});
                    }
                } else if (totalRemaining <= (WARNING_TIME - WARNING_DURATION)
                        && totalRemaining >= (WARNING_TIME - WARNING_DURATION - 100)) {
                    // hide it after five seconds
                    showText(mWarningView, "");
                }
            }
        }
    };

}
