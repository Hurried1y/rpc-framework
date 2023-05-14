package com.hurried1y.config;


import java.io.IOException;

/**
 * User：Hurried1y
 * Date：2023/5/12
 * 属性引导程序
 */
public class PropertiesBootstrap {
    private final static String SERVER_PORT = "rpc.serverPort";
    private final static String REGISTRY_ADDRESS = "rpc.registryAddress";
    public static final String REGISTER_TYPE = "rpc.registerType";
    public static final String APPLICATION_NAME = "rpc.applicationName";
    public static final String PROXY_TYPE = "rpc.proxyType";
    public static final String ROUTER_TYPE = "rpc.router";
    public static final String SERVER_SERIALIZE_TYPE = "rpc.serverSerialize";
    public static final String CLIENT_SERIALIZE_TYPE = "rpc.clientSerialize";


    public static ServerConfig loadServerConfigFromLocal(){
        try {
            PropertiesLoader.loadConfiguration();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setServerPort(PropertiesLoader.getPropertiesInteger(SERVER_PORT));
        serverConfig.setApplicationName(PropertiesLoader.getPropertiesStr(APPLICATION_NAME));
        serverConfig.setRegisterAddr(PropertiesLoader.getPropertiesStr(REGISTRY_ADDRESS));
        serverConfig.setRegisterType(PropertiesLoader.getPropertiesStr(REGISTER_TYPE));
        serverConfig.setServerSerialize(PropertiesLoader.getPropertiesStr(SERVER_SERIALIZE_TYPE));
        return serverConfig;
    }

    public static ClientConfig loadClientConfigFromLocal(){
        try {
            PropertiesLoader.loadConfiguration();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setApplicationName(PropertiesLoader.getPropertiesNotBlank(APPLICATION_NAME));
        clientConfig.setRegisterAddr(PropertiesLoader.getPropertiesNotBlank(REGISTRY_ADDRESS));
        clientConfig.setRegisterType(PropertiesLoader.getPropertiesNotBlank(REGISTER_TYPE));
        return clientConfig;
    }
}
