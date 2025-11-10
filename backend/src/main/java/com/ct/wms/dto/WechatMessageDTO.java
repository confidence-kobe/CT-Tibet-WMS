package com.ct.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 微信模板消息DTO
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WechatMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 接收人OpenID
     */
    private String openId;

    /**
     * 模板ID
     */
    private String templateId;

    /**
     * 跳转页面路径
     */
    private String page;

    /**
     * 模板数据
     */
    private Map<String, TemplateData> data;

    /**
     * 模板数据项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateData implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 数据值
         */
        private String value;

        /**
         * 颜色
         */
        private String color;
    }
}
