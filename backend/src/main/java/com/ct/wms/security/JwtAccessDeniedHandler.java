package com.ct.wms.security;

import com.alibaba.fastjson2.JSON;
import com.ct.wms.common.api.Result;
import com.ct.wms.common.api.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT访问拒绝处理器 - 处理无权限访问
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.error("Access denied error: {}", accessDeniedException.getMessage());

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        Result<Object> result = Result.error(ResultCode.FORBIDDEN, "无权限访问");

        response.getWriter().write(JSON.toJSONString(result));
    }
}
