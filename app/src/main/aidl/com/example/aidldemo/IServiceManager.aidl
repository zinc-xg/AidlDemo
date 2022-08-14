// IServiceManager.aidl
package com.example.aidldemo;

// 用于管理多个服务对象，即提供一个可以获取多个IBinder的IBinder对象
interface IServiceManager {
    IBinder getService(String serviceName);
}