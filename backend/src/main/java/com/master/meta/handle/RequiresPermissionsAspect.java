package com.master.meta.handle;

import com.master.meta.constants.Logical;
import com.master.meta.handle.annotation.RequiresPermissions;
import com.master.meta.handle.security.RestPermissionEvaluator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author Created by 11's papa on 2025/11/14
 */
@Aspect
@Component
public class RequiresPermissionsAspect {
    private final RestPermissionEvaluator permissionEvaluator;

    public RequiresPermissionsAspect(RestPermissionEvaluator permissionEvaluator) {
        this.permissionEvaluator = permissionEvaluator;
    }

    @Around("@annotation(com.master.meta.handle.annotation.RequiresPermissions)")
    public Object checkHasPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequiresPermissions annotation = method.getAnnotation(RequiresPermissions.class);
        Logical logical = annotation.logical();
        if (logical == Logical.OR) {
            if (!permissionEvaluator.hasAnyPermission(annotation.value())) {
                throw new AccessDeniedException("没有足够的权限执行此操作，需要以下任一权限: " + String.join(", ", annotation.value()));
            }
        } else {
            if (!permissionEvaluator.hasAllPermissions(annotation.value())) {
                throw new AccessDeniedException("没有足够的权限执行此操作，需要以下所有权限: " + String.join(", ", annotation.value()));
            }
        }
        return joinPoint.proceed();
    }
}
