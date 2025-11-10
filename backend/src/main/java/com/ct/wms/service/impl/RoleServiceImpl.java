package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ct.wms.common.enums.UserStatus;
import com.ct.wms.common.exception.BusinessException;
import com.ct.wms.dto.RoleDTO;
import com.ct.wms.entity.Role;
import com.ct.wms.entity.User;
import com.ct.wms.mapper.RoleMapper;
import com.ct.wms.mapper.UserMapper;
import com.ct.wms.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 角色Service实现类
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final UserMapper userMapper;

    @Override
    public Page<Role> listRoles(Integer pageNum, Integer pageSize, String keyword) {
        Page<Role> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Role::getRoleName, keyword)
                    .or().like(Role::getRoleCode, keyword));
        }

        wrapper.orderByAsc(Role::getRoleLevel);

        return roleMapper.selectPage(page, wrapper);
    }

    @Override
    public List<Role> listAllRoles() {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getStatus, UserStatus.ENABLED.getValue());
        wrapper.orderByAsc(Role::getRoleLevel);

        return roleMapper.selectList(wrapper);
    }

    @Override
    public Role getRoleById(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }

        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRole(RoleDTO dto) {
        // 检查角色编码是否存在
        if (checkRoleCodeExists(dto.getRoleCode(), null)) {
            throw new BusinessException(400, "角色编码已存在");
        }

        // 创建角色
        Role role = new Role();
        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode().toUpperCase());
        role.setRoleLevel(dto.getRoleLevel());
        role.setStatus(UserStatus.ENABLED.getValue());
        role.setRemark(dto.getRemark());

        roleMapper.insert(role);

        log.info("创建角色: id={}, roleCode={}", role.getId(), role.getRoleCode());

        return role.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(Long id, RoleDTO dto) {
        // 查询角色
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }

        // 检查角色编码是否重复
        if (!role.getRoleCode().equals(dto.getRoleCode().toUpperCase())) {
            if (checkRoleCodeExists(dto.getRoleCode(), id)) {
                throw new BusinessException(400, "角色编码已存在");
            }
        }

        // 更新角色
        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode().toUpperCase());
        role.setRoleLevel(dto.getRoleLevel());
        role.setRemark(dto.getRemark());

        roleMapper.updateById(role);

        log.info("更新角色: id={}, roleCode={}", id, role.getRoleCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        // 查询角色
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }

        // 检查是否有用户使用该角色
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRoleId, id);
        long count = userMapper.selectCount(wrapper);

        if (count > 0) {
            throw new BusinessException(400, "该角色下还有用户，无法删除");
        }

        // 删除角色
        roleMapper.deleteById(id);

        log.info("删除角色: id={}, roleCode={}", id, role.getRoleCode());
    }

    @Override
    public boolean checkRoleCodeExists(String roleCode, Long excludeId) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getRoleCode, roleCode.toUpperCase());

        if (excludeId != null) {
            wrapper.ne(Role::getId, excludeId);
        }

        return roleMapper.selectCount(wrapper) > 0;
    }
}
