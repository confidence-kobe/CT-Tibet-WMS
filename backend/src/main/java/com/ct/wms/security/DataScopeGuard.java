package com.ct.wms.security;

import com.ct.wms.common.exception.BusinessException;
import com.ct.wms.entity.Apply;
import com.ct.wms.entity.Role;
import com.ct.wms.entity.User;
import com.ct.wms.entity.Warehouse;
import com.ct.wms.mapper.RoleMapper;
import com.ct.wms.mapper.UserMapper;
import com.ct.wms.mapper.WarehouseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 数据范围校验：权限 = 角色 + 归属。
 * <p>
 * {@code @PreAuthorize} 只能判断“你是不是仓管员/部门管理员”，
 * “这个仓库/这张单子是不是你管的”必须在这里按数据判断（2026-09-29 W2 评审 #2 #3）。
 * <ul>
 *   <li>ADMIN（含定时任务注入的 system 账户，权限为 ROLE_ADMIN）：全部放行</li>
 *   <li>仓库仓管员：warehouse.managerId == 当前用户</li>
 *   <li>部门管理员：当前用户部门 == 仓库/申请单所属部门</li>
 *   <li>申请人本人：可以查看自己的申请单</li>
 * </ul>
 * 角色以数据库为准（与 {@code ApplyServiceImpl} 的审批校验一致），不信任客户端或令牌里携带的部门信息。
 */
@Component
@RequiredArgsConstructor
public class DataScopeGuard {

    private static final String ADMIN = "ADMIN";
    private static final String DEPT_ADMIN = "DEPT_ADMIN";

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final WarehouseMapper warehouseMapper;

    /** 当前操作者（从数据库加载的部门与角色） */
    private static final class Actor {
        final Long id;
        final Long deptId;
        final String roleCode;
        final boolean admin;

        Actor(Long id, Long deptId, String roleCode, boolean admin) {
            this.id = id;
            this.deptId = deptId;
            this.roleCode = roleCode;
            this.admin = admin;
        }
    }

    private Actor currentActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new BusinessException(401, "未登录");
        }
        boolean adminAuthority = auth.getAuthorities() != null && auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(("ROLE_" + ADMIN)::equals);
        if (!(auth.getPrincipal() instanceof UserDetailsImpl)) {
            // 定时任务的 system 账户等非用户主体：只有 ROLE_ADMIN 才放行
            return new Actor(null, null, adminAuthority ? ADMIN : null, adminAuthority);
        }
        Long userId = ((UserDetailsImpl) auth.getPrincipal()).getId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        Role role = user.getRoleId() != null ? roleMapper.selectById(user.getRoleId()) : null;
        // 统一转大写：sql/init_data.sql 的角色编码是小写（admin / dept_admin），测试种子是大写（Codex 异源审查发现的回归）
        String roleCode = role != null && role.getRoleCode() != null
                ? role.getRoleCode().toUpperCase(java.util.Locale.ROOT) : null;
        return new Actor(userId, user.getDeptId(), roleCode, ADMIN.equals(roleCode) || adminAuthority);
    }

    private static boolean manages(Actor a, Warehouse w) {
        return a.id != null && w.getManagerId() != null && w.getManagerId().equals(a.id);
    }

    private static boolean sameDeptAdmin(Actor a, Long deptId) {
        return DEPT_ADMIN.equals(a.roleCode) && a.deptId != null && a.deptId.equals(deptId);
    }

    /**
     * 能否操作该仓库（直接出库、确认取货、取消出库、查看出库单）
     */
    public void assertCanOperateWarehouse(Long warehouseId) {
        Actor a = currentActor();
        if (a.admin) {
            return;
        }
        Warehouse w = warehouseId != null ? warehouseMapper.selectById(warehouseId) : null;
        if (w == null) {
            throw new BusinessException(404, "仓库不存在");
        }
        if (!manages(a, w) && !sameDeptAdmin(a, w.getDeptId())) {
            throw new BusinessException(403, "无权操作该仓库的数据");
        }
    }

    /**
     * 能否查看该申请单：申请人本人、所属部门的部门管理员、该仓库仓管员、ADMIN
     */
    public void assertCanViewApply(Apply apply) {
        Actor a = currentActor();
        if (a.admin || (a.id != null && a.id.equals(apply.getApplicantId())) || sameDeptAdmin(a, apply.getDeptId())) {
            return;
        }
        Warehouse w = apply.getWarehouseId() != null ? warehouseMapper.selectById(apply.getWarehouseId()) : null;
        if (w != null && manages(a, w)) {
            return;
        }
        throw new BusinessException(403, "无权查看此申请单");
    }
}
