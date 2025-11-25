package com.master.meta.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class EnumValidator {
    public static <E extends Enum<E>> E validateEnum(Class<E> enumClass, String value) {
        if (StringUtils.isBlank(value)) {
            log.error("Invalid value for enum {}: {}", enumClass.getSimpleName(), value);
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            log.error("Invalid value for enum {}: {}", enumClass.getSimpleName(), value, e);
            return null;
        }
    }
}
