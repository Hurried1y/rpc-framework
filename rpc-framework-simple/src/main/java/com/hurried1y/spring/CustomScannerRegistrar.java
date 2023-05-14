package com.hurried1y.spring;

import com.hurried1y.annotation.RpcScan;
import com.hurried1y.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.stereotype.Component;

/**
 * User：Hurried1y
 * Date：2023/4/20
 */
@Slf4j
public class CustomScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {
    //扫描框架项目的bean
    private static final String SPRING_BEAN_BASE_PACKAGE = "com.hurried1y";
    private static final String BASE_PACKAGE_ATTRIBUTE_NAME = "basePackage";
    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry, BeanNameGenerator importBeanNameGenerator) {
        //获取注解属性
        final AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(RpcScan.class.getName()));
        //获取注解属性中的basePackage
        String[] rpcScanBasePackages = new String[0];
        if(annotationAttributes != null){
            rpcScanBasePackages = annotationAttributes.getStringArray(BASE_PACKAGE_ATTRIBUTE_NAME);
        }
        //如果没有指定扫描包，则默认扫描当前类所在的包
        if(rpcScanBasePackages.length == 0){
            rpcScanBasePackages = new String[]{((StandardAnnotationMetadata) importingClassMetadata).getIntrospectedClass().getPackage().getName()};
        }
        //扫描 RpcService 注解的扫描器
        final CustomScanner rpcServiceScanner = new CustomScanner(registry, RpcService.class);
        //扫描 Component 注解的扫描器
        final CustomScanner componentScanner = new CustomScanner(registry, Component.class);
        //设置扫描包
        if(resourceLoader != null){
            rpcServiceScanner.setResourceLoader(resourceLoader);
            componentScanner.setResourceLoader(resourceLoader);
        }

        int springBeanAmount = componentScanner.scan(SPRING_BEAN_BASE_PACKAGE);
        log.info("springBeanScanner扫描的数量 [{}]", springBeanAmount);
        //扫描 RpcService 注解
        int rpcServiceCount = rpcServiceScanner.scan(rpcScanBasePackages);
        log.info("rpcServiceScanner扫描的数量 [{}]", rpcServiceCount);
    }

}
