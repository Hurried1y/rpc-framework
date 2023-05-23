# rpc-framework

#### 介绍

基于Netty的RPC框架，实现分布式下两台服务器之间的远程过程调用。                                                                                                          
更多框架细节可以转到我的个人博客：https://hurried1y.github.io/

#### 软件架构

1.  服务注册                                            
    以Zookeeper作为服务的注册中心，实现服务端与客户端的关系解耦
2.  服务发现                                           
3.  负载均衡                                       
4.  服务提供                                       


#### 安装教程

1.  引入rpc-framework-common
2.  引入rpc-framework-simple

#### 使用说明

1.  在启动类标注@RpcScan
2.  按需标注@RpcService、@RpcReference
