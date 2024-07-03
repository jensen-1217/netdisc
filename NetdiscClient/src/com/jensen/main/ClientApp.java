package com.jensen.main;

import com.jensen.service.FileUpDownService;
import com.jensen.service.FileUpDownServiceImp;

public class ClientApp {
    public static void main(String[] args) {
        //创建客户端 浏览，上传，下载 业务逻辑类
        FileUpDownService service = new FileUpDownServiceImp();
        //启动客户端
        service.start();
    }
}
