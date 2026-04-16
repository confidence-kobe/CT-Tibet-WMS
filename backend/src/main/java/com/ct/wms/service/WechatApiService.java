package com.ct.wms.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ct.wms.config.WechatProperties;
import com.ct.wms.dto.WechatMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 微信小程序 API 服务
 *
 * <p>职责：
 * <ul>
 *   <li>获取并缓存 access_token（Redis，默认 7000s，比微信 7200s 留安全余量）</li>
 *   <li>下发订阅消息 {@code /cgi-bin/message/subscribe/send}</li>
 * </ul>
 *
 * <p>若 {@code wechat.mini-program.enabled=false} 或未配置 appId/appSecret，
 * 发送方法会直接返回 false 并记录 WARN，不抛异常——便于开发环境无配置运行。
 *
 * @author CT Development Team
 * @since 2026-04-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatApiService {

    private static final String REDIS_KEY_ACCESS_TOKEN = "wms:wechat:access_token";

    private final WechatProperties properties;
    private final RestTemplate restTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 进程内锁，避免多线程同时向微信刷新 access_token（单实例部署足够；
     * 集群部署下多实例之间偶发重复刷新可接受——微信允许并发，新 token 会替换旧 token）
     */
    private final ReentrantLock refreshLock = new ReentrantLock();

    /**
     * 发送订阅消息
     *
     * @param wechatMessage 消息 DTO
     * @return 是否发送成功
     */
    public boolean sendSubscribeMessage(WechatMessageDTO wechatMessage) {
        if (!properties.isEnabled()) {
            log.warn("微信模板消息未启用 (wechat.mini-program.enabled=false)，跳过发送: openId={}",
                    wechatMessage.getOpenId());
            return false;
        }
        if (StringUtils.isBlank(properties.getAppId()) || StringUtils.isBlank(properties.getAppSecret())) {
            log.warn("微信 AppID/AppSecret 未配置，跳过发送: openId={}", wechatMessage.getOpenId());
            return false;
        }
        if (StringUtils.isBlank(wechatMessage.getOpenId()) || StringUtils.isBlank(wechatMessage.getTemplateId())) {
            log.warn("微信消息缺少 openId 或 templateId，跳过发送: {}", wechatMessage);
            return false;
        }

        String accessToken = getAccessToken();
        if (StringUtils.isBlank(accessToken)) {
            log.error("获取微信 access_token 失败，跳过发送: openId={}", wechatMessage.getOpenId());
            return false;
        }

        URI uri = UriComponentsBuilder.fromHttpUrl(properties.getSubscribeSendUrl())
                .queryParam("access_token", accessToken)
                .build(true)
                .toUri();

        Map<String, Object> body = buildSubscribeBody(wechatMessage);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> httpEntity = new HttpEntity<>(JSON.toJSONString(body), headers);

        try {
            String response = restTemplate.postForObject(uri, httpEntity, String.class);
            JSONObject json = JSON.parseObject(response);
            if (json == null) {
                log.error("微信订阅消息返回空响应: openId={}", wechatMessage.getOpenId());
                return false;
            }
            Integer errcode = json.getInteger("errcode");
            if (errcode != null && errcode == 0) {
                log.info("微信订阅消息下发成功: openId={}, templateId={}",
                        wechatMessage.getOpenId(), wechatMessage.getTemplateId());
                return true;
            }
            // access_token 过期(40001/42001)时主动清缓存，下次重试
            if (errcode != null && (errcode == 40001 || errcode == 42001 || errcode == 40014)) {
                log.warn("微信 access_token 失效，清理缓存: errcode={}, errmsg={}",
                        errcode, json.getString("errmsg"));
                stringRedisTemplate.delete(REDIS_KEY_ACCESS_TOKEN);
            }
            log.error("微信订阅消息下发失败: openId={}, errcode={}, errmsg={}",
                    wechatMessage.getOpenId(), errcode, json.getString("errmsg"));
            return false;
        } catch (Exception e) {
            log.error("调用微信订阅消息接口异常: openId={}", wechatMessage.getOpenId(), e);
            return false;
        }
    }

    /**
     * 获取 access_token（带 Redis 缓存）
     */
    public String getAccessToken() {
        String cached = stringRedisTemplate.opsForValue().get(REDIS_KEY_ACCESS_TOKEN);
        if (StringUtils.isNotBlank(cached)) {
            return cached;
        }

        refreshLock.lock();
        try {
            // 双重检查，避免重复刷新
            cached = stringRedisTemplate.opsForValue().get(REDIS_KEY_ACCESS_TOKEN);
            if (StringUtils.isNotBlank(cached)) {
                return cached;
            }
            return refreshAccessToken();
        } finally {
            refreshLock.unlock();
        }
    }

    /**
     * 强制从微信刷新 access_token 并写入缓存
     */
    private String refreshAccessToken() {
        URI uri = UriComponentsBuilder.fromHttpUrl(properties.getAccessTokenUrl())
                .queryParam("grant_type", "client_credential")
                .queryParam("appid", properties.getAppId())
                .queryParam("secret", properties.getAppSecret())
                .build(true)
                .toUri();

        try {
            String response = restTemplate.getForObject(uri, String.class);
            JSONObject json = JSON.parseObject(response);
            if (json == null) {
                log.error("微信 access_token 返回空响应");
                return null;
            }
            String token = json.getString("access_token");
            if (StringUtils.isBlank(token)) {
                log.error("微信 access_token 获取失败: errcode={}, errmsg={}",
                        json.getInteger("errcode"), json.getString("errmsg"));
                return null;
            }
            // 微信返回 expires_in 通常为 7200s，使用配置的缓存时长留安全余量
            Integer expiresIn = json.getInteger("expires_in");
            int cacheSeconds = properties.getAccessTokenCacheSeconds();
            if (expiresIn != null && expiresIn > 0 && expiresIn - 200 < cacheSeconds) {
                cacheSeconds = expiresIn - 200;
            }
            stringRedisTemplate.opsForValue().set(
                    REDIS_KEY_ACCESS_TOKEN, token, Duration.ofSeconds(cacheSeconds));
            log.info("微信 access_token 刷新成功，缓存 {}s", cacheSeconds);
            return token;
        } catch (Exception e) {
            log.error("调用微信 access_token 接口异常", e);
            return null;
        }
    }

    /**
     * 构造微信订阅消息请求体
     * <p>将 DTO 中的 {@code Map<String, TemplateData>} 转换为微信要求的
     * {@code Map<String, {value:"..."}>} 结构
     */
    private Map<String, Object> buildSubscribeBody(WechatMessageDTO wechatMessage) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("touser", wechatMessage.getOpenId());
        body.put("template_id", wechatMessage.getTemplateId());
        if (StringUtils.isNotBlank(wechatMessage.getPage())) {
            body.put("page", wechatMessage.getPage());
        }
        body.put("miniprogram_state", properties.getMiniprogramState());
        body.put("lang", properties.getLang());

        Map<String, Object> data = new LinkedHashMap<>();
        if (wechatMessage.getData() != null) {
            wechatMessage.getData().forEach((k, v) -> {
                if (v != null && v.getValue() != null) {
                    Map<String, String> item = new LinkedHashMap<>();
                    item.put("value", v.getValue());
                    data.put(k, item);
                }
            });
        }
        body.put("data", data);
        return body;
    }
}
