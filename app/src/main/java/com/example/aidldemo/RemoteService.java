package com.example.aidldemo;


import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.aidldemo.entity.Message;

import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RemoteService extends Service {

    private boolean isConnected = false;

    private Handler handler = new Handler(Looper.getMainLooper());

    private ArrayList<MessageReceiveListener> messageReceiveListenerArrayList = new ArrayList<>();

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private ScheduledFuture scheduledFuture;

    IConnectionService iConnectionService = new IConnectionService.Stub() {

        // aidl接口中的方法不会在主进程中运行，而是在binder的线程池中运行，即下面的三个方法都会跑在不同的线程中
        // 所以有关UI的操作(如下面的Toast)，不能直接在aidl接口的方法中进行，需要使用handler的post转换到主线程中去。
        @Override
        public void connect() throws RemoteException {
            try {
                handler.post(()-> Toast.makeText(RemoteService.this, "Connecting", Toast.LENGTH_SHORT).show());
                // 模拟连接时间5s
                Thread.sleep(5000);
                isConnected = true;
                handler.post(()-> Toast.makeText(RemoteService.this, "Connected!", Toast.LENGTH_SHORT).show());
                // 连接上后模拟remoteService端每隔5s回调一下注册的messageReceiveListener的onReceiveMessage方法，即向客户端发送一个message
                scheduledFuture = scheduledThreadPoolExecutor.scheduleAtFixedRate(()->{
                    for (MessageReceiveListener messageReceiveListener: messageReceiveListenerArrayList){
                        Message message = new Message();
                        message.setContent("this message from remote");
                        try {
                            messageReceiveListener.onReceiveMessage(message);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                        }, 5000, 5000, TimeUnit.MILLISECONDS);
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
        }

        @Override
        public void registerMessageReciveListener(MessageReceiveListener messageReceiveListener) throws RemoteException {
            if (messageReceiveListener != null) {
                messageReceiveListenerArrayList.add(messageReceiveListener);
            }
        }

        @Override
        public void unRegisterMessageReciveListener(MessageReceiveListener messageReceiveListener) throws RemoteException {
            if (messageReceiveListener != null) {
                messageReceiveListenerArrayList.remove(messageReceiveListener);
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
