package com.thespeakers_studio.thespeakersstudioapp.handler;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.thespeakers_studio.thespeakersstudioapp.model.Outline;
import com.thespeakers_studio.thespeakersstudioapp.model.OutlineItem;
import com.thespeakers_studio.thespeakersstudioapp.model.TimerStatus;
import com.thespeakers_studio.thespeakersstudioapp.service.TimerService;
import com.thespeakers_studio.thespeakersstudioapp.service.MessageFriend;

import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.LOGD;

/**
 * Created by smcgi_000 on 8/31/2016.
 */
public class TimerWatchHandler extends Handler {
    private static final String TAG = TimerWatchHandler.class.getSimpleName();

    public interface TimerWatchInterface extends TimerHandler.TimerInterface {
        public void setup(Bundle outlineBundle, int duration, boolean inProgress);
        public void serviceKilled(Bundle outlineBundle, boolean isFinished);
        public void timeWarning();
    }

    TimerWatchInterface mInterface;
    private boolean alive;

    public TimerWatchHandler (TimerWatchInterface callback) {
        mInterface = callback;
    }

    public void disableInterface() {
        mInterface = null;
        alive = false;
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.what != 20) {
            //LOGD(TAG, "Received message from service: " + msg.what);
        }
        if (mInterface == null) {
            return;
        }
        switch(msg.what) {
            case MessageFriend.MSG_READY:
                // when the service is ready, it will deliver the outline and duration to us
                mInterface.setup(msg.peekData(), msg.arg1, msg.arg2 == 1);
                alive = true;
                break;

            case MessageFriend.MSG_TIME:
                if (!alive) {
                    return;
                }
                // every time the time updates, the service broadcasts the update to us
                msg.getData().setClassLoader(TimerStatus.class.getClassLoader());
                TimerStatus status = msg.getData().getParcelable("data");
                if (status != null && mInterface != null) {
                    mInterface.updateTime(status.currentRemaining, status.totalRemaining,
                            status.elapsedTime);
                }
                break;

            case MessageFriend.MSG_OUTLINE_ITEM:
                if (!alive) {
                    return;
                }
                OutlineItem item = null;
                if (msg.peekData() != null) {
                    // when we get a new outline item, the service will broadcast that
                    msg.getData().setClassLoader(OutlineItem.class.getClassLoader());
                    item = msg.getData().getParcelable("data");
                }
                mInterface.outlineItem(item, msg.arg1);
                break;

            case MessageFriend.MSG_PAUSE:
                // when the timer pauses, we are notified
                mInterface.pause();
                break;

            case MessageFriend.MSG_RESUME:
                // and etcetera
                if (msg.peekData() != null) {
                    msg.getData().setClassLoader(OutlineItem.class.getClassLoader());
                    OutlineItem resumeItem = msg.getData().getParcelable("data");
                    mInterface.resume(resumeItem);
                } else {
                    mInterface.resume(null);
                }
                break;

            case MessageFriend.MSG_FINISHED:
                mInterface.finish();
                break;

            case MessageFriend.MSG_KILL:
                // when the service is killed, it will return the outline and whether or not it
                // went all the way through the duration
                mInterface.serviceKilled(msg.peekData(), msg.arg1 == 1);
                alive = false;
                break;

            case MessageFriend.MSG_WARNING:
                mInterface.timeWarning();

            default:
                super.handleMessage(msg);
        }
    }
}
