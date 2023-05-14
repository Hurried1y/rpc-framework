package com.hurried1y.annotation;

import java.lang.annotation.*;

/**
 * User：Hurried1y
 * Date：2023/4/20
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface RpcReference {
    /**
     * Service version, default value is empty string
     */
    String version() default "";

    /**
     * Service group, default value is empty string
     */
    String group() default "";
}
