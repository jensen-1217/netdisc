package com.jensen.service;

import com.jensen.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetdiscServiceImp implements NetdiscService {
    private ServerSocket serverSocket;//监听客户端连接请求
    private ResourceBundle bundle;//读取配置文件
    private ExecutorService threadPool;//线程池
    private static final int THREAD_POOL_SIZE=50;//线程池大小
    private static Logger logger=LoggerFactory.getLogger("NetdiscServiceImp类");
    public NetdiscServiceImp(){
        init();
    }


    /**
     * 读取配置文件
     * 初始化服务器
     * 初始化线程池
     */
    @Override
    public void init() {
        bundle=ResourceBundle.getBundle("config");//读取配置文件
        int port=Integer.parseInt(bundle.getString("serverPort"));
        logger.trace("获取到端口号了");
        //bundle.getString获取配置文件中键为servePort的值，返回为字符串，Interger.parseInt将字符串转换为整数
        try {
            serverSocket = new ServerSocket(port);
            //创建一个套接字
        } catch (IOException e) {
            throw new BusinessException("创建端口失败，请检查端口占用");
        }

        threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        //初始化线程池
        logger.trace("线程初始化成功");
    }


    /**
     * 接收客户端连接，使用线程池处理
     */
    @Override
    public void start() {
        while (true){
            try {
                //监听客户端连接，连接成功生成socket对象
                logger.trace("正在监听客户端连接");
                Socket socket = serverSocket.accept();
                //接收到请求后，业务提交给线程池
                threadPool.submit(new FileUpDownServiceImp(socket));

                logger.trace("客户端连接成功");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
