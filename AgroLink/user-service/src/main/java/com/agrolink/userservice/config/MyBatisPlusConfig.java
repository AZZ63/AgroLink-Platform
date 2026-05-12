package com.agrolink.userservice.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 配置类
 * <p>
 * 配置自动填充处理器，用于在插入或更新时自动设置公共字段（如创建时间）。
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * 注册 MetaObjectHandler 自动填充处理器
     * <p>
     * 当前仅在插入时自动填充 {@code createdAt} 字段。
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                // 插入时自动填充 createdAt 字段为当前时间
                this.strictInsertFill(metaObject, "createdAt", LocalDateTime::now, LocalDateTime.class);
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                // 更新时暂无需自动填充的字段
            }
        };
    }
}
