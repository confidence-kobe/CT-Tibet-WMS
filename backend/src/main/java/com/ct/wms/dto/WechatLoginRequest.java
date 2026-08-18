package com.ct.wms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "微信小程序登录请求")
public class WechatLoginRequest {

    @NotBlank(message = "微信code不能为空")
    @Schema(description = "wx.login()获取的code")
    private String code;

    @Schema(description = "加密数据（可选，用于获取手机号等）")
    private String encryptedData;

    @Schema(description = "加密算法初始向量（可选）")
    private String iv;
}
