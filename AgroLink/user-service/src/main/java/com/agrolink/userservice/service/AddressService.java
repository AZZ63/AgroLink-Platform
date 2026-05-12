package com.agrolink.userservice.service;

import com.agrolink.common.exception.BusinessException;
import com.agrolink.userservice.entity.Address;
import com.agrolink.userservice.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 收货地址业务逻辑层
 * <p>
 * 封装地址的增删改查及默认地址设置等业务逻辑，包含归属权校验。
 */
@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    /**
     * 获取指定用户的全部地址列表
     */
    public List<Address> getUserAddresses(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    /**
     * 根据地址 ID 和用户 ID 获取地址详情
     * <p>
     * 校验地址归属权，防止跨用户访问。
     */
    public Address getAddress(Long id, Long userId) {
        Address address = addressRepository.selectById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(404,"地址不存在");
        }
        return address;
    }

    /**
     * 新增收货地址
     * <p>
     * 若该地址被标记为默认地址，则先清除用户原有的默认地址。
     */
    @Transactional
    public Address createAddress(Address address, Long userId) {
        address.setUserId(userId);
        address.setId(null);
        if (address.getIsDefault() == null) address.setIsDefault(false);
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            clearDefault(userId);
        }
        addressRepository.insert(address);
        return address;
    }

    /**
     * 修改收货地址
     * <p>
     * 校验地址归属权，若新地址被标记为默认则先清除原有的默认地址。
     */
    @Transactional
    public Address updateAddress(Address address, Long userId) {
        Address existing = addressRepository.selectById(address.getId());
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new BusinessException(403,"无权修改此地址");
        }
        address.setUserId(userId);
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            clearDefault(userId);
        }
        addressRepository.updateById(address);
        return addressRepository.selectById(address.getId());
    }

    /**
     * 删除收货地址
     * <p>
     * 校验地址归属权后执行删除。
     */
    @Transactional
    public void deleteAddress(Long id, Long userId) {
        Address address = addressRepository.selectById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(403,"无权删除此地址");
        }
        addressRepository.deleteById(id);
    }

    /**
     * 将指定地址设为当前用户的默认地址
     * <p>
     * 先清除用户原有的默认地址标记，再将当前地址标记为默认。
     */
    @Transactional
    public void setDefault(Long id, Long userId) {
        Address address = addressRepository.selectById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(404,"地址不存在");
        }
        clearDefault(userId);
        address.setIsDefault(true);
        addressRepository.updateById(address);
    }

    /**
     * 清除当前用户的默认地址标记
     * <p>
     * 若用户已有默认地址，将其 {@code isDefault} 置为 false。
     */
    private void clearDefault(Long userId) {
        Address existing = addressRepository.findDefaultByUserId(userId);
        if (existing != null) {
            existing.setIsDefault(false);
            addressRepository.updateById(existing);
        }
    }
}
