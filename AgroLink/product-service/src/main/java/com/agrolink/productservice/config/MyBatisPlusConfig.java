package com.agrolink.productservice.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 配置类
 * <p>
 * 配置分页插件和自动填充处理器（用于统一填充 createdAt 等公共字段）。
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * 注册 MyBatis-Plus 拦截器，添加分页功能
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页插件，支持物理分页
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

    /**
     * 注册元数据自动填充处理器
     * <p>
     * 在插入数据时自动填充 createdAt 字段为当前时间。
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                // 插入时自动填充 createdAt 字段
                this.strictInsertFill(metaObject, "createdAt", LocalDateTime::now, LocalDateTime.class);
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                // 更新时暂无需自动填充
            }
        };
    }
}
