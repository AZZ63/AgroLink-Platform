package com.agrolink.userservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 收货地址实体类
 * <p>
 * 映射数据库表 {@code addresses}，存储用户收货地址信息。
 */
@Data
@TableName("addresses")
public class Address {
    /** 地址主键 ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 所属用户 ID */
    private Long userId;
    /** 联系人姓名 */
    private String contactName;
    /** 联系电话 */
    private String phone;
    /** 省份 */
    private String province;
    /** 城市 */
    private String city;
    /** 区/县 */
    private String district;
    /** 详细地址 */
    private String detail;
    /** 是否为默认地址 */
    private Boolean isDefault;
    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
