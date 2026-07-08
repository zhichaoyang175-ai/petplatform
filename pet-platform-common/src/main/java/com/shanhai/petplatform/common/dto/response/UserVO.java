package com.shanhai.petplatform.common.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shanhai.petplatform.common.enums.UserRoleEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户信息 VO
 *
 * @author PetPlatform Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {

    /** 用户ID */
    private Long id;

    /** 手机号（脱敏） */
    private String phone;

    /** 昵称 */
    private String nickname;

    /** 头像URL */
    private String avatarUrl;

    /** 邮箱 */
    private String email;

    /** 角色 code */
    private Integer role;

    /** 角色名称 */
    private String roleName;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 快速构建 VO（后续用 MapStruct 替代） */
    public static UserVO of(Long id, String phone, String nickname, String avatarUrl,
                            String email, Integer role, LocalDateTime createdAt) {
        UserVO vo = new UserVO();
        vo.setId(id);
        vo.setPhone(phone);
        vo.setNickname(nickname);
        vo.setAvatarUrl(avatarUrl);
        vo.setEmail(email);
        vo.setRole(role);
        vo.setRoleName(UserRoleEnum.fromCode(role).getDesc());
        vo.setCreatedAt(createdAt);
        return vo;
    }

}
