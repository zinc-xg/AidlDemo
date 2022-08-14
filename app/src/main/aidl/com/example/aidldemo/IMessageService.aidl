// IMessageService.aidl
package com.example.aidldemo;
import com.example.aidldemo.entity.Message;
import com.example.aidldemo.MessageReceiveListener;

// 提供消息服务
interface IMessageService {

   void sendMessage(in Message message);

   void registerMessageReciveListener(MessageReceiveListener messageReceiveListener);

   void unRegisterMessageReciveListener(MessageReceiveListener messageReceiveListener);
}