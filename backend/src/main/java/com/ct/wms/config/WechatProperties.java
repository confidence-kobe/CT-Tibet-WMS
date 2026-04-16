package com.ct.wms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信小程序配置
 *
 * <p>通过 {@code wechat.mini-program.*} 前缀在 application.yml 中配置。
 * 生产环境建议通过环境变量 {@code WECHAT_APPID} / {@code WECHAT_APPSECRET} 注入。
 *
 * @author CT Development Team
 * @since 2026-04-15
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat.mini-program")
public class WechatProperties {

    /**
     * 是否启用微信模板消息发送
     * <p>关闭时 {@link com.ct.wms.service.WechatApiService} 只打印日志，不真正调用微信API
     */
    private boolean enabled = false;

    /**
     * 小程序 AppID
     */
    private String appId;

    /**
     * 小程序 AppSecret
     */
    private String appSecret;

    /**
     * access_token 获取地址
     */
    private String accessTokenUrl = "https://api.weixin.qq.com/cgi-bin/token";

    /**
     * 订阅消息下发地址
     */
    private String subscribeSendUrl = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send";

    /**
     * 小程序版本：developer / trial / formal
     */
    private String miniprogramState = "formal";

    /**
     * 语言，默认 zh_CN
     */
    private String lang = "zh_CN";

    /**
     * 连接超时(ms)
     */
    private int connectTimeout = 5000;

    /**
     * 读超时(ms)
     */
    private int readTimeout = 10000;

    /**
     * access_token 本地缓存时长(秒)
     * <p>微信返回的 access_token 有效期 7200s，这里留 200s 安全余量
     */
    private int accessTokenCacheSeconds = 7000;
}
