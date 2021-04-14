package com.tcp.client.zookeeper;


import com.tcp.client.config.ZookeeperConf;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class ServiceDiscovery {
    private static final Logger log = LoggerFactory.getLogger(ServiceDiscovery.class);
    /**
     * zook 连接同步器
     */
    private CountDownLatch latch = new CountDownLatch(1);
    /**
     * service服务列表
     */
    private volatile List<String> serviceAddressList = new ArrayList<>();
    /**
     * zookeeper 注册中心的地址
     */
    private String registryAddress;

    /**
     *
     * @param registryAddress zookeeper 注册中心的地址
     */
    public ServiceDiscovery(String registryAddress) {
        this.registryAddress = registryAddress;
        ZooKeeper zk = connectServer();
        if (zk != null) {
            watchNode(zk);
        }
    }

    /**
     * 连接 zookeeper
     *
     * @return
     */
    private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, ZookeeperConf.ZK_SESSION_TIMEOUT, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                        log.info("[    zookeeper connect success! registryAddress {}   ]",registryAddress);
                        latch.countDown();
                    }
                }
            });
            latch.await();
        } catch (IOException | InterruptedException e) {
            log.error(" ServiceDiscovery.connectServer() error :  {}", e);
        }
        return zk;
    }

    /**
     * 获取服务地址列表
     *
     * @param zk
     */
    private void watchNode(final ZooKeeper zk) {

        try {
            //获取子节点列表
            List<String> nodeList = zk.getChildren(ZookeeperConf.ZK_REGISTRY_PATH, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeChildrenChanged) {
                        log.info(" zk  {} 服务节点更新，重新获取中....",ZookeeperConf.ZK_REGISTRY_PATH);
                        //发生子节点变化时再次调用此方法更新服务地址
                        watchNode(zk);
                    }
                }
            });

            List<String> dataList = nodeList.stream().map(a -> {
                try {
                    byte[] data = zk.getData(ZookeeperConf.ZK_REGISTRY_PATH + "/" + a, false, null);
                    return new String(data);
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return "";
            }).collect(Collectors.toList());
            log.debug("  zk  {}  node data: {}",ZookeeperConf.ZK_REGISTRY_PATH,dataList.stream().toArray(String[]::new));
            this.serviceAddressList = dataList;
        } catch (KeeperException | InterruptedException e) {
            log.error("{}",e);
        }
    }

    /**
     * 通过服务发现，获取服务提供方的地址
     *
     * @return
     */
    public String discover() {
        String data = null;
        int size = serviceAddressList.size();
        if (size > 0) {
            if (size == 1) {  //只有一个服务提供方
                data = serviceAddressList.get(0);
                log.info("unique service address : {}", data);
            } else {          //使用随机分配法。简单的负载均衡法
                data = serviceAddressList.get(ThreadLocalRandom.current().nextInt(size));
                log.info("choose an address : {}", data);
            }
        }
        return data;
    }




}
