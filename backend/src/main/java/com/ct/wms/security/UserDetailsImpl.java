package com.ct.wms.security;

import com.ct.wms.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security用户详情实现类
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    @JsonIgnore
    private String password;

    private String realName;

    private Long deptId;

    private Long roleId;

    private String roleCode;

    private Integer status;

    private Collection<? extends GrantedAuthority> authorities;

    /**
     * 从User实体构建UserDetailsImpl
     *
     * @param user     用户实体
     * @param roleCode 角色编码
     * @return UserDetailsImpl
     */
    public static UserDetailsImpl build(User user, String roleCode) {
        // 构建权限列表，使用角色编码
        Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + roleCode.toUpperCase())
        );

        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRealName(),
                user.getDeptId(),
                user.getRoleId(),
                roleCode,
                user.getStatus().getValue(),
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 0-启用 1-禁用
        return status == 0;
    }
}
