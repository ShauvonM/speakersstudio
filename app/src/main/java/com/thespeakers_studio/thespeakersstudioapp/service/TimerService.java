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
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;

import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.LOGD;
import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.makeLogTag;

/**
 * Created by smcgi_000 on 8/31/2016.
 */
public class TimerService extends Service implements TimerHandler.TimerInterface {
    private static final String TAG = TimerService.class.getSimpleName();

    private MessageFriend mMessageFriend;

    private TimerHandler mTimerHandler = new TimerHandler(this);
    private Messenger mMessenger = new Messenger(mTimerHandler);

    private boolean mIsPractice;

    @Override
    public void onCreate() {
        mMessageFriend = new MessageFriend();
        mMessageFriend.setReplyToMessenger(mMessenger);

        LOGD(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent restartIntent = new Intent(this, PracticeSetupActivity.class);
        Bundle outlineBundle = intent.getBundleExtra(Utils.INTENT_OUTLINE);
        mIsPractice = outlineBundle != null;
        int duration;
        if (mIsPractice) {
            Outline outline = new Outline(this, outlineBundle);
            mTimerHandler.setOutline(outline);
            restartIntent.putExtra(Utils.INTENT_PRESENTATION_ID, outline.getPresentationId());
            duration = outline.getDurationMillis();
        } else {
            duration = intent.getIntExtra(Utils.INTENT_DURATION, 0);
            mTimerHandler.setDuration(duration);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                        restartIntent, 0);

        Notification.Builder notificationBuilder = new Notification.Builder(this);
        notificationBuilder.setSmallIcon(R.drawable.ic_record_voice_over_white_24dp)
                .setContentIntent(pendingIntent)
                .setContentTitle(getString(R.string.timer_service_started))
                .setContentText(String.format(getString(R.string.timer_service_description),
                        Utils.getTimeStringFromMillisInText(duration, getResources())));

        Notification notification;
        if (Utils.versionGreaterThan(15)) {
            notification = notificationBuilder.build();
        } else {
            notification = notificationBuilder.getNotification();
        }

        startForeground(R.id.timer_service_notification, notification);

        return Service.START_NOT_STICKY; // TODO: get the best value here
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
    }

    public void kill() {
        mMessageFriend.sendMessage(MessageFriend.MSG_KILL, mTimerHandler.getOutline().toBundle(),
                mTimerHandler.isFinished() ? 1 : 0);
        stopSelf();
        stopForeground(true);
    }

    //
    // TimerHandler.TimerInterface methods:
    //
    @Override
    public void updateTime(int currentRemaining, int totalRemaining, int elapsed) {
        //LOGD(TAG, "TIME: " + currentRemaining);
        TimerStatus time = new TimerStatus(currentRemaining, totalRemaining, elapsed);
        mMessageFriend.sendMessage(MessageFriend.MSG_TIME, time);
    }

    @Override
    public void outlineItem(OutlineItem item, int remainingTime) {
        LOGD(TAG, "New Outline Item: " + item.getText());
        mMessageFriend.sendMessage(MessageFriend.MSG_OUTLINE_ITEM, item, remainingTime);
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
        mMessageFriend.sendMessage(MessageFriend.MSG_FINISHED);
    }
}