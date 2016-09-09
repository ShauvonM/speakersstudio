package com.thespeakers_studio.thespeakersstudioapp.handler;

import android.os.Handler;
import android.os.Message;

import com.thespeakers_studio.thespeakersstudioapp.model.Outline;
import com.thespeakers_studio.thespeakersstudioapp.model.OutlineItem;
import com.thespeakers_studio.thespeakersstudioapp.service.TimerService;
import com.thespeakers_studio.thespeakersstudioapp.settings.SettingsUtils;
import com.thespeakers_studio.thespeakersstudioapp.service.MessageFriend;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;

import java.util.ArrayList;

import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.LOGD;

/**
 * Created by smcgi_000 on 8/31/2016.
 */
public class TimerHandler extends Handler {
    private static final String TAG = TimerHandler.class.getSimpleName();

    // settings
    private boolean mWait;
    private boolean mTrack;
    //

    // global members
    private TimerService mTimerService;
    private Outline mOutline;
    private int mOutlineItemIndex = -1;
    //

    // time values
    private long mStartTime; // stores a unix epoch, so it has to be a long
    private int mElapsed; // the rest are just intervals, so they can be ints
    private int mOutlineDuration = 0;
    private int mCurrentExpiration;
    private int mCurrentItemDuration;
    //

    // states
    private boolean mIsPractice;
    private boolean mFinished;
    private boolean mPaused;
    private boolean mStarted;
    private boolean mFromBundle = false; // TODO: might not need this at all
    //

    public interface TimerInterface {
        public void outlineItem(OutlineItem item, int remainingTime);
        public void updateTime(int currentRemaining, int totalRemaining, int elapsed);
        public void pause();
        public void resume(OutlineItem item);
        public void finish();
    }

    private ArrayList<TimerInterface> mInterface;

    public TimerHandler(TimerService service) {
        super();
        reset();

        mTimerService = service;
        if (service != null) {
            addInterface(service);
        }
    }

    public void reset() {
        LOGD(TAG, "Resetting");

        mOutlineItemIndex = -1;
        mOutlineDuration = 0;
        mIsPractice = false;
        mInterface = new ArrayList<>();
    }

    public void setPrefs(boolean wait, boolean track) {
        mWait = wait;
        mTrack = track;
    }

    public void addInterface(TimerInterface face) {
        mInterface.add(face);
    }

    public void setOutline(Outline outline) {
        mOutline = outline;
        mOutlineDuration = mOutline.getDurationMillis();
        mIsPractice = true;
        LOGD(TAG, "Outline set! Duration: " + mOutlineDuration);
    }

    public Outline getOutline() {
        return mOutline;
    }

    public void setDuration(int duration) {
        mOutlineDuration = duration;
        mIsPractice = false;
    }

    public int getDuration() {
        return mOutlineDuration;
    }

    @Override
    public void handleMessage(Message msg) {
        //LOGD(TAG, "Message received from client " + msg.what);
        switch (msg.what) {
            case MessageFriend.MSG_REGISTER:
                mTimerService.addClient(msg.replyTo);
                break;
            case MessageFriend.MSG_UNREGISTER:
                mTimerService.removeClient(msg.replyTo);
                break;
            case MessageFriend.MSG_START:
                startTimer();
                break;
            case MessageFriend.MSG_PAUSE:
                togglePause();
                break;
            case MessageFriend.MSG_NEXT:
                skipAhead();
                break;
            case MessageFriend.MSG_STOP:
                break;
            case MessageFriend.MSG_KILL:
                stop();
                mTimerService.kill();
            default:
                super.handleMessage(msg);
        }
    }

    public void startTimer() {
        if (mOutline == null && mOutlineDuration == 0) {
            return; // uh oh
        }

        if (mWait) {
            mCurrentExpiration = SettingsUtils.TIMER_DELAY_TIME;
            mStarted = false;
        } else {
            initStart();
        }

        if (!mPaused) {
            postDelayed(updateTimerRunnable, 500);
        } else {
            for (TimerInterface face : mInterface) {
                face.updateTime(mCurrentExpiration - mElapsed,
                        mOutlineDuration - mElapsed, mElapsed);
                face.pause();
            }
        }
    }

    private void initStart() {
        mStarted = true;
        mStartTime = 0;
        mElapsed = 0;
        mOutlineItemIndex = -1;
        mCurrentExpiration = 0;
    }

    public void stop() {
        removeCallbacks(updateTimerRunnable);
    }

    public boolean isStarted() {
        return mStarted;
    }

    public boolean isFinished() {
        return mFinished;
    }

    public boolean isPaused() {
        return mPaused;
    }

    /*
    public void vibrate(long[] pattern) {
        if (pattern.length == 0) {
            return;
        }
        if (mVibrate) {
            Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(pattern, -1);
        }
    }
    public void vibrate(int pattern) {
        vibrate(new long[] {pattern});
    }
    */

    public void skipAhead() {
        if (mTrack) {
            // store the duration this thing actually took
            OutlineItem currentItem = getCurrentItem();
            int currentRemaining = mCurrentExpiration - mElapsed;
            currentItem.setTimedDuration(mCurrentItemDuration - currentRemaining);
        }
        // fudge the time a little bit so that we are sure we are on the second
        // yes this will technically throw off the duration,
        // but by less than a second
        mStartTime = Utils.now() - mElapsed;
        nextItem();
    }

    public OutlineItem getCurrentItem() {
        return mOutline.getItem(mOutlineItemIndex);
    }

    private void nextItem() {
        if (mIsPractice) {
            if (!mStarted) {
                initStart();
            }
            mOutlineItemIndex++;
            if (mOutlineItemIndex < mOutline.getItemCount()) {
                final OutlineItem item = getCurrentItem();

                int thisDuration = item.getDuration();

                // collect any time left over from the last item
                // (which will be > 0 if the user skipped to the next item)
                int remainingTime = mCurrentExpiration - mElapsed;

                // skip items with 0 duration,
                // or items that will have 0 duration with any left-over time
                if (thisDuration == 0 || (remainingTime + thisDuration) <= 0) {
                    nextItem();
                    return;
                }

                for (TimerInterface callback : mInterface) {
                    callback.outlineItem(item, remainingTime);
                }

                if (item.getParentId().equals(OutlineItem.NO_PARENT)) {
                    //vibrate(Utils.VIBRATE_PULSE);

                    // move on to the next item to set up the duration
                    nextItem();
                } else {
                    //this is a sub-item

                    // set the timer value - this is what the timer is counting down to
                    mCurrentExpiration = mElapsed + (remainingTime + thisDuration);

                    // store this item's duration, so we can know how long it takes
                    //  (if the user skips it early)
                    mCurrentItemDuration = remainingTime + thisDuration;
                }
            } else {
                // we're done!
                finish();
            }

        } else { // isPractice == false

            if (!mStarted) {
                initStart();
                mCurrentExpiration = mOutlineDuration;
            } else if (mPaused) {
                mPaused = false;
            } else {
                finish();
            }

        }
    }

    private void broadcastItem() {

    }

    private void finish() {
        mFinished = true;

        /*
        vibrate(new long[]{Utils.VIBRATE_PULSE, Utils.VIBRATE_PULSE_GAP, Utils.VIBRATE_PULSE,
                Utils.VIBRATE_PULSE_GAP, Utils.VIBRATE_PULSE});
                */
        mOutlineDuration = 0;
        mCurrentExpiration = 0;
        mStartTime = 0;

        for (TimerInterface face : mInterface) {
            face.finish();
        }
    }

    public boolean togglePause() {
        if (mPaused) {
            // resume
            mPaused = false;
            mStartTime = 0;

            // update the durations
            mOutlineDuration = mOutlineDuration - mElapsed;
            if (mStarted) {
                mCurrentExpiration = mCurrentExpiration - mElapsed;
            }

            //resumeTimer();

            for (TimerInterface face : mInterface) {
                face.resume(getCurrentItem());
            }
            postDelayed(updateTimerRunnable, 0);
        } else {
            // pause
            mPaused = true;

            for (TimerInterface face : mInterface) {
                face.pause();
            }
            removeCallbacks(updateTimerRunnable);
        }
        return mPaused;
    }

    /*
    private void resumeTimer() {
        OutlineItem item = getCurrentItem(); //mOutline.getItem(mOutlineItemIndex);
        // we take a step back so that we can redo the last step
        // and trigger all the callbacks and whatnot
        mCurrentExpiration -= item.getDuration();
        mOutlineItemIndex--;
        nextItem();
    }
    */

    private Runnable updateTimerRunnable = new Runnable() {
        @Override
        public void run() {
            if (mStartTime == 0) {
                mStartTime = Utils.now();
            }

            int elapsed = (int) (Utils.now() - mStartTime);
            mElapsed = Utils.roundDownToThousand(elapsed);
            // totalRemaining is the duration of the entire presentation, which we want to stop at 0
            int totalRemaining = Math.max(mOutlineDuration - mElapsed, 0);
            // currentRemaining is the duration of the current section of the outline
            int currentRemaining = mCurrentExpiration - mElapsed;

            //LOGD(TAG, "elapsed time: " + mElapsed + " " + totalRemaining + " " + currentRemaining);

            TimerHandler.this.postDelayed(this, 100);

            if (currentRemaining <= 0 && !mFinished) {
                nextItem();
            } else {
                for (TimerInterface face : mInterface) {
                    face.updateTime(currentRemaining, totalRemaining, mElapsed);
                }
            }
        }
    };
}
