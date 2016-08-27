package com.thespeakers_studio.thespeakersstudioapp.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.activity.PracticeSetupActivity;
import com.thespeakers_studio.thespeakersstudioapp.model.Outline;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;

import java.util.ArrayList;

import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.LOGD;

/**
 * Created by smcgi_000 on 8/23/2016.
 */
public class TimerService extends Service {
    private static String TAG = "SS_timerService";

    // for showing and hiding notifications
    NotificationManager mNM;

    ArrayList<Messenger> mClients = new ArrayList<>();

    private Outline mOutline;

    static final int ONGOING_NOTIFICATION_ID = 1;

    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_OUTLINE_BUNDLE = 3;
    static final int MSG_TIME_STARTED = 4;

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_OUTLINE_BUNDLE:
                    Bundle outlineBundle = msg.getData();
                    setOutline(outlineBundle);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    private void doSendMessage(int what, int argument) {
        for (Messenger messenger : mClients) {
            try {
                messenger.send(Message.obtain(
                        null, what, argument, 0
                ));
            } catch (RemoteException e) {
                mClients.remove(messenger);
            }
        }
    }

    private void setOutline(Bundle outlineBundle) {
        mOutline = new Outline(getApplicationContext(), outlineBundle);

        showNotification(mOutline.getDurationMillis());

        doSendMessage(MSG_TIME_STARTED, (int) mOutline.getDurationMillis());
    }

    @Override
    public void onCreate() {
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        //showNotification();
    }

    @Override
    public void onDestroy() {
        mNM.cancel(R.string.timer_service_started);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    private void showNotification(long duration) {
        CharSequence text = String.format(
                getText(R.string.timer_service_started).toString(),
                Utils.getTimeStringFromMillisInText(duration, getResources())
        );

        Intent notificationIntent = new Intent(this, PracticeSetupActivity.class);
        notificationIntent.putExtra(Utils.INTENT_PRESENTATION_ID, mOutline.getPresentationId());

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_record_voice_over_white_24dp)
                .setTicker(text)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getText(R.string.app_name))
                .setContentText(text)
                .setContentIntent(contentIntent)
                .getNotification();

        startForeground(ONGOING_NOTIFICATION_ID, notification);
        //mNM.notify(R.string.timer_service_started, notification);
    }

    private int mStartTime;




}
