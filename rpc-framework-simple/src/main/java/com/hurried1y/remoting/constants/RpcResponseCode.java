package com.hurried1y.remoting.constants;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * User：Hurried1y
 * Date：2023/4/19
 */
@AllArgsConstructor
public enum RpcResponseCode {
    SUCCESS(200, "rpc request success");

    private final int code;
    private final String message;

    public int getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }
}
