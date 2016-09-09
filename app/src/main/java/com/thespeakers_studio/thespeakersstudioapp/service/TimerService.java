package com.thespeakers_studio.thespeakersstudioapp.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.activity.PracticeSetupActivity;
import com.thespeakers_studio.thespeakersstudioapp.handler.TimerHandler;
import com.thespeakers_studio.thespeakersstudioapp.model.Outline;
import com.thespeakers_studio.thespeakersstudioapp.model.OutlineItem;
import com.thespeakers_studio.thespeakersstudioapp.model.TimerStatus;
import com.thespeakers_studio.thespeakersstudioapp.settings.SettingsUtils;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;

import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.LOGD;
import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.makeLogTag;

/**
 * Created by smcgi_000 on 8/31/2016.
 */
public class TimerService extends Service implements TimerHandler.TimerInterface {
    private static final String TAG = TimerService.class.getSimpleName();

    private MessageFriend mMessageFriend;

    private TimerHandler mTimerHandler;
    private Messenger mMessenger;

    private boolean mIsPractice;

    private Intent mReopenAppIntent;

    private int mTimeRemaining;
    private int mCurrentRemaining;
    private int mElapsed;

    private OutlineItem mCurrentTopic;
    private OutlineItem mCurrentItem;

    private boolean mVibrate;
    private boolean mShowWarning;

    private boolean mIsTopicVibrating;
    private boolean mFinished;

    @Override
    public void onCreate() {
        mTimerHandler = new TimerHandler(this);
        mMessenger = new Messenger(mTimerHandler);

        mMessageFriend = new MessageFriend();
        mMessageFriend.setReplyToMessenger(mMessenger);

        mReopenAppIntent = new Intent(this, PracticeSetupActivity.class);
        // FIXME: need a stack or something so the back button will work when we reopen the activity

        LOGD(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle outlineBundle = intent.getBundleExtra(Utils.INTENT_OUTLINE);
        mIsPractice = outlineBundle != null;
        int duration;
        if (mIsPractice) {
            Outline outline = new Outline(this, outlineBundle);
            mTimerHandler.setOutline(outline);
            mReopenAppIntent.putExtra(Utils.INTENT_PRESENTATION_ID, outline.getPresentationId());
            duration = outline.getDurationMillis();
        } else {
            duration = intent.getIntExtra(Utils.INTENT_DURATION, 0);
            mTimerHandler.setDuration(duration);
        }
        mTimeRemaining = duration;

        boolean wait = intent.getBooleanExtra(SettingsUtils.PREF_TIMER_WAIT, true);
        boolean track = intent.getBooleanExtra(SettingsUtils.PREF_TIMER_TRACK, true);
        mTimerHandler.setPrefs(wait, track);

        mVibrate = intent.getBooleanExtra(SettingsUtils.PREF_TIMER_VIBRATE, true);
        mShowWarning = intent.getBooleanExtra(SettingsUtils.PREF_TIMER_WARNING, true);

        return Service.START_NOT_STICKY; // TODO: get the best value here
    }

    private void showNotification(OutlineItem item) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                mReopenAppIntent, 0);

        String title = getString(R.string.app_name);
        String text;
        if (item != null) {
            String itemText = item.getText();
            title += " " + getString(R.string.timer_service_started);
            text = String.format(getString(R.string.timer_service_description),
                        itemText,
                        Utils.getTimeStringFromMillisInText(mTimeRemaining, getResources()));
        } else {
            title += " " + getString(R.string.timer_service_finished);
            text = getString(R.string.timer_service_finished_description);
        }

        Notification.Builder notificationBuilder = new Notification.Builder(this);
        notificationBuilder.setSmallIcon(R.drawable.ic_record_voice_over_white_24dp)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(text);

        Notification notification;
        if (Utils.versionGreaterThan(15)) {
            notification = notificationBuilder.build();
        } else {
            notification = notificationBuilder.getNotification();
        }

        startForeground(R.id.timer_service_notification, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        LOGD(TAG, "onBind");
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LOGD(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        LOGD(TAG, "onDestroy");
        super.onDestroy();
    }

    //
    // handle things and send messages:
    //
    public void start() {
        // TODO: show the notification here?
    }

    public void addClient(Messenger messenger) {
        LOGD(TAG, "Adding client!");
        mMessageFriend.addDestinationMessenger(messenger);

        mMessageFriend.sendMessage(MessageFriend.MSG_READY, mTimerHandler.getOutline().toBundle(),
                mTimerHandler.getDuration(), mTimerHandler.isStarted() ? 1 : 0);

        if (mCurrentItem != null && !mFinished) {
            mMessageFriend.sendMessage(MessageFriend.MSG_OUTLINE_ITEM,
                    mCurrentItem, mCurrentRemaining - mElapsed);
        }
    }

    public void removeClient(Messenger messenger) {
        LOGD(TAG, "Removing client!");
        mMessageFriend.removeDestinationMessenger(messenger);
    }

    public void kill() {
        if (mVibrate) {
            Utils.vibrateCancel(this);
        }

        mMessageFriend.sendMessage(MessageFriend.MSG_KILL, mTimerHandler.getOutline().toBundle(),
                mTimerHandler.isFinished() ? 1 : 0);
        stopForeground(true);
        stopSelf();
    }

    //
    // TimerHandler.TimerInterface methods:
    //
    @Override
    public void updateTime(int currentRemaining, int totalRemaining, int elapsed) {
        //LOGD(TAG, "TIME: " + currentRemaining);
        TimerStatus time = new TimerStatus(currentRemaining, totalRemaining, elapsed);
        mMessageFriend.sendMessage(MessageFriend.MSG_TIME, time);

        mTimeRemaining = totalRemaining;
        mCurrentRemaining = currentRemaining;
        mElapsed = elapsed;

        // mCurrentItem will not be null once the timer has started, so we can use that to check
        if (mShowWarning && mVibrate && mCurrentItem != null) {
            if (totalRemaining <= SettingsUtils.TIMER_WARNING_TIME) {
                // two minute warning will pulse three times
                Utils.vibrate(this, Utils.VIBRATE_PATTERN_TRIPLE);
                // we did the thing, which means we don't need to do it again
                mShowWarning = false;

                mMessageFriend.sendMessage(MessageFriend.MSG_WARNING);
            }
        }
    }

    @Override
    public void outlineItem(OutlineItem item, int remainingTime) {
        LOGD(TAG, "New Outline Item: " + item.getText());
        mMessageFriend.sendMessage(MessageFriend.MSG_OUTLINE_ITEM, item, remainingTime);
        showNotification(item);

        if (item.getParentId().equals(OutlineItem.NO_PARENT)) {
            if (mVibrate) {
                // for parent items, pulse two times
                Utils.vibrate(this, Utils.VIBRATE_PATTERN_DOUBLE);
                mIsTopicVibrating = true;
            }
            mCurrentTopic = item;
        } else {
            if (mVibrate && !mIsTopicVibrating) {
                // for normal items, pulse once
                Utils.vibratePulse(this);
            }
            mIsTopicVibrating = false;
            // cache the current item to broadcast when clients connect
            mCurrentItem = item;
        }
    }

    @Override
    public void pause() {
        LOGD(TAG, "PAUSE");
        mMessageFriend.sendMessage(MessageFriend.MSG_PAUSE);
    }

    @Override
    public void resume() {
        LOGD(TAG, "RESUME");
        mMessageFriend.sendMessage(MessageFriend.MSG_RESUME);
    }

    @Override
    public void finish() {
        LOGD(TAG, "FINISHED!!!");
        mFinished = true;
        mMessageFriend.sendMessage(MessageFriend.MSG_FINISHED);

        showNotification(null);

        if (mVibrate) {
            Utils.vibrateLongRepeat(this);
        }
    }
}
