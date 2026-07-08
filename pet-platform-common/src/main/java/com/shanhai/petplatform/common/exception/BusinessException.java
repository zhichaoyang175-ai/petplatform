package com.shanhai.petplatform.common.exception;

import lombok.Getter;

/**
 * 业务异常 — 所有业务层异常均抛出此异常或其子类，
 * 由 GlobalExceptionHandler 统一捕获并转换为前端响应格式。
 *
 * @author PetPlatform Team
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 错误码 */
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

}
