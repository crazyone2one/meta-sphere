package com.master.meta.handle.exception;

/**
 * @author Created by 11's papa on 2025/10/11
 */
public class RefreshTokenExpiredException extends RuntimeException {
    public RefreshTokenExpiredException(String message) {
        super(message);
    }
}
