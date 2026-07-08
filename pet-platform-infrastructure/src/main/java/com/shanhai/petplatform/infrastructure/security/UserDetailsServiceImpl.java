package com.shanhai.petplatform.infrastructure.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shanhai.petplatform.repository.entity.User;
import com.shanhai.petplatform.repository.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Security 用户详情加载服务
 *
 * @author PetPlatform Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // username 即手机号
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, username));

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        if (user.getStatus() == 0) {
            throw new UsernameNotFoundException("账号已被禁用: " + username);
        }

        List<SimpleGrantedAuthority> authorities = buildAuthorities(user.getRole());

        return org.springframework.security.core.userdetails.User.builder()
                .username(String.valueOf(user.getId()))
                .password(user.getPassword())
                .authorities(authorities)
                .disabled(user.getStatus() == 0)
                .build();
    }

    /**
     * 根据 role 构建权限
     */
    private List<SimpleGrantedAuthority> buildAuthorities(Integer role) {
        String roleName = switch (role) {
            case 2 -> "ROLE_ADOPTER";
            case 3 -> "ROLE_ADMIN";
            default -> "ROLE_APPLICANT";
        };
        return List.of(new SimpleGrantedAuthority(roleName));
    }

}
