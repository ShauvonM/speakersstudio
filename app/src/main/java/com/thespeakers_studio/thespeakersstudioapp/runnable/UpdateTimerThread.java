package com.thespeakers_studio.thespeakersstudioapp.runnable;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.model.Outline;
import com.thespeakers_studio.thespeakersstudioapp.model.OutlineItem;
import com.thespeakers_studio.thespeakersstudioapp.settings.SettingsUtils;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;

import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.LOGD;

/**
 * Created by smcgi_000 on 8/25/2016.
 */
public class UpdateTimerThread {

    private String TAG = "SS_UpdateTimerThread";

    private final int DELAY_TIME = 5000;

    private Handler mTimeHandler = new Handler();

    private Context mContext;

    private Outline mOutline;

    private boolean mFromBundle;

    private long mStartTime;
    private long mElapsed;
    private long mOutlineDuration;
    private long mCurrentExpiration;
    private long mCurrentItemDuration;

    private int mOutlineItemIndex;

    private boolean mStarted;
    private boolean mFinished;
    private boolean mPaused;

    private boolean mVibrate;
    private boolean mTrack;

    private boolean mIsPractice;

    private UpdateTimerInterface mInterface;

    public static String BUNDLE_TIMER = "timer";

    private String BUNDLE_START_TIME = "start_time";
    private String BUNDLE_CURRENT_EXPIRATION = "current_expiration";
    private String BUNDLE_CURRENT_ITEM_DURATION = "current_item_duration";
    private String BUNDLE_STARTED = "started";
    private String BUNDLE_PAUSED = "paused";
    private String BUNDLE_FINISHED = "finished";

    private String BUNDLE_ITEM_INDEX = "outline_item_index";

    public interface UpdateTimerInterface {
        public void outlineItem(OutlineItem item, long remainingTime);
        public void updateTime(long currentRemaining, long totalRemaining, long elapsed);
        public void finish();
    }

    public UpdateTimerThread(Context context, Bundle timerBundle) {
        mContext = context;
        mOutlineItemIndex = -1;
        mOutlineDuration = 0;
        mVibrate = SettingsUtils.getTimerVibrate(mContext);
        mTrack = SettingsUtils.getTimerTrack(mContext);

        if (timerBundle != null && timerBundle.getLong(BUNDLE_START_TIME, 0) > 0) {
            mFromBundle = true;

            mStartTime = timerBundle.getLong(BUNDLE_START_TIME);
            mCurrentExpiration = timerBundle.getLong(BUNDLE_CURRENT_EXPIRATION);
            mCurrentItemDuration = timerBundle.getLong(BUNDLE_CURRENT_ITEM_DURATION);

            mStarted = timerBundle.getBoolean(BUNDLE_STARTED);
            mPaused = timerBundle.getBoolean(BUNDLE_PAUSED);
            mFinished = timerBundle.getBoolean(BUNDLE_FINISHED);

            mOutlineItemIndex = timerBundle.getInt(BUNDLE_ITEM_INDEX, -1);

            LOGD(TAG, "Restarting from bundle, start time: " + mStartTime);
        }
    }

    public void setup(UpdateTimerInterface callback, Outline outline, int duration) {
        mInterface = callback;

        mOutline = outline;
        mIsPractice = mOutline != null;
        mOutlineDuration = mIsPractice ? mOutline.getDurationMillis() : duration;
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();

        bundle.putLong(BUNDLE_START_TIME, mStartTime);
        bundle.putLong(BUNDLE_CURRENT_EXPIRATION, mCurrentExpiration);
        bundle.putLong(BUNDLE_CURRENT_ITEM_DURATION, mCurrentItemDuration);
        bundle.putBoolean(BUNDLE_STARTED, mStarted);
        bundle.putBoolean(BUNDLE_PAUSED, mPaused);
        bundle.putBoolean(BUNDLE_FINISHED, mFinished);
        bundle.putInt(BUNDLE_ITEM_INDEX, mOutlineItemIndex);

        return bundle;
    }

    public void start(boolean delay) {
        if (mOutline == null && mOutlineDuration == 0) {
            return; // uh oh
        }

        if (mFromBundle) {
            if (mOutlineItemIndex >= 0 && mOutlineItemIndex < mOutline.getItemCount()) {
                OutlineItem item = mOutline.getItem(mOutlineItemIndex);
                // we take a step back so that we can redo the last step
                // and trigger all the callbacks and whatnot
                mCurrentExpiration -= item.getDuration();
                mOutlineItemIndex--;
                nextItem();
            }
        } else {
            if (delay) {
                mCurrentExpiration = DELAY_TIME;
                mStarted = false;
            } else {
                initStart();
            }
        }

        mTimeHandler.postDelayed(updateTimerThread, 500);
    }

    public void start() {
        start(false);
    }

    private void initStart() {
        mStarted = true;
        mStartTime = 0;
        mElapsed = 0;
        mOutlineItemIndex = -1;
        mCurrentExpiration = 0;
    }

    public void stop() {
        mTimeHandler.removeCallbacks(updateTimerThread);
    }

    public boolean isStarted() {
        return mStarted;
    }

    public boolean isFinished() {
        return mFinished;
    }

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

    public void skipAhead() {
        if (mTrack) {
            // store the duration this thing actually took
            OutlineItem currentItem = getCurrentItem();
            long currentRemaining = mCurrentExpiration - mElapsed;
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

                long thisDuration = item.getDuration();
                // collect any time left over from the last item
                // (which will be > 0 if the user skipped to the next item)
                long remainingTime = mCurrentExpiration - mElapsed;

                // skip items with 0 duration,
                // or items that will have 0 duration with any left-over time
                if (thisDuration == 0 || (remainingTime + thisDuration) <= 0) {
                    nextItem();
                    return;
                }

                if (mInterface != null) {
                    mInterface.outlineItem(item, remainingTime);
                }

                if (item.getParentId().equals(OutlineItem.NO_PARENT)) {
                    vibrate(Utils.VIBRATE_PULSE);

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

    private void finish() {
        mFinished = true;

        vibrate(new long[]{Utils.VIBRATE_PULSE, Utils.VIBRATE_PULSE_GAP, Utils.VIBRATE_PULSE,
                Utils.VIBRATE_PULSE_GAP, Utils.VIBRATE_PULSE});
        mOutlineDuration = 0;
        mCurrentExpiration = 0;
        mStartTime = 0;

        if (mInterface != null) {
            mInterface.finish();
        }
    }

    public boolean togglePause() {
        if (mPaused) {
            mPaused = false;
            mStartTime = 0;
            mOutlineItemIndex--;
            nextItem();

            mTimeHandler.postDelayed(updateTimerThread, 0);
        } else {
            mPaused = true;
            mTimeHandler.removeCallbacks(updateTimerThread);
            mOutlineDuration = mOutlineDuration - mElapsed;
            if (mStarted) {
                mCurrentExpiration = mCurrentExpiration - mElapsed;
            }
        }
        return mPaused;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public long getCurrentItemDuration() {
        return mCurrentItemDuration;
    }

    private Runnable updateTimerThread = new Runnable() {
        @Override
        public void run() {
            if (mStartTime == 0) {
                mStartTime = Utils.now();
            }

            long elapsed = Utils.now() - mStartTime;
            mElapsed = Utils.roundDownToThousand(elapsed);
            // totalRemaining is the duration of the entire presentation, which we want to stop at 0
            long totalRemaining = Math.max(mOutlineDuration - mElapsed, 0);
            // currentRemaining is the duration of the current section of the outline
            long currentRemaining = mCurrentExpiration - mElapsed;

            //LOGD(TAG, "elapsed time: " + mElapsed + " " + totalRemaining + " " + currentRemaining);

            mTimeHandler.postDelayed(this, 100);

            if (currentRemaining <= 0 && !mFinished) {
                nextItem();
            } else {
                if (mInterface != null) {
                    mInterface.updateTime(currentRemaining, totalRemaining, mElapsed);
                }
            }
        }
    };

}
