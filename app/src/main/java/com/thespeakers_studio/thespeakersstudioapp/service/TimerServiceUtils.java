package com.thespeakers_studio.thespeakersstudioapp.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.LOGD;

/**
 * Created by smcgi_000 on 8/23/2016.
 */
public class TimerServiceUtils {

    private String TAG = "SS_TimerServiceUtils";

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case TimerService.MSG_TIME_STARTED:
                    LOGD(TAG, "Time started! " + msg.arg1);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private Context mContext;

    private boolean mIsBound;
    private Messenger mService; // mService is the service that we send messages to
    private Messenger mMessenger = new Messenger(new IncomingHandler()); // mMessenger receives messages

    private Queue<Message> mMessageQueue;

    private ServiceConnection mConnection;

    public TimerServiceUtils(Context context) {
        mContext = context;

        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = new Messenger(service);

                sendMessage(TimerService.MSG_REGISTER_CLIENT);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
                LOGD(TAG, "Disconnected.");
            }
        };

        mMessageQueue = new PriorityQueue<>(1, new Comparator<Message>() {
            @Override
            public int compare(Message lhs, Message rhs) {
                return lhs.what < rhs.what ? 0 : 1;
            }
        });

        doBindService();
    }

    private void processQueue() {
        if (mIsBound && mService != null) {
            while(mMessageQueue.size() > 0) {
                Message msg = mMessageQueue.remove();
                try {
                    mService.send(msg);
                } catch (RemoteException e) {
                    // the good news is we don't have to do anything
                    // the bad news is that the service has crashed
                    // oh well
                }
            }
        }
    }

    private void enqueueMessage(Message msg) {
        msg.replyTo = mMessenger;
        mMessageQueue.add(msg);
        processQueue();
    }
    private void sendMessage(int what, String content) {
        enqueueMessage(Message.obtain(null, what, content));
    }
    private void sendMessage(int what) {
        enqueueMessage(Message.obtain(null, what));
    }
    private void sendMessage(int what, Bundle bundle) {
        Message msg = Message.obtain(null, what);
        msg.setData(bundle);
        enqueueMessage(msg);
    }

    public void setOutlineBundle(Bundle outlineBundle) {
        //sendMessage(TimerService.MSG_OUTLINE_BUNDLE, outlineId);
        sendMessage(TimerService.MSG_OUTLINE_BUNDLE, outlineBundle);
    }

    public void doBindService() {
        mContext.bindService(new Intent(mContext, TimerService.class),
                mConnection, Context.BIND_AUTO_CREATE);

        mIsBound = true;
        LOGD(TAG, "Binding.");
    }

    public void doUnbindService() {
        if (mIsBound) {
            sendMessage(TimerService.MSG_UNREGISTER_CLIENT);
            mContext.unbindService(mConnection);
            mIsBound = false;
            LOGD(TAG, "Unbinding.");
        }
    }

}
