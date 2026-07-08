package com.shanhai.petplatform.common.exception;

/**
 * 无权限异常 — HTTP 403
 * <p>
 * 触发场景：角色权限不足（如领养人尝试调用送养人专属接口）
 *
 * @author PetPlatform Team
 */
public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super(403, message);
    }

    public ForbiddenException() {
        super(403, "您没有权限执行此操作");
    }

}
