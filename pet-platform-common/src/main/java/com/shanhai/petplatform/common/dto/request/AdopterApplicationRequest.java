package com.shanhai.petplatform.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 送养人认证申请入参
 *
 * @author PetPlatform Team
 */
@Data
public class AdopterApplicationRequest {

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @NotBlank(message = "身份证号不能为空")
    private String idCard;

    @NotBlank(message = "联系电话不能为空")
    private String phone;

    private String email;

    /** 申请理由 */
    private String reason;
}
