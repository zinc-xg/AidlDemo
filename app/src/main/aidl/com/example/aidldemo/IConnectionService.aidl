// IConnectionService.aidl
package com.example.aidldemo;

// 提供连接服务
interface IConnectionService {

    // oneway 生命的方法，调用方不会被阻塞，但要求方法不能有返回值
    oneway void connect();

    void disconnected();

    boolean isConnected();
}