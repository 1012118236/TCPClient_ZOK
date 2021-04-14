package com.tcp.client;

import com.tcp.client.config.ZookeeperConf;
import com.tcp.client.utils.TCPUtil;
import com.tcp.client.zookeeper.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TcpClient {

    private static final Logger log = LoggerFactory.getLogger(TcpClient.class);

    public static void main(String[] args) throws IOException {
        ServiceDiscovery serviceDiscovery = new ServiceDiscovery(ZookeeperConf.discoveryAddress);
        String serverAddress = serviceDiscovery.discover();
        if (serverAddress!=null) {
            String[] array = serverAddress.split(":");
            if (array!=null && array.length==2) {
                String host = array[0];
                String port = array[1];
                log.info(" netty server path is {}",serverAddress);
                TCPUtil.sendTCPRequest(host,port,"我是tcpclient","UTF-8");

            }
        }

    }



}
