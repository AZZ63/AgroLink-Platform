package com.agrolink.userservice.controller;

import com.agrolink.common.constant.AuthConstant;
import com.agrolink.common.exception.BusinessException;
import com.agrolink.common.result.Result;
import com.agrolink.userservice.entity.SysMenu;
import com.agrolink.userservice.repository.SysMenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统菜单管理控制器
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class MenuController {

    private final SysMenuRepository sysMenuRepository;

    @GetMapping("/menus")
    public Result<List<Map<String, Object>>> getMenuTree() {
        List<SysMenu> allMenus = sysMenuRepository.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysMenu>()
                        .orderByAsc(SysMenu::getSortOrder));
        return Result.success(buildTree(allMenus));
    }

    @GetMapping("/menus/flat")
    public Result<List<SysMenu>> getFlatMenus() {
        List<SysMenu> list = sysMenuRepository.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysMenu>()
                        .orderByAsc(SysMenu::getSortOrder));
        return Result.success(list);
    }

    @PostMapping("/menus")
    public Result<Void> createMenu(@RequestBody SysMenu menu) {
        if (menu.getParentId() == null) menu.setParentId(0L);
        if (menu.getSortOrder() == null) menu.setSortOrder(0);
        if (menu.getVisible() == null) menu.setVisible(1);
        sysMenuRepository.insert(menu);
        return Result.success();
    }

    @PutMapping("/menus/{id}")
    public Result<Void> updateMenu(@PathVariable Long id, @RequestBody SysMenu menu) {
        SysMenu exist = sysMenuRepository.selectById(id);
        if (exist == null) throw new BusinessException(404, "菜单不存在");
        menu.setId(id);
        sysMenuRepository.updateById(menu);
        return Result.success();
    }

    @DeleteMapping("/menus/{id}")
    public Result<Void> deleteMenu(@PathVariable Long id) {
        SysMenu exist = sysMenuRepository.selectById(id);
        if (exist == null) throw new BusinessException(404, "菜单不存在");
        Long childCount = sysMenuRepository.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException(400, "存在子菜单，请先删除子菜单");
        }
        sysMenuRepository.deleteById(id);
        return Result.success();
    }

    @GetMapping("/current/menus")
    public Result<List<Map<String, Object>>> getCurrentUserMenus(
            @RequestHeader(AuthConstant.ROLE_HEADER) String role) {
        List<SysMenu> allMenus = sysMenuRepository.selectByRoleCode(role);
        return Result.success(buildTree(allMenus));
    }

    private List<Map<String, Object>> buildTree(List<SysMenu> menus) {
        Map<Long, List<SysMenu>> grouped = menus.stream()
                .collect(Collectors.groupingBy(m -> m.getParentId() != null ? m.getParentId() : 0L));
        List<Map<String, Object>> tree = new ArrayList<>();
        for (SysMenu menu : grouped.getOrDefault(0L, Collections.emptyList())) {
            tree.add(buildNode(menu, grouped));
        }
        return tree;
    }

    private Map<String, Object> buildNode(SysMenu menu, Map<Long, List<SysMenu>> grouped) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("id", menu.getId());
        node.put("parentId", menu.getParentId());
        node.put("menuName", menu.getMenuName());
        node.put("path", menu.getPath());
        node.put("component", menu.getComponent());
        node.put("icon", menu.getIcon());
        node.put("permissionCode", menu.getPermissionCode());
        node.put("sortOrder", menu.getSortOrder());
        node.put("visible", menu.getVisible());
        List<SysMenu> children = grouped.getOrDefault(menu.getId(), Collections.emptyList());
        if (!children.isEmpty()) {
            List<Map<String, Object>> childNodes = children.stream()
                    .sorted(Comparator.comparingInt(m -> m.getSortOrder() != null ? m.getSortOrder() : 0))
                    .map(child -> buildNode(child, grouped))
                    .collect(Collectors.toList());
            node.put("children", childNodes);
        }
        return node;
    }
}
