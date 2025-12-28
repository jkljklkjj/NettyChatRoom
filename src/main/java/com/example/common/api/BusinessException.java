package com.example.common.api;

public class BusinessException extends RuntimeException {
    private final StatusCode errorCode;

    public BusinessException(StatusCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(StatusCode errorCode, String detail) {
        super(detail);
        this.errorCode = errorCode;
    }

    public StatusCode getErrorCode() { return errorCode; }
}

