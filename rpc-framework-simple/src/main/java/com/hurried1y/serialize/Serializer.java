package com.hurried1y.serialize;

import com.hurried1y.extension.SPI;

/**
 * User：Hurried1y
 * Date：2023/4/26
 * 序列化接口
 */
@SPI
public interface Serializer {
    /**
     * 序列化
     * @param object 要序列化的对象
     * @return 字节数组
     */
    byte[] serialize(Object object);

    /**
     * 反序列化
     * @param bytes 序列化后的字节数组
     * @param clazz 目标类
     * @param <T> 类的类型。举个例子,  {@code String.classM} 的类型是 {@code Class<String>}.
     * @return 反序列化的对象
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
