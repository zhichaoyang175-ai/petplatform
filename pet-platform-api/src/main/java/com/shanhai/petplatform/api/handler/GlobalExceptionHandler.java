package com.shanhai.petplatform.api.handler;

import com.shanhai.petplatform.common.exception.BusinessException;
import com.shanhai.petplatform.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * 全局异常处理器 — 统一拦截所有 Controller 抛出的异常，
 * 转换为前端可解析的 R 响应格式。
 *
 * @author PetPlatform Team
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常 — 返回异常中携带的 code + message
     */
    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    /**
     * 请求体参数校验失败 (@Valid / @Validated)
     * 返回 400 + 第一条校验错误信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String msg = extractFirstError(e.getBindingResult().getAllErrors());
        log.warn("参数校验失败: {}", msg);
        return R.fail(400, msg);
    }

    /**
     * 表单参数绑定异常 (GET 请求参数校验)
     * 返回 400 + 参数绑定错误
     */
    @ExceptionHandler(BindException.class)
    public R<Void> handleBindException(BindException e) {
        String msg = extractFirstError(e.getAllErrors());
        log.warn("参数绑定失败: {}", msg);
        return R.fail(400, msg);
    }

    /**
     * 权限不足 — Spring Security 抛出
     * 返回 403
     */
    @ExceptionHandler(AccessDeniedException.class)
    public R<Void> handleAccessDenied(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return R.fail(403, "您没有权限执行此操作");
    }

    /**
     * 兜底异常 — 所有未捕获的运行时异常
     * 返回 500，记录完整堆栈
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("系统内部错误", e);
        return R.fail(500, "系统内部错误，请联系管理员");
    }

    // ────────────────── 辅助方法 ──────────────────

    /**
     * 从校验错误列表中提取第一条可读信息
     */
    private String extractFirstError(List<ObjectError> errors) {
        if (errors.isEmpty()) {
            return "参数校验失败";
        }
        ObjectError error = errors.get(0);
        if (error instanceof FieldError fieldError) {
            return fieldError.getField() + ": " + fieldError.getDefaultMessage();
        }
        return error.getDefaultMessage();
    }

}
