package com.thespeakers_studio.thespeakersstudioapp.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
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
import com.thespeakers_studio.thespeakersstudioapp.handler.TimerWatchHandler;
import com.thespeakers_studio.thespeakersstudioapp.settings.SettingsUtils;
import com.thespeakers_studio.thespeakersstudioapp.service.MessageFriend;
import com.thespeakers_studio.thespeakersstudioapp.utils.OutlineHelper;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;

import com.thespeakers_studio.thespeakersstudioapp.model.Outline;
import com.thespeakers_studio.thespeakersstudioapp.model.OutlineItem;

import java.util.ArrayList;
import java.util.Set;

import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.LOGD;

/**
 * Created by smcgi_000 on 7/20/2016.
 */
public class PresentationPracticeDialog extends DialogFragment implements
        View.OnClickListener, TimerWatchHandler.TimerWatchInterface {



    public interface PresentationPracticeDialogInterface {
        public void onServiceKilled(Outline outline, boolean isFinished);
    }

    public static final String TAG = PresentationPracticeDialog.class.getSimpleName();

    private PresentationPracticeDialogInterface mInterface;
    private Outline mOutline;
    private OutlineHelper mOutlineHelper = new OutlineHelper();
    private int mDuration = 0;

    // states and settings
    private boolean mTimerInProgress;

    private boolean mIsPractice;
    private boolean mDisplayTimer;
    private boolean mShowWarning;

    private boolean mIsTimerStarted;
    private boolean mIsTimerFinished;
    private boolean mIsTimerPaused;

    private boolean mShowingTopic;
    //

    private Dialog mDialog;

    // views!
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
    //

    // service stuff
    private TimerWatchHandler mTimerWatchHandler;
    private MessageFriend mMessageFriend = new MessageFriend();
    //

    // TODO: make animation times and durations constants
    private final int INTERSTITIAL_DURATION = 1000;
    private final int ANIMATION_DURATION = 300;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRetainInstance(true);
    }

    public void setInterface(PresentationPracticeDialogInterface inter) {
        mInterface = inter;
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

    public void setBinder(IBinder binder) {
        Messenger serviceMessenger = new Messenger(binder);
        mTimerWatchHandler = new TimerWatchHandler(this);
        Messenger thisMessenger = new Messenger(mTimerWatchHandler);

        mMessageFriend.setMessengers(serviceMessenger, thisMessenger);

        LOGD(TAG, "Binder set!");
    }

    // called when the dialog is shown
    @Override
    public void onStart() {
        super.onStart();
        // we will wait for setup to be called by the service
        mMessageFriend.sendMessage(MessageFriend.MSG_REGISTER);
    }

    // removes this item from the service, so it won't be getting messages anymore
    // also kills the interface on our handler so the dialog won't keep trying to update
    public void unregister() {
        LOGD(TAG, "Unregistering");
        mMessageFriend.sendMessage(MessageFriend.MSG_UNREGISTER);
        if (mTimerWatchHandler != null) {
            mTimerWatchHandler.disableInterface();
        }
    }

    // setup is part of the TimerWatchInterface, and is called when the service is ready to go
    @Override
    public void setup (Bundle outlineBundle, int duration, boolean inProgress) {
        if (outlineBundle != null) {
            mOutline = new Outline(getContext(), outlineBundle);
            mIsPractice = true;
        } else {
            mIsPractice = false;
            mDuration = duration;
        }
        mDisplayTimer = SettingsUtils.getTimerShow(getContext());
        mShowWarning = SettingsUtils.getTimerWarning(getContext());

        if (inProgress) {
            mIsTimerStarted = true;
        } else {
            startTimer();
        }
    }

    // we are ready to go, so send the signal to the service to begin
    private void startTimer() {
        mMessageFriend.sendMessage(MessageFriend.MSG_START);
    }

    // called when the dialog is sent away
    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        LOGD(TAG, "onCancel");
        killService();
    }

    // kills the service and then dismisses the dialog
    public void dismissKill() {
        killService();
        dismiss();
    }

    // send a message to the service that it should go away
    // They should all be destroyed.
    public void killService() {
        LOGD(TAG, "killing Service");
        mMessageFriend.sendMessage(MessageFriend.MSG_KILL);
    }

    // serviceKilled is part of the TimerWatchHandler.TimerWatchInterface
    // called when the handler receives a message that the service is dead
    @Override
    public void serviceKilled(Bundle outlineBundle, boolean isFinished) {
        mOutline = new Outline(getContext(), outlineBundle);
        if (mInterface != null) {
            mInterface.onServiceKilled(mOutline, isFinished);
        }
        // reset everything
        mMessageFriend.resetDestinationMessenger();
        mIsTimerPaused = false;
        mIsTimerStarted = false;
        mIsTimerFinished = false;
    }

    // an internal interface to handle what happens after the fade-in animation, if necessary
    private interface showViewInterface {
        public void onReadyToShow(View v);
    }

    // a universal method to show or hide the various views on the dialog
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
        if (!isAdded()) {
            return;
        }

        mIsTimerFinished = true;

        showText(mOutputSubView, "");
        showText(mOutputMainView, R.string.done);

        hideButton(mButtonLeft);
        hideButton(mButtonRight);
        showButton(mButtonDone);

        blink(mTimerTotalView);
    }

    @Override
    public void updateTime(int currentRemaining, int totalRemaining, int elapsed) {
        if (!isAdded()) {
            return;
        }

        if (mIsTimerStarted && mDisplayTimer && mIsPractice) {
            // show the time for the whole presentation in smaller output
            setTimer(mTimerTotalView, totalRemaining);
        }
        if (!mIsTimerFinished) {
            if (mIsTimerStarted && mDisplayTimer) {
                // show the current remaining time in 0:00 format
                setTimer(mTimerView, currentRemaining);
            } else if (mIsTimerStarted) {
                // if we don't want to show the timer, don't show the timer
                // if mTopicText is not empty, we are showing the topic name for a second
                mTimerView.setText("");
            } else {
                prepareDelay();
                // show the 5, 4, 3, 2, 1 countdown
                showText(mTimerView, Utils.secondsFromMillis(currentRemaining));
            }
        } else {
            // we are totally done, so we can begin counting up!
            mTimerView.setTextColor(ContextCompat.getColor(getActivity(), R.color.success));
            setTimer(mTimerView, -elapsed);
        }
    }

    @Override
    public void timeWarning() {
        showText(mWarningView, R.string.two_minutes_left);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showText(mWarningView, "");
            }
        }, SettingsUtils.TIMER_WARNING_DURATION);
    }

    @Override
    public void outlineItem(final OutlineItem item, int remainingTime) {
        if (!isAdded()) {
            return;
        }

        // when we get outline items, we know the timer has started;
        mIsTimerStarted = true;

        if (item.getParentId().equals(OutlineItem.NO_PARENT)) {
            // FIXME: only show the topic name if there's enough time left to so
            //if (remainingTime + item.getDuration() > INTERSTITIAL_DURATION) {
                // this is a top level item, so we will show it for a second and then move on
                // to the first sub-item
                showText(mOutputMainView, "");
                hideView(mBulletList);
                showText(mBulletListHeader, "");
                showText(mOutputSubView, "");
                showText(mOutputMainView, item.getText());

                hideButton(mButtonLeft);
                hideButton(mButtonRight);

                mShowingTopic = true;
            //}
        } else {
            //this is a sub-item

            if (!mShowingTopic) {
                showOutlineItem(item);
            } else {
                // delay showing the next item for a second
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mShowingTopic = false;
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

        showText(mOutputSubView, item.getTopicText());

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

    @Override
    public void pause() {
        mIsTimerPaused = true;

        showText(mOutputMainView, R.string.paused);

        hideView(mBulletListHeader);
        hideView(mBulletList);

        blink(mTimerView);
        hideView(mButtonRight);
        //hideView(mButtonLeft);
    }
    @Override
    public void resume(OutlineItem item) {
        mIsTimerPaused = false;
        mTimerView.clearAnimation();
        outlineItem(item, 0);
    }

    private void setTimer(TextView timer, int time) {
        timer.setVisibility(View.VISIBLE);
        timer.setText(Utils.getTimeStringFromMillis(time, getResources()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.practice_timer_current:
                // tap the time to pause or resume the timer
                mMessageFriend.sendMessage(MessageFriend.MSG_PAUSE);
                break;
            case R.id.button_next:
                if (mIsTimerFinished) {
                    dismissKill();
                } else if (mIsTimerStarted && !mIsTimerPaused) {
                    mMessageFriend.sendMessage(MessageFriend.MSG_NEXT);
                }
                break;
        }
    }

}
