package com.shanhai.petplatform.common.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求
 *
 * @author PetPlatform Team
 */
@Data
public class RegisterRequest {

    /** 手机号 */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度需在6-20位之间")
    private String password;

    /** 昵称 */
    @NotBlank(message = "昵称不能为空")
    @Size(min = 2, max = 20, message = "昵称长度需在2-20位之间")
    private String nickname;

    /** 角色: 1-领养人 2-送养人（服务端会二次校验，拒绝越权角色） */
    @NotNull(message = "角色不能为空")
    @Min(value = 1, message = "角色非法")
    @Max(value = 2, message = "角色非法")
    private Integer role;

}
