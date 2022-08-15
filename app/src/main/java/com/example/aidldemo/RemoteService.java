package com.example.aidldemo;


import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Messenger;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.aidldemo.entity.Message;

import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RemoteService extends Service {

    private boolean isConnected = false;

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull android.os.Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            // 通过Parcel来反序列化一个对象的时候，最好用bundle设置一下对象的ClassLoader，否则可能序列化异常
            bundle.setClassLoader(Message.class.getClassLoader());
            Message message = bundle.getParcelable("message");
            // handleMessage方法是在主线程，所以这里可以直接进行UI操作
            Toast.makeText(RemoteService.this, message.getContent(), Toast.LENGTH_SHORT).show();

            try {
                // 获取android.os.Message消息发送方的Messenger对象（即客户端的Messenger），这样就可以给客户端回复一个消息
                Messenger messengerClient = msg.replyTo;
                android.os.Message messageHandlerReply = new android.os.Message();

                // 这里android的Message需要塞入一个bundle, bundle中用键值对的方式塞入真正要传递的数据
                Message replyMessage = new Message();
                replyMessage.setContent("this is a reply message");
                Bundle bundleReply = new Bundle();
                bundleReply.putParcelable("replyMessage", replyMessage);
                messageHandlerReply.setData(bundleReply);
                messengerClient.send(messageHandlerReply);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
    };

    // 不能使用ArrayList, 因为由ipc传递过来的对象会经过序列化和反序列化，那么在client侧发送来的对象是同一个对象时，
    // 传递到service侧就不是同一个对象了，所以当想使用list.remove(obj)的时候，就不能按我们预想的去移除对象
    // 这里得用RemoteCallbackList来解决想这个问题
    private RemoteCallbackList<MessageReceiveListener> messageReceiveListenerRemoteCallbackList =
            new RemoteCallbackList<>();

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private ScheduledFuture scheduledFuture;
    private Messenger messenger = new Messenger(handler);

    IConnectionService iConnectionService = new IConnectionService.Stub() {

        // aidl接口中的方法不会在主进程中运行，而是在binder的线程池中运行，即下面的三个方法都会跑在不同的线程中
        // 所以有关UI的操作(如下面的Toast)，不能直接在aidl接口的方法中进行，需要使用handler的post转换到主线程中去。
        @Override
        public void connect() throws RemoteException {
            try {
                handler.post(()-> Toast.makeText(RemoteService.this, "Connecting", Toast.LENGTH_LONG).show());
                // 模拟连接时间5s
                Thread.sleep(5000);
                isConnected = true;
                handler.post(()-> Toast.makeText(RemoteService.this, "Connected!", Toast.LENGTH_SHORT).show());

                // 连接上后模拟remoteService端每隔5s回调一下注册的messageReceiveListener的onReceiveMessage方法，即向客户端发送一个message
                scheduledFuture = scheduledThreadPoolExecutor.scheduleAtFixedRate(()->{
                    int size = messageReceiveListenerRemoteCallbackList.beginBroadcast();
                    for (int i =0; i < size; i++){
                        Message message = new Message();
                        message.setContent("this message from remote");
                        try {
                            messageReceiveListenerRemoteCallbackList.getBroadcastItem(i).onReceiveMessage(message);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    messageReceiveListenerRemoteCallbackList.finishBroadcast();
                        },
                        5000, 5000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void disconnected() throws RemoteException {
            isConnected = false;
            scheduledFuture.cancel(true);
            handler.post(()->Toast.makeText(RemoteService.this, "Disconnected!", Toast.LENGTH_SHORT).show());
        }

        @Override
        public boolean isConnected() throws RemoteException {
            return isConnected;
        }
    };

    IMessageService iMessageService = new IMessageService.Stub() {
        @Override
        public void sendMessage(Message message) throws RemoteException {
            handler.post(()-> Toast.makeText(RemoteService.this, message.getContent(), Toast.LENGTH_SHORT).show());
            if (isConnected) {
                message.setSendSuccess(true);
            } else {
                message.setSendSuccess(false);
            }
            Log.d("theEffectOf_in", "send in remoteService the isSendSuccess of message: " + message.isSendSuccess());
        }

        @Override
        public void sendMessageWithInout(Message message) throws RemoteException {
            handler.post(()-> Toast.makeText(RemoteService.this, message.getContent(), Toast.LENGTH_SHORT).show());
            if (isConnected) {
                message.setSendSuccess(true);
            } else {
                message.setSendSuccess(false);
            }
            message.setContent("content changed in service");
            Log.d("theEffectOf_in", "send in remoteService the isSendSuccess of message: "
                    + message.isSendSuccess());
            Log.d("theEffectOf_in", "send in remoteService the content of message: "
                    + message.getContent());
        }

        @Override
        public void registerMessageReciveListener(MessageReceiveListener messageReceiveListener) throws RemoteException {
            if (messageReceiveListener != null) {
                messageReceiveListenerRemoteCallbackList.register(messageReceiveListener);
            }
        }

        @Override
        public void unRegisterMessageReciveListener(MessageReceiveListener messageReceiveListener) throws RemoteException {
            if (messageReceiveListener != null) {
                messageReceiveListenerRemoteCallbackList.unregister(messageReceiveListener);
            }
        }
    };

    IServiceManager iServiceManager = new IServiceManager.Stub() {
        @Override
        public IBinder getService(String serviceName) throws RemoteException {
           if (IConnectionService.class.getSimpleName().equals(serviceName)) {
               return iConnectionService.asBinder();
           } else if (IMessageService.class.getSimpleName().equals(serviceName)) {
               return iMessageService.asBinder();
           } else if (Messenger.class.getSimpleName().equals(serviceName)) {
               return messenger.getBinder();
           } else {
               return null;
           }
        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iServiceManager.asBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);

    }
}
