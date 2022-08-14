// MessageReceiveListener.aidl
package com.example.aidldemo;
import com.example.aidldemo.entity.Message;

// 用于消息服务的回调
interface MessageReceiveListener {

    void onReceiveMessage(in Message message);

}