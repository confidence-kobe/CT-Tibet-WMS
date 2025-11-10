package com.ct.wms.security;

import com.alibaba.fastjson2.JSON;
import com.ct.wms.common.api.Result;
import com.ct.wms.common.api.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT认证入口点 - 处理未认证请求
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        log.error("Unauthorized error: {}", authException.getMessage());

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Result<Object> result = Result.error(ResultCode.UNAUTHORIZED, "未授权，请先登录");

        response.getWriter().write(JSON.toJSONString(result));
    }
}
