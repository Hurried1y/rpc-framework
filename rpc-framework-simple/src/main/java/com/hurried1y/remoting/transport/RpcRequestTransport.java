package com.hurried1y.remoting.transport;

import com.hurried1y.remoting.dto.RpcRequest;

/**
 * User：Hurried1y
 * Date：2023/4/19
 */

public interface RpcRequestTransport {
    Object sendRpcRequest(RpcRequest rpcRequest);
}
