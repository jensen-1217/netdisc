package com.jensen.main;

import com.jensen.service.NetdiscService;
import com.jensen.service.NetdiscServiceImp;

public class ServerApp {
    public static void main(String[] args) {
        NetdiscService netdisc= new NetdiscServiceImp();
        netdisc.start();
    }
}
