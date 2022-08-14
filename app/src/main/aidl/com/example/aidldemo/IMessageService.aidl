// IMessageService.aidl
package com.example.aidldemo;
import com.example.aidldemo.entity.Message;
import com.example.aidldemo.MessageReceiveListener;

// 提供消息服务
interface IMessageService {
   // 使用in的对象实体必须要实现android.os.Parcelable接口，实现writeToParcel方法
   // in的作用：表示数据只会从主进程（调用进程）流入服务端，在服务端对message对象的任何改变，
   // 不会引起主进程中message对象的任何变化，（类似函数调用的值传递）
   void sendMessage(in Message message);

   // 用inout关键字，可以使得在函数的调用过程中的改变，传回调用时传入的参数，
   // 即使得调用方式类似“引用传递”，但必须在定义对象的类中添加实现readFromParcel
   void sendMessageWithInout(inout Message message);

   void registerMessageReciveListener(MessageReceiveListener messageReceiveListener);

   void unRegisterMessageReciveListener(MessageReceiveListener messageReceiveListener);
}