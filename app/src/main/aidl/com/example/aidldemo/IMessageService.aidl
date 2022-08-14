// IMessageService.aidl
package com.example.aidldemo;
import com.example.aidldemo.entity.Message;
import com.example.aidldemo.MessageReceiveListener;

// 提供消息服务
interface IMessageService {

   // in的作用：表示数据只会从主进程（调用进程）流入服务端，在服务端对message对象的任何改变，
   // 不会引起主进程中message对象的任何变化，（类似函数调用的值传递）
   void sendMessage(in Message message);

   void registerMessageReciveListener(MessageReceiveListener messageReceiveListener);

   void unRegisterMessageReciveListener(MessageReceiveListener messageReceiveListener);
}