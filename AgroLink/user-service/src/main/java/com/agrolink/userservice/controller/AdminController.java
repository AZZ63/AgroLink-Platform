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
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员后台管理控制器
 * <p>
 * 提供用户列表查询、角色修改、用户删除等管理功能，仅 ADMIN 角色可访问。
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    /**
     * 获取全部用户列表（仅管理员）
     */
    @GetMapping("/users")
    public Result<List<User>> listUsers(@RequestHeader(AuthConstant.ROLE_HEADER) String role) {
        if (!"ADMIN".equals(role)) throw new BusinessException(403, "仅管理员可操作");
        return Result.success(userRepository.selectList(null));
    }

    /**
     * 修改指定用户的角色（仅管理员）
     * <p>
     * 同步更新用户表（users）和角色映射表（user_role），确保数据一致性。
     */
    @PutMapping("/users/{id}/role")
    public Result<Void> updateRole(@PathVariable Long id, @RequestBody String role,
                                    @RequestHeader(AuthConstant.ROLE_HEADER) String userRole) {
        if (!"ADMIN".equals(userRole)) throw new BusinessException(403, "仅管理员可操作");
        User user = userRepository.selectById(id);
        if (user == null) throw new BusinessException(404, "用户不存在");
        if (!role.matches("FARMER|MERCHANT|ADMIN")) throw new BusinessException(400, "无效角色");

        user.setRole(role);
        userRepository.updateById(user);

        // 同步更新 user_role 映射表
        Role targetRole = roleRepository.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Role>()
                        .eq(Role::getRoleCode, role));
        if (targetRole != null) {
            userRoleRepository.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserRole>()
                    .eq(UserRole::getUserId, id));
            UserRole ur = new UserRole();
            ur.setUserId(id);
            ur.setRoleId(targetRole.getId());
            userRoleRepository.insert(ur);
        }

        return Result.success();
    }

    /**
     * 删除指定用户（仅管理员）
     */
    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id,
                                    @RequestHeader(AuthConstant.ROLE_HEADER) String role) {
        if (!"ADMIN".equals(role)) throw new BusinessException(403, "仅管理员可操作");
        User user = userRepository.selectById(id);
        if (user == null) throw new BusinessException(404, "用户不存在");
        userRepository.deleteById(id);
        return Result.success();
    }

}
