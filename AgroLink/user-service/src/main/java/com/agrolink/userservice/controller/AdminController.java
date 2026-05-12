package com.agrolink.userservice.controller;

import com.agrolink.common.constant.AuthConstant;
import com.agrolink.common.result.Result;
import com.agrolink.common.exception.BusinessException;
import com.agrolink.userservice.entity.Role;
import com.agrolink.userservice.entity.User;
import com.agrolink.userservice.entity.UserRole;
import com.agrolink.userservice.repository.RoleRepository;
import com.agrolink.userservice.repository.UserRepository;
import com.agrolink.userservice.repository.UserRoleRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员后台管理控制器
 * <p>
 * 提供用户列表分页查询、角色修改、状态管理、用户删除等管理功能。
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    /**
     * 分页查询用户列表（支持关键词搜索）
     */
    @GetMapping("/users")
    public Result<Map<String, Object>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .orderByDesc(User::getCreatedAt);

        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getPhone, keyword));
        }

        Page<User> p = userRepository.selectPage(new Page<>(page, size), wrapper);
        List<User> users = p.getRecords();

        List<Map<String, Object>> list = users.stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("role", u.getRole());
            m.put("roleName", getRoleName(u.getRole()));
            m.put("phone", u.getPhone());
            m.put("status", u.getStatus());
            m.put("createdAt", u.getCreatedAt());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", list);
        result.put("total", p.getTotal());
        result.put("page", p.getCurrent());
        result.put("size", p.getSize());
        return Result.success(result);
    }

    @PutMapping("/users/{id}/role")
    public Result<Void> updateRole(@PathVariable Long id, @RequestBody String role) {
        User user = userRepository.selectById(id);
        if (user == null) throw new BusinessException(404, "用户不存在");
        if (!role.matches("FARMER|MERCHANT|ADMIN")) throw new BusinessException(400, "无效角色");

        user.setRole(role);
        userRepository.updateById(user);

        Role targetRole = roleRepository.selectOne(
                new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, role));
        if (targetRole != null) {
            userRoleRepository.delete(new LambdaQueryWrapper<UserRole>()
                    .eq(UserRole::getUserId, id));
            UserRole ur = new UserRole();
            ur.setUserId(id);
            ur.setRoleId(targetRole.getId());
            userRoleRepository.insert(ur);
        }
        return Result.success();
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        User user = userRepository.selectById(id);
        if (user == null) throw new BusinessException(404, "用户不存在");
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "状态值无效（0=禁用，1=启用）");
        }
        user.setStatus(status);
        userRepository.updateById(user);
        return Result.success();
    }

    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        User user = userRepository.selectById(id);
        if (user == null) throw new BusinessException(404, "用户不存在");
        userRoleRepository.delete(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, id));
        userRepository.deleteById(id);
        return Result.success();
    }

    private String getRoleName(String roleCode) {
        if ("ADMIN".equals(roleCode)) return "管理员";
        if ("FARMER".equals(roleCode)) return "农户";
        if ("MERCHANT".equals(roleCode)) return "商户";
        return roleCode;
    }
}
