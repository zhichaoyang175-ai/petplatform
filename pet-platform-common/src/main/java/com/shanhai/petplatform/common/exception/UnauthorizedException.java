package com.shanhai.petplatform.common.exception;

/**
 * 未授权异常 — HTTP 401
 * <p>
 * 触发场景：未登录、token 过期/无效
 *
 * @author PetPlatform Team
 */
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super(401, message);
    }

    public UnauthorizedException() {
        super(401, "请先登录");
    }

}
