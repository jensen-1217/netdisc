package com.jensen.service;

import java.io.File;

public interface FileUpDownService {
    //启动
    void start();

    //浏览目录
    void scanDirection(File file);

    //下载
    void downloadFile(File file);

    //上传
    void uploadFile(File file);
}
