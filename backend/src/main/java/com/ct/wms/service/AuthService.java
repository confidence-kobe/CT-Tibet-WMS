package com.ct.wms.service;

import com.ct.wms.dto.LoginRequest;
import com.ct.wms.vo.LoginVO;

/**
 * 认证Service接口
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    LoginVO login(LoginRequest request);

    /**
     * 退出登录
     */
    void logout();

    /**
     * 刷新Token
     *
     * @param oldToken 旧Token
     * @return 新Token
     */
    String refreshToken(String oldToken);
}
