package com.agrolink.notifyservice.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 *
 * <p>在插入数据时自动为 {@code createdAt} 字段填充当前时间，
 * 避免业务代码中手动设置创建时间。</p>
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时的自动填充策略：将 {@code createdAt} 设为当前系统时间
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime::now, LocalDateTime.class);
    }

    /**
     * 更新时的自动填充策略（当前无特殊需求，保留空实现）
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 暂无需要自动填充的更新字段
    }
}
