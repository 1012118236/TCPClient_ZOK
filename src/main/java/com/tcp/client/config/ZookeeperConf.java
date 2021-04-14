package com.tcp.client.config;

/**
 * zookeeper 配置信息
 */
public class ZookeeperConf {
    /**
     * 主节点路径
     */
    public static final String ZK_REGISTRY_PATH = "/tmp_root_path";
    /**
     * 会话延时
     */
    public static final int ZK_SESSION_TIMEOUT = 20000 ;

    /**
     * 注册中心地址
     */
    public static final String discoveryAddress = "192.168.138.128:2181"; // 注册中心的地址

}
