package com.hurried1y.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User：Hurried1y
 * Date：2023/5/12
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ServerConfig {
    private Integer serverPort;
    private String applicationName;
    private String registerAddr;
    private String registerType;


    /**
     * 服务端序列化方式 example: hession2,kryo,jdk,fastjson
     */
    private String serverSerialize;
}
