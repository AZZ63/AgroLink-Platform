package com.agrolink.userservice.controller;

import com.agrolink.common.exception.BusinessException;
import com.agrolink.common.result.Result;
import com.agrolink.userservice.entity.Role;
import com.agrolink.userservice.entity.SysPermission;
import com.agrolink.userservice.entity.SysRolePermission;
import com.agrolink.userservice.repository.RoleRepository;
import com.agrolink.userservice.repository.SysPermissionRepository;
import com.agrolink.userservice.repository.SysRolePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleRepository roleRepository;
    private final SysPermissionRepository sysPermissionRepository;
    private final SysRolePermissionRepository sysRolePermissionRepository;

    @GetMapping
    public Result<List<Role>> listRoles() {
        return Result.success(roleRepository.selectList(null));
    }

    @PostMapping
    public Result<Void> createRole(@RequestBody Role role) {
        if (role.getRoleCode() == null || role.getRoleName() == null) {
            throw new BusinessException(400, "角色编码和名称不能为空");
        }
        Role exist = roleRepository.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Role>()
                        .eq(Role::getRoleCode, role.getRoleCode()));
        if (exist != null) throw new BusinessException(409, "角色编码已存在");
        roleRepository.insert(role);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> updateRole(@PathVariable Long id, @RequestBody Role role) {
        Role exist = roleRepository.selectById(id);
        if (exist == null) throw new BusinessException(404, "角色不存在");
        role.setId(id);
        roleRepository.updateById(role);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        Role exist = roleRepository.selectById(id);
        if (exist == null) throw new BusinessException(404, "角色不存在");
        sysRolePermissionRepository.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysRolePermission>()
                        .eq(SysRolePermission::getRoleId, id));
        roleRepository.deleteById(id);
        return Result.success();
    }

    @GetMapping("/{id}/permissions")
    public Result<List<Long>> getRolePermissions(@PathVariable Long id) {
        List<SysRolePermission> rps = sysRolePermissionRepository.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysRolePermission>()
                        .eq(SysRolePermission::getRoleId, id));
        return Result.success(rps.stream().map(SysRolePermission::getPermissionId).collect(Collectors.toList()));
    }

    @PutMapping("/{id}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long id, @RequestBody List<Long> permissionIds) {
        Role exist = roleRepository.selectById(id);
        if (exist == null) throw new BusinessException(404, "角色不存在");
        sysRolePermissionRepository.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysRolePermission>()
                        .eq(SysRolePermission::getRoleId, id));
        if (permissionIds != null && !permissionIds.isEmpty()) {
            permissionIds.stream().map(pid -> {
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(id); rp.setPermissionId(pid); return rp;
            }).forEach(sysRolePermissionRepository::insert);
        }
        return Result.success();
    }

    @GetMapping("/permissions/tree")
    public Result<List<Map<String, Object>>> getPermissionTree(
            @RequestParam(required = false) Long roleId) {
        List<SysPermission> allPermissions = sysPermissionRepository.selectList(null);
        Set<Long> assignedIds;
        if (roleId != null) {
            assignedIds = sysRolePermissionRepository.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysRolePermission>()
                            .eq(SysRolePermission::getRoleId, roleId)).stream()
                    .map(SysRolePermission::getPermissionId).collect(Collectors.toSet());
        } else {
            assignedIds = new HashSet<>();
        }

        Map<String, List<SysPermission>> grouped = allPermissions.stream()
                .collect(Collectors.groupingBy(p -> p.getPermissionCode().substring(0, p.getPermissionCode().indexOf(':'))));

        List<Map<String, Object>> tree = new ArrayList<>();
        for (Map.Entry<String, List<SysPermission>> entry : grouped.entrySet()) {
            String moduleName = entry.getKey();
            Map<String, Object> moduleNode = new LinkedHashMap<>();
            moduleNode.put("id", "module_" + moduleName);
            moduleNode.put("label", getModuleName(moduleName));
            moduleNode.put("disabled", true);

            List<Map<String, Object>> children = entry.getValue().stream().map(p -> {
                Map<String, Object> node = new LinkedHashMap<>();
                node.put("id", p.getId());
                node.put("label", p.getPermissionName() + " (" + p.getPermissionCode() + ")");
                node.put("checked", assignedIds.contains(p.getId()));
                return node;
            }).collect(Collectors.toList());
            moduleNode.put("children", children);
            tree.add(moduleNode);
        }
        return Result.success(tree);
    }

    private String getModuleName(String prefix) {
        switch (prefix) {
            case "user": return "用户管理";
            case "role": return "角色管理";
            case "menu": return "菜单管理";
            case "product": return "商品管理";
            default: return prefix;
        }
    }
}
