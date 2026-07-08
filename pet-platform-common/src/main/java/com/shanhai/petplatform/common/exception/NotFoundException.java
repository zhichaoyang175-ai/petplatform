package com.shanhai.petplatform.common.exception;

/**
 * 资源不存在异常 — HTTP 404
 * <p>
 * 触发场景：宠物、申请、用户等资源 ID 查不到
 *
 * @author PetPlatform Team
 */
public class NotFoundException extends BusinessException {

    public NotFoundException(String message) {
        super(404, message);
    }

    public NotFoundException() {
        super(404, "请求的资源不存在");
    }

}
