package com.example.aidldemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.aidldemo.entity.Message;

public class MainActivity extends AppCompatActivity{

    private Button buttonConnect, buttonDisconnect, buttonIsConnected,
            buttonSendMessage, buttonRegisterListener, buttonUnRegisterListener,
            buttonSendMessageWithInout, buttonSendByMessenger;

    private IConnectionService iConnectionServiceProxy;
    private IMessageService iMessageServiceProxy;
    private IServiceManager iServiceManagerProxy;
    private Messenger messengerProxy;

    // 在client中实现aidl定义的接口方法，相当于远程service可以回调用client的方法，这里
    // MessageReceiveListener的onReceiveMessage方法，是模拟在收到service发送的消息后，client的处理方式
    private MessageReceiveListener messageReceiveListener = new MessageReceiveListener.Stub() {
        @Override
        public void onReceiveMessage(Message message) throws RemoteException {
            // UI相关的处理要放到主线程中去，而aidl中的方法是在binder的线程池中运行，所以这里要使用handler.post
            new Handler(Looper.getMainLooper()).post(
                    ()-> Toast.makeText(MainActivity.this, "client: " + message.getContent(), Toast.LENGTH_SHORT).show());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindResources();

        Intent intent = new Intent(this, RemoteService.class);
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                // 通过serviceManager拿到多个不同服务(这里指ConnectionService和MessageService)
                iServiceManagerProxy = IServiceManager.Stub.asInterface(iBinder);
                try {
                    iConnectionServiceProxy = IConnectionService.Stub.asInterface(
                            iServiceManagerProxy.getService(IConnectionService.class.getSimpleName()));
                    iMessageServiceProxy = IMessageService.Stub.asInterface(
                            iServiceManagerProxy.getService(IMessageService.class.getSimpleName()));
                    messengerProxy = new Messenger(iServiceManagerProxy.getService(Messenger.class.getSimpleName()));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        }, Context.BIND_AUTO_CREATE);

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // 当主线程中不想被接口阻塞时，需要在aidl文件中声明函数为oneway
                    iConnectionServiceProxy.connect();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        buttonDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    iConnectionServiceProxy.disconnected();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        buttonIsConnected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    boolean isConnected = iConnectionServiceProxy.isConnected();
                    Toast.makeText(MainActivity.this, "isConnected?: " + String.valueOf(isConnected),
                            Toast.LENGTH_SHORT).show();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        buttonSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Message message = new Message();
                    message.setContent("this message from client main activity");
                    Log.d("theEffectOf_in", "before send in mainActivity the isSendSuccess of message: "
                            + message.isSendSuccess());
                    iMessageServiceProxy.sendMessage(message);
                    Log.d("theEffectOf_in", "after send in mainActivity the isSendSuccess of message: " + message.isSendSuccess());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        buttonSendMessageWithInout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Message message = new Message();
                    message.setContent("this message from client main activity");
                    Log.d("theEffectOf_in", "before send in mainActivity the content of message: "
                            + message.getContent());
                    Log.d("theEffectOf_in", "before send in mainActivity the isSendSuccess of message: "
                            + message.isSendSuccess());
                    iMessageServiceProxy.sendMessageWithInout(message);
                    Log.d("theEffectOf_in", "after send in mainActivity the isSendSuccess of message: "
                            + message.isSendSuccess());
                    Log.d("theEffectOf_in", "after send in mainActivity the content of message: "
                            + message.getContent());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        buttonRegisterListener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    iMessageServiceProxy.registerMessageReciveListener(messageReceiveListener);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        buttonUnRegisterListener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    iMessageServiceProxy.unRegisterMessageReciveListener(messageReceiveListener);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        buttonSendByMessenger.setOnClickListener(new View.OnClickListener() {
            // Messenger来进行ipc, 相当于拿到remote侧的handler, 这边的操作即给handler发送一个带数据的android.os.Message
            // remote端的handler会接收到这个数据，在其handleMessage方法中处理
            @Override
            public void onClick(View v) {
                try {
                    Message message = new Message();
                    message.setContent("this is a message send from mainActivity by Messenger");
                    android.os.Message messageHandler = new android.os.Message();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("message", message);
                    messageHandler.setData(bundle);
                    messengerProxy.send(messageHandler);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void bindResources() {
        buttonConnect = findViewById(R.id.button);
        buttonDisconnect = findViewById(R.id.button2);
        buttonIsConnected = findViewById(R.id.button3);
        buttonSendMessage = findViewById(R.id.button4);
        buttonRegisterListener = findViewById(R.id.button5);
        buttonUnRegisterListener = findViewById(R.id.button6);
        buttonSendMessageWithInout = findViewById(R.id.button7);
        buttonSendByMessenger = findViewById(R.id.button8);
    }

}