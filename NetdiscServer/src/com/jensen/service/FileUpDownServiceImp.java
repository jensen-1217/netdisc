package com.jensen.service;

import com.jensen.exception.BusinessException;
import com.jensen.util.AgreementUtil;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.Socket;
import java.util.ResourceBundle;

public class FileUpDownServiceImp implements FileUpDownService,Runnable{

    private final ResourceBundle bundle;
    private final File rootDir;
    private Socket socket;

    public FileUpDownServiceImp(Socket socket) {
        this.socket = socket;
        //读取配置文件
        bundle=ResourceBundle.getBundle("config");
        //读取根目录路径
        rootDir=new File(bundle.getString("rootDir"));
        if(rootDir.isFile()){
            throw new BusinessException("根目录路径与已存在文件冲突");
        }else if (!rootDir.exists()&&!rootDir.mkdirs()){
            throw new BusinessException("根目录创建失败，请检查路径是否正确");
        }
    }

    @Override
    public void run() {

        try (Socket socket=this.socket;
            InputStream netIn = socket.getInputStream();
            OutputStream netOut = socket.getOutputStream();
        ){
            //读协议
          final  String agreement = AgreementUtil.receiveAgreement(netIn);

          //解析字符串
            String type = AgreementUtil.getTpye(agreement);

            switch (type){
                case "SCAN":
                    scanDirectory(agreement,netIn,netOut);
                    break;
                case "DOWNLOAD":
                    downloadFile(agreement,netIn,netOut);
                    break;
                case "UPLOAD":
                    uploadFile(agreement,netIn,netOut);
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    //上传文件
    @Override
    public void uploadFile(String agreement, InputStream netIn, OutputStream netOut) throws IOException {
        System.out.println("文件上传");
        String fileName = AgreementUtil.getFileName(agreement);
        File file = new File(rootDir, fileName);
        if (file.exists()){

            String s = AgreementUtil.getAgreement("UPLOAD", fileName, "FAILED", fileName + "文件已存在！");
            AgreementUtil.sendAgreement(netOut,s);
        }else {
            String s = AgreementUtil.getAgreement("UPLOAD", fileName, "OK", null);
            AgreementUtil.sendAgreement(netOut,s);

            IOUtils.copy(netIn,new FileOutputStream(file));
            s = AgreementUtil.getAgreement("UPLOAD", fileName, "OK", fileName + "文件上传成功！");
            AgreementUtil.sendAgreement(netOut,s);
        }
    }


    //下载文件
    @Override
    public void downloadFile(String agreement, InputStream netIn, OutputStream netOut) throws IOException {
        System.out.println("文件下载");
        String fileName = AgreementUtil.getFileName(agreement);
        System.out.println("fileName="+fileName);
        File dir = new File(rootDir,fileName);
        System.out.println("dir="+dir);
        if (dir.exists()){
           // System.out.println(dir.exists());
            String s = AgreementUtil.getAgreement("DOWNLOAD", fileName, "OK", null);
            AgreementUtil.sendAgreement(netOut,s);
            IOUtils.copy(new FileInputStream(dir),netOut);
            socket.shutdownOutput();

            BufferedReader br = new BufferedReader(new InputStreamReader(netIn));
            String firstLine = br.readLine();
            System.out.println(AgreementUtil.getMessage(firstLine));

        }else {
            String s = AgreementUtil.getAgreement("DOWNLOAD", null, "FAILED", "文件不存在，无法下载！");
            AgreementUtil.sendAgreement(netOut,s);
        }
    }


    //浏览目录
    @Override
    public void scanDirectory(String agreement, InputStream netIN, OutputStream netOut) throws IOException {
        //获取客户端要浏览的目录
        String fileName = AgreementUtil.getFileName(agreement);
        //将root转换为真是的服务器路径
        String fileDir = fileName.replace("root", rootDir.toString());

        File dir = new File(fileDir);

        if (dir.isFile()){
            String s = AgreementUtil.getAgreement("SCAN", null, "FAILED", "目录不存在，只能浏览当前子目录");
            AgreementUtil.sendAgreement(netOut,s);
        }else {
           // System.out.println(fileName);
            String s = AgreementUtil.getAgreement("SCAN", fileName, "OK", null);
            AgreementUtil.sendAgreement(netOut,s);

            OutputStreamWriter osw = new OutputStreamWriter(netOut);
            File[] children = dir.listFiles();
            for (File child : children) {
                String fileType = child.isFile() ? "文件" : "目录";
                osw.write(fileType+"   "+child.getName()+"\r\n");
            }
            osw.flush();
        }

    }

}
