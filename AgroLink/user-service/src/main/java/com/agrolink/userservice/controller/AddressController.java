package com.agrolink.userservice.controller;

import com.agrolink.common.constant.AuthConstant;
import com.agrolink.common.result.Result;
import com.agrolink.userservice.entity.Address;
import com.agrolink.userservice.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收货地址管理控制器
 * <p>
 * 提供当前登录用户收货地址的增、删、改、查及设置默认地址等功能。
 */
@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    /**
     * 获取当前用户的全部收货地址列表
     */
    @GetMapping("/list")
    public Result<List<Address>> list(@RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        return Result.success(addressService.getUserAddresses(userId));
    }

    /**
     * 根据地址 ID 获取单条地址详情
     */
    @GetMapping("/{id}")
    public Result<Address> get(@PathVariable Long id,
                                @RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        return Result.success(addressService.getAddress(id, userId));
    }

    /**
     * 新增收货地址
     */
    @PostMapping("/create")
    public Result<Address> create(@RequestBody Address address,
                                   @RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        return Result.success(addressService.createAddress(address, userId));
    }

    /**
     * 更新收货地址
     */
    @PutMapping("/update")
    public Result<Address> update(@RequestBody Address address,
                                   @RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        return Result.success(addressService.updateAddress(address, userId));
    }

    /**
     * 删除指定的收货地址
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                                @RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        addressService.deleteAddress(id, userId);
        return Result.success();
    }

    /**
     * 将指定地址设为当前用户的默认地址
     */
    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id,
                                    @RequestHeader(AuthConstant.USER_ID_HEADER) Long userId) {
        addressService.setDefault(id, userId);
        return Result.success();
    }
}
