package com.jensen.service;

import com.jensen.exception.BusinessException;
import com.jensen.util.AgreementUtil;
import com.jensen.util.IOUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.ResourceBundle;
import java.util.Scanner;

public class FileUpDownServiceImp implements FileUpDownService{
    public File current=new File("root");
    private ResourceBundle bundle;
    private String downloadPath;
    private String serverIP;
    private Integer serverPort;
    private static Logger logger=LoggerFactory.getLogger("FilenUpDownServiceImp类");

    @Override
    public void start() {
        try {
            //读取配置文件中的数据
            bundle = ResourceBundle.getBundle("config");
            //读取配置文件中下载路径
            downloadPath = bundle.getString("DownloadPath");
            //把下载路径的字符串封装成对象
            File downloadDir = new File(downloadPath);

            if (downloadDir.isFile()) {
                throw new BusinessException("文件不能作为下载目录，请更改下载路径");
            } else if (!downloadDir.exists() && !downloadDir.mkdirs()) {
                throw new BusinessException("下载目录初始化失败，请检查下载路径配置是否正确");
            }

            Scanner sc = new Scanner(System.in);
            System.out.println("------------欢迎使用云盘---------------");

            while (true) {
                System.out.println("----------------------------------------------------------------------");
                System.out.println("1.浏览当前目录\t2.浏览子目录\t3.返回上一级目录\t4.下载文件\t5.上传文件");
                System.out.println("----------------------------------------------------------------------");
                String choice = sc.nextLine();
                switch (choice) {
                    case "1":
                        System.out.println(current);
                        scanDirection(current);
                        break;
                    case "2":
                        System.out.println("请输入要浏览的子目录：");
                        String dir = sc.nextLine();
                        try {
                            //当前目录拼接子目录
                            //System.out.println(current);
                            scanDirection(new File(current, dir));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case "3":
                        if (current.getName().equals("root")) {
                            System.out.println("没有上一级了");
                        } else {
                            //扫码当前文件的父目录
                            scanDirection(current.getParentFile());
                        }
                        break;
                    case "4":
                        System.out.println("请输入需要下载的文件名（含后缀）：");
                        String fileName = sc.nextLine();
                        downloadFile(new File(current, fileName));
                        break;
                    case "5":
                        while (true) {
                            System.out.println("请输入在计算机中要上传的文件路径");
                            String uploadFilePath = sc.nextLine();
                            File upFile = new File(uploadFilePath);
                            if (!upFile.exists()) {
                                logger.trace("文件不存在，请重新输入！");
                            } else if (upFile.isDirectory()) {
                                logger.trace("不支持目录上传！");
                            } else if (upFile.isFile()) {
                                uploadFile(upFile);
                                break;
                            }
                        }
                        break;
                    default:
                        logger.trace("功能尚在开发中....");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    @Override
    public void scanDirection(File path) {
        serverIP=bundle.getString("ServerIP");
        serverPort=Integer.parseInt(bundle.getString("ServerPort"));
        try (Socket socket=new Socket(serverIP, serverPort);
             InputStream netIn = socket.getInputStream();
             OutputStream netOut = socket.getOutputStream();
        ){

            String scan = AgreementUtil.getAgreement("SCAN", path.toString(), null, null);

            AgreementUtil.sendAgreement(netOut,scan);

            logger.trace("等待服务器返回消息");

            BufferedReader br = new BufferedReader(new InputStreamReader(netIn));

            String content;

            String fileLine = br.readLine();

            String status = AgreementUtil.getStatus(fileLine);

            if (status.equals("OK")){
                current= new File(AgreementUtil.getFileName(fileLine));
                System.out.println("当前目录："+current);
                while ((content=br.readLine())!=null){
                    System.out.println(content);
                }
            }else {
                logger.trace("浏览失败："+AgreementUtil.getMessage(fileLine));
            }


        }catch (IOException e){
            e.printStackTrace();
        }

    }

    @Override
    public void downloadFile(File downFile) {
        System.out.println("文件下载");
        //从配置文件获取IP和端口号
        serverIP=bundle.getString("ServerIP");
        serverPort=Integer.parseInt(bundle.getString("ServerPort"));


        try (Socket socket=new Socket(serverIP, serverPort);
             //连接服务器，获取输入输出流
             InputStream netIn = socket.getInputStream();
             OutputStream netOut = socket.getOutputStream();
        ){
            String download = AgreementUtil.getAgreement("DOWNLOAD", downFile.getName(), null, null);

            AgreementUtil.sendAgreement(netOut,download);

            System.out.println("--------------等待服务器返回消息-------------");

            BufferedReader br = new BufferedReader(new InputStreamReader(netIn));
            String firstLine = br.readLine();
            String status = AgreementUtil.getStatus(firstLine);

            if (status.equals("OK")){
                File file = new File(downloadPath, downFile.getName());
                IOUtils.copy(netIn,new FileOutputStream(file));

                String s = AgreementUtil.getAgreement("UPLOAD", downFile.getName(), "OK", downFile.getName() + "文件下载成功！");
                AgreementUtil.sendAgreement(netOut,s);
            }else {
                System.out.println("下载失败:"+AgreementUtil.getMessage(firstLine));
            }
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    @Override
    public void uploadFile(File upFile) {
        System.out.println("文件上传");
        serverIP=bundle.getString("ServerIP");
        serverPort=Integer.parseInt(bundle.getString("ServerPort"));
        try (Socket socket=new Socket(serverIP, serverPort);
             InputStream netIn = socket.getInputStream();
             OutputStream netOut = socket.getOutputStream();
        ){
            String upload = AgreementUtil.getAgreement("UPLOAD", upFile.getName(), null, null);

            AgreementUtil.sendAgreement(netOut,upload);

            System.out.println("--------------等待服务器返回消息-------------");

            BufferedReader br = new BufferedReader(new InputStreamReader(netIn));
            String firstLine = br.readLine();
            String status = AgreementUtil.getStatus(firstLine);

            if (status.equals("OK")){
                IOUtils.copy(new FileInputStream(upFile),netOut);
                socket.shutdownOutput();

                br = new BufferedReader(new InputStreamReader(netIn));
                firstLine = br.readLine();
                System.out.println(AgreementUtil.getMessage(firstLine));
            }else {
                System.out.println("上传失败:"+AgreementUtil.getMessage(firstLine));
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
