package com.ct.wms.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus字段自动填充处理器
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时自动填充
     *
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始插入填充...");

        // 自动填充创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        // 自动填充更新时间
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        // TODO: 从SecurityContext获取当前登录用户ID
        // Long userId = SecurityUtils.getCurrentUserId();
        // this.strictInsertFill(metaObject, "createBy", Long.class, userId);
        // this.strictInsertFill(metaObject, "updateBy", Long.class, userId);
    }

    /**
     * 更新时自动填充
     *
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始更新填充...");

        // 自动填充更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        // TODO: 从SecurityContext获取当前登录用户ID
        // Long userId = SecurityUtils.getCurrentUserId();
        // this.strictUpdateFill(metaObject, "updateBy", Long.class, userId);
    }
}
