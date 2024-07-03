package com.jensen.service;

public interface NetdiscService {
    /**初始化服务器
     * 读取配置文件端口号
     * 初始化线程池
     */
    void init();
    /**
     * 接收客户端连接，使用线程池统一处理
     */
    void start();
}
