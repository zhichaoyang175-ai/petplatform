package com.shanhai.petplatform.common.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 登录响应 VO
 *
 * @author PetPlatform Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    /** 用户ID */
    private Long userId;

    /** 访问令牌 */
    private String token;

    /** 刷新令牌 */
    private String refreshToken;

    /** 角色 */
    private Integer role;

    /** 昵称 */
    private String nickname;

    /** 头像URL */
    private String avatarUrl;

    /** 快速构建 */
    public static LoginVO of(Long userId, String token, String refreshToken,
                             Integer role, String nickname, String avatarUrl) {
        LoginVO vo = new LoginVO();
        vo.setUserId(userId);
        vo.setToken(token);
        vo.setRefreshToken(refreshToken);
        vo.setRole(role);
        vo.setNickname(nickname);
        vo.setAvatarUrl(avatarUrl);
        return vo;
    }

}
