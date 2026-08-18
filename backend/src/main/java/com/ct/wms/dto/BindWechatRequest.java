package com.ct.wms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@Schema(description = "绑定微信请求")
public class BindWechatRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "微信openid不能为空")
    @Size(max = 64, message = "openid长度不能超过64位")
    @Schema(description = "微信openid")
    private String wechatOpenid;
}
