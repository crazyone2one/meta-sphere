package com.master.meta.handle.annotation;

import java.lang.annotation.*;

/**
 * @author Created by 11's papa on 2025/10/11
 */
@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NoResultHolder {
}
