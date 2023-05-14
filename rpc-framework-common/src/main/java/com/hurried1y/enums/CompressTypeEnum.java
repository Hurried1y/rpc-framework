package com.hurried1y.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * User：Hurried1y
 * Date：2023/4/26
 */
@AllArgsConstructor
@Getter
public enum CompressTypeEnum {

    GZIP((byte) 0x01, "gzip");

    private final byte code;
    private final String name;

    public static String getName(byte code) {
        for (CompressTypeEnum c : CompressTypeEnum.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }

}
