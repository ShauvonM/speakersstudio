package com.thespeakers_studio.thespeakersstudioapp.service;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.LOGD;

/**
 * Created by smcgi_000 on 9/1/2016.
 */
public class MessageFriend {
    private static final String TAG = MessageFriend.class.getSimpleName();

    /*
        A helper class to help queue and send messages to one or more messengers
        Calling sendMessage will queue up messages, and those messages will be sent
        as soon as the "replyTo" messenger and "destination" messenger(s) are set.
     */

    // message types
    public static final int MSG_REGISTER = 1;
    public static final int MSG_READY = 2;

    public static final int MSG_START = 6;
    public static final int MSG_PAUSE = 7;
    public static final int MSG_RESUME = 8;
    public static final int MSG_NEXT = 9;
    public static final int MSG_STOP = 10;

    public static final int MSG_TIME = 20;
    public static final int MSG_OUTLINE_ITEM = 21;
    public static final int MSG_FINISHED = 22;

    public static final int MSG_KILL = 25;
    //

    private ArrayList<Messenger> mTo;
    private Messenger mFrom;
    private Queue<Message> mMessageQueue;

    public MessageFriend() {
        mTo = new ArrayList<>();
        mMessageQueue = new PriorityQueue<>(1, new Comparator<Message>() {
            @Override
            public int compare(Message lhs, Message rhs) {
                return lhs.what < rhs.what ? 0 : 1;
            }
        });
    }

    public void setMessengers(Messenger destination, Messenger self) {
        setReplyToMessenger(self);
        addDestinationMessenger(destination);
        processQueue();
    }

    public void setReplyToMessenger(Messenger replyTo) {
        mFrom = replyTo;
    }

    public void addDestinationMessenger(Messenger service) {
        mTo.add(service);
    }

    public void sendMessage(int what, Object obj, int arg, int arg2) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg;
        msg.arg2 = arg2;
        if (obj != null && obj instanceof Bundle) {
            msg.setData((Bundle) obj);
        } else if (obj != null) {
            msg.getData().putParcelable("data", (Parcelable) obj);
        }
        enqueueMessage(msg);
    }
    public void sendMessage(int what, Object obj) {
        sendMessage(what, obj, 0, 0);
    }
    public void sendMessage(int what, int arg) {
        sendMessage(what, null, arg, 0);
    }
    public void sendMessage(int what) {
        sendMessage(what, null, 0, 0);
    }
    public void sendMessage(int what, Bundle bundle, int arg) {
        sendMessage(what, bundle, arg, 0);
    }
    public void sendMessage(int what, Object obj, int arg) {
        sendMessage(what, obj, arg, 0);
    }

    public void processQueue() {
        if (mTo.size() > 0 && mFrom != null) {
            while(mMessageQueue.size() > 0) {
                Message msg = mMessageQueue.remove();
                msg.replyTo = mFrom;
                try {
                    for (Messenger messenger : mTo) {
                        messenger.send(msg);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void enqueueMessage(Message msg) {
        mMessageQueue.add(msg);
        processQueue();
    }

}
