package com.jensen.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface FileUpDownService {
    /**
     * 上传文件
     * @param agreement
     * @param netIN
     * @param netOut
     * @throws IOException
     */
    void uploadFile(String agreement, InputStream netIN, OutputStream netOut)throws IOException;

    /**
     * 下载
     * @param agreement
     * @param netIN
     * @param netOut
     * @throws IOException
     */
    void downloadFile(String agreement,InputStream netIN,OutputStream netOut)throws IOException;

    /**
     * 浏览目录
     * @param agreement
     * @param netIN
     * @param netOut
     * @throws IOException
     */
    void scanDirectory(String agreement,InputStream netIN,OutputStream netOut)throws IOException;
}
