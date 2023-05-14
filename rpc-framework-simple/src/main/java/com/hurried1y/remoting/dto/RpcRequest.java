package com.hurried1y.remoting.dto;

import lombok.*;

import java.io.Serializable;
import java.net.InetSocketAddress;

/**
 * User：Hurried1y
 * Date：2023/4/19
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 6672133783386466359L;

    private String requestId;
    private String interfaceName;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] paramTypes;
    private String version;
    private String group;


    public String getRpcServiceName() {
        return getInterfaceName() + getGroup() + getVersion();
    }
}

