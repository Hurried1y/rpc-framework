package com.hurried1y.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * User：Hurried1y
 * Date：2023/5/12
 * 属性配置加载类
 */
public class PropertiesLoader {
    private static Properties properties;
    private static Map<String, String> propertiesMap = new HashMap<>();
    //缓存配置文件名称
    private static final String DEFAULT_PROPERTIES_FILE = "rpc.properties";

    public static void loadConfiguration() throws IOException {
        if (properties != null){
            return;
        }
        properties = new Properties();
        InputStream in = PropertiesLoader.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE);
        properties.load(in);
    }

    /**
     * 根据键值获取配置属性
     * @param key 键
     * @return 值
     */
    public static String getPropertiesStr(String key){
        if (properties == null){
            return null;
        }
        if (key == null){
            return null;
        }
        if (!propertiesMap.containsKey(key)){
            String value = properties.getProperty(key);
            propertiesMap.put(key, value);
        }
        return propertiesMap.get(key) == null ? null : String.valueOf(propertiesMap.get(key));
    }

    /**
     * 根据键值获取配置属性，如果为空则抛出异常
     * @param key 键
     * @return 值
     */
    public static String getPropertiesNotBlank(String key){
        String value = getPropertiesStr(key);
        if (value == null || "".equals(value)){
            throw new IllegalArgumentException();
        }
        return value;
    }

    /**
     * 根据键值获取配置属性，如果为空则返回默认值
     * @param key 键
     * @param defaultVal 默认值
     * @return 值
     */
    public static String getPropertiesStrDefault(String key, String defaultVal){
        String value = getPropertiesStr(key);
        return value == null || "".equals(value) ? defaultVal : value;
    }

    /**
     * 根据键值获取配置INT属性
     * @param key
     * @return
     */
    public static Integer getPropertiesInteger(String key){
        if (properties == null) {
            return null;
        }
        if(key == null || "".equals(key)){
            return null;
        }
        if (!propertiesMap.containsKey(key)) {
            String value = properties.getProperty(key);
            propertiesMap.put(key, value);
        }
        return Integer.valueOf(propertiesMap.get(key));
    }

    /**
     * 根据键值获取配置属性
     *
     * @param key
     * @return
     */
    public static Integer getPropertiesIntegerDefault(String key,Integer defaultVal) {
        if (properties == null) {
            return defaultVal;
        }
        if(key == null || "".equals(key)){
            return defaultVal;
        }
        String value = properties.getProperty(key);
        if(value==null){
            propertiesMap.put(key, String.valueOf(defaultVal));
            return defaultVal;
        }
        if (!propertiesMap.containsKey(key)) {
            propertiesMap.put(key, value);
        }
        return Integer.valueOf(propertiesMap.get(key));
    }
}
