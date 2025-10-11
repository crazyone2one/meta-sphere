package com.master.meta.handle.result;

import com.master.meta.handle.exception.RefreshTokenExpiredException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Created by 11's papa on 2025/10/11
 */
@Slf4j
@RestControllerAdvice
public class RestControllerExceptionHandler {
    /**
     * 处理方法参数验证异常的处理器
     * 当控制器方法的参数验证失败时，此方法会捕获MethodArgumentNotValidException异常并进行处理
     *
     * @param ex 方法参数验证异常对象，包含验证失败的详细信息
     * @return 返回封装了验证错误信息的ResultHolder对象，包含错误码、错误消息和具体的字段错误信息
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultHolder handleValidationExceptions(MethodArgumentNotValidException ex) {
        // 提取所有验证错误信息，构建字段名到错误消息的映射
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        // 返回验证失败的统一响应结果
        return ResultHolder.error(ResultCode.VALIDATE_FAILED.getCode(), ResultCode.VALIDATE_FAILED.getMessage(), errors);
    }

    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<ResultHolder> handleRefreshTokenExpired(RefreshTokenExpiredException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ResultHolder.error(100401, ex.getMessage(), getStackTraceAsString(ex)));
    }

    // 添加对ExpiredJwtException的专门处理
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ResultHolder> handleExpiredJwtException(ExpiredJwtException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ResultHolder.error(100401, "Token has expired", getStackTraceAsString(ex)));
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ResultHolder> handleJwtException(JwtException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ResultHolder.error(100401, ex.getMessage(), getStackTraceAsString(ex)));
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ResultHolder> handleException(Exception e) {
        log.error("系统异常:", e);
        return ResponseEntity.internalServerError()
                .body(ResultHolder.error(ResultCode.FAILED.getCode(),
                        e.getMessage(), getStackTraceAsString(e)));
    }

    public static String getStackTraceAsString(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw, true));
        return sw.toString();
    }
}
