package com.shanhai.petplatform.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * 统一响应类 — 所有 Controller 返回体必须包装为 R<T>
 *
 * @param <T> 响应数据类型
 * @author PetPlatform Team
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class R<T> {

    /** 状态码 — 200 成功，其他为错误码 */
    private int code;

    /** 提示消息 */
    private String message;

    /** 响应数据 */
    private T data;

    private R() {}

    private R(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ────────────────── 成功 ──────────────────

    /** 成功 — 无数据 */
    public static <T> R<T> ok() {
        return new R<>(200, "success", null);
    }

    /** 成功 — 有数据 */
    public static <T> R<T> ok(T data) {
        return new R<>(200, "success", data);
    }

    /** 成功 — 自定义消息 */
    public static <T> R<T> ok(String message, T data) {
        return new R<>(200, message, data);
    }

    // ────────────────── 失败 ──────────────────

    /** 失败 — 指定错误码和消息 */
    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, message, null);
    }

    /** 失败 — 默认 500 错误 */
    public static <T> R<T> fail(String message) {
        return new R<>(500, message, null);
    }

    /** 失败 — 指定错误码、消息和数据 */
    public static <T> R<T> fail(int code, String message, T data) {
        return new R<>(code, message, data);
    }

}
