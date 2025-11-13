package com.ct.wms.controller;

import com.ct.wms.common.api.Result;
import com.ct.wms.dto.LoginRequest;
import com.ct.wms.service.AuthService;
import com.ct.wms.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.PermitAll;

/**
 * 认证授权Controller
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证授权", description = "用户登录、退出、刷新Token等接口")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "PC端或小程序用户登录")
    @PermitAll
    public Result<LoginVO> login(@Validated @RequestBody LoginRequest request) {
        log.info("用户登录: username={}, loginType={}", request.getUsername(), request.getLoginType());
        LoginVO loginVO = authService.login(request);
        return Result.success(loginVO, "登录成功");
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录", description = "清除用户登录状态")
    public Result<Void> logout() {
        log.info("用户退出登录");
        authService.logout();
        return Result.success("退出成功");
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "刷新Token", description = "使用旧Token刷新获取新Token")
    public Result<String> refreshToken(@RequestHeader("Authorization") String authorization) {
        String oldToken = authorization.substring(7); // 去掉"Bearer "前缀
        String newToken = authService.refreshToken(oldToken);
        return Result.success(newToken, "Token刷新成功");
    }
}
