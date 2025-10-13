package com.master.meta.handle.exception;

import com.master.meta.handle.result.IResultCode;
import lombok.Getter;

/**
 * @author Created by 11's papa on 2025/10/11
 */
@Getter
public class CustomException extends RuntimeException {
    protected IResultCode errorCode;

    public CustomException(String message) {
        super(message);
    }

    public CustomException(Throwable t) {
        super(t);
    }

    public CustomException(IResultCode errorCode) {
        super("");
        this.errorCode = errorCode;
    }

    public CustomException(IResultCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public CustomException(IResultCode errorCode, Throwable t) {
        super(t);
        this.errorCode = errorCode;
    }

    public CustomException(String message, Throwable t) {
        super(message, t);
    }

}
