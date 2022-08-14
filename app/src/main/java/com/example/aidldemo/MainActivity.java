package com.example.aidldemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.aidldemo.entity.Message;

public class MainActivity extends AppCompatActivity{

    private Button buttonConnect, buttonDisconnect, buttonIsConnected,
            buttonSendMessage, buttonRegisterListener, buttonUnRegisterListener;
    private IConnectionService iConnectionServiceProxy;
    private IMessageService iMessageServiceProxy;
    private IServiceManager iServiceManagerProxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindResources();

        Intent intent = new Intent(this, RemoteService.class);
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                // 通过色serviceManager拿到多个不同服务(ConnectionService和MessageService)
                iServiceManagerProxy = IServiceManager.Stub.asInterface(iBinder);
                try {
                    iConnectionServiceProxy = IConnectionService.Stub.asInterface(
                            iServiceManagerProxy.getService(IConnectionService.class.getSimpleName()));
                    iMessageServiceProxy = IMessageService.Stub.asInterface(
                            iServiceManagerProxy.getService(IMessageService.class.getSimpleName()));
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
                    iMessageServiceProxy.sendMessage(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        buttonRegisterListener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        buttonUnRegisterListener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
    }

}