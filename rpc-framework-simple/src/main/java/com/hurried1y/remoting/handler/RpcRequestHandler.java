package com.hurried1y.remoting.handler;

import com.hurried1y.provider.impl.ZkServiceProviderImpl;
import com.hurried1y.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * User：Hurried1y
 * Date：2023/4/20
 */
@Slf4j
public class RpcRequestHandler {
    //TODO 通过配置文件选择不同的ServiceProvider
    private final ZkServiceProviderImpl serviceProvider;

    public RpcRequestHandler(){
        serviceProvider = new ZkServiceProviderImpl();
    }

    /**
     * 处理rpc请求
     * @param rpcRequest rpc请求
     * @return 执行结果
     */
    public Object handle(RpcRequest rpcRequest){
        Object service = serviceProvider.getService(rpcRequest.getRpcServiceName());
        return invokeTargetMethod(rpcRequest, service);
    }

    /**
     * 获取目标方法并执行，返回执行结果
     * @param rpcRequest rpc请求
     * @param service 目标服务
     * @return 执行结果
     */
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result = null;
        try {
            //通过反射获取目标方法
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            //执行目标方法
            result = method.invoke(service, rpcRequest.getParameters());
            log.info("服务执行 - 服务: [{}] 成功地执行了接口: [{}]", rpcRequest.getRpcServiceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return result;
    }
}