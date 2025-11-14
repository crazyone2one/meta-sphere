package com.master.meta.handle.annotation;

import com.master.meta.constants.Logical;
import org.springframework.lang.NonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Created by 11's papa on 2025/11/4
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermissions {
    @NonNull
    String[] value();

    Logical logical() default Logical.AND;
}
