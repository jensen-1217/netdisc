package com.jensen.util;


import java.io.*;

/*
协议
格式："Type=SCAN,FileName=root,Status=OK,Message=信息"
 */
public class AgreementUtil {
    //操作类型
    public static String getTpye(String agreement){
        String[] strings=agreement.split(",");
        return strings[0].split("=")[1];
    }

    // 解析协议 : 文件名字
    public static String getFileName(String agreement) {
        String[] strings = agreement.split(",");
        return strings[1].split("=")[1];
    }

    // 解析协议 : 文件状态
    public static String getStatus(String agreement) {
        String[] strings = agreement.split(",");
        return strings[2].split("=")[1];
    }

    // 解析协议 : 状态信息
    public static String getMessage(String agreement) {
        String[] strings = agreement.split(",");
        return strings[3].split("=")[1];
    }

    //协议封装
    public static String getAgreement(String type,String fileName,String status,String mesage){
        StringBuilder sb=new StringBuilder();
        sb.append("TPYE").append("=").append(type).append(",");
        sb.append("fileName").append("=").append(fileName).append(",");
        sb.append("Status").append("=").append(status).append(",");
        sb.append("Message").append("=").append(mesage).append(",");
        return sb.toString();
    }

    //协议发送
    public static void sendAgreement(OutputStream netOut,String agreement)throws IOException{
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(netOut));
        bw.write(agreement);
        bw.newLine();
        bw.flush();
    }

    //协议接收
    public static String receiveAgreement(InputStream netIn) throws IOException{
        BufferedReader br = new BufferedReader(new InputStreamReader(netIn));
        String agreementContent = br.readLine();
        return agreementContent;
    }


}
