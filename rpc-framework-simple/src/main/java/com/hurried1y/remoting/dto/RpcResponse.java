package com.hurried1y.remoting.dto;

import com.hurried1y.enums.RpcResponseCodeEnum;
import com.hurried1y.remoting.constants.RpcResponseCode;
import lombok.*;

import java.io.Serializable;

/**
 * User：Hurried1y
 * Date：2023/4/19
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcResponse<T> implements Serializable {
    private static final long serialVersionUID = 6672133783386466359L;

    private String requestId;
    //response code
    private Integer code;
    //response message
    private String message;
    //response body
    private T data;

    public static <T> RpcResponse<T> success(T data, String requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(RpcResponseCode.SUCCESS.getCode());
        response.setMessage(RpcResponseCode.SUCCESS.getMessage());
        response.setRequestId(requestId);
        if(data != null) {
            response.setData(data);
        }
        return response;
    }

    public static <T> RpcResponse<T> fail(RpcResponseCodeEnum rpcResponseCodeEnum) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(rpcResponseCodeEnum.getCode());
        response.setMessage(rpcResponseCodeEnum.getMessage());
        return response;
    }
}


