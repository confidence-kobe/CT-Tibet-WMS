package com.ct.wms.security;

import com.ct.wms.common.enums.ApplyStatus;
import com.ct.wms.common.enums.OutboundSource;
import com.ct.wms.common.enums.OutboundStatus;
import com.ct.wms.common.enums.OutboundType;
import com.ct.wms.common.exception.BusinessException;
import com.ct.wms.dto.OutboundDTO;
import com.ct.wms.entity.Apply;
import com.ct.wms.entity.Outbound;
import com.ct.wms.mapper.ApplyMapper;
import com.ct.wms.mapper.OutboundMapper;
import com.ct.wms.service.ApplyService;
import com.ct.wms.service.InventoryService;
import com.ct.wms.service.OutboundService;
import com.ct.wms.util.TestDataBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 回归测试：权限 = 角色 + 归属（2026-09-29 W2 评审 #2 #3）。
 * <p>
 * 种子数据（test/resources/data.sql）：
 * 用户 1 admin(ADMIN,部门1)；2 dept_admin(DEPT_ADMIN,部门2)；3 warehouse(WAREHOUSE,部门2，管理仓库1)；
 * 4 employee1(USER,部门2)；5 employee2(USER,部门3)。仓库 1 属部门2、仓管员=3；仓库 2 属部门3、无仓管员。
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("数据范围：角色 + 归属")
class DataScopeGuardTest {

    private static final long WH_NETWORK = 1L;     // 部门2，仓管员=用户3
    private static final long WH_MARKETING = 2L;   // 部门3

    @Autowired private ApplyService applyService;
    @Autowired private OutboundService outboundService;
    @Autowired private InventoryService inventoryService;
    @Autowired private ApplyMapper applyMapper;
    @Autowired private OutboundMapper outboundMapper;

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private void loginAs(long userId, String role) {
        TestDataBuilder.mockSecurityContext(userId, "u" + userId, role);
    }

    private Long newApply(long applicantId, long deptId, long warehouseId) {
        Apply a = new Apply();
        a.setApplyNo("SQ_T_" + UUID.randomUUID());
        a.setApplicantId(applicantId);
        a.setDeptId(deptId);
        a.setWarehouseId(warehouseId);
        a.setStatus(ApplyStatus.PENDING);
        a.setApplyTime(LocalDateTime.now());
        applyMapper.insert(a);
        return a.getId();
    }

    private Long newPendingOutbound(long warehouseId) {
        Outbound o = new Outbound();
        o.setOutboundNo("CK_T_" + UUID.randomUUID());
        o.setWarehouseId(warehouseId);
        o.setOutboundType(OutboundType.RECEIVE);
        o.setSource(OutboundSource.FROM_APPLY);
        o.setStatus(OutboundStatus.PENDING_PICKUP);
        o.setOperatorId(1L);
        o.setOutboundTime(LocalDateTime.now());
        outboundMapper.insert(o);
        return o.getId();
    }

    // ---------------------------------------------------------------- #2 查看申请单
    @Test
    @DisplayName("#2 员工不能查看其他部门员工的申请单")
    void employeeCannotViewOthersApply() {
        Long othersApply = newApply(5L, 3L, WH_MARKETING);
        loginAs(4L, "USER");
        assertThatThrownBy(() -> applyService.getApplyById(othersApply))
                .isInstanceOf(BusinessException.class).hasMessageContaining("无权");
    }

    @Test
    @DisplayName("#2 本人、同部门管理员、仓库仓管员、ADMIN 可以查看")
    void allowedViewers() {
        Long apply = newApply(4L, 2L, WH_NETWORK);
        for (long[] who : new long[][]{{4}, {2}, {3}, {1}}) {
            loginAs(who[0], "X");
            assertThatCode(() -> applyService.getApplyById(apply)).as("user " + who[0]).doesNotThrowAnyException();
        }
    }

    @Test
    @DisplayName("#2 部门管理员不能查看其他部门的申请单")
    void deptAdminCannotViewOtherDept() {
        Long apply = newApply(5L, 3L, WH_MARKETING);
        loginAs(2L, "DEPT_ADMIN");
        assertThatThrownBy(() -> applyService.getApplyById(apply)).isInstanceOf(BusinessException.class);
    }

    // ---------------------------------------------------------------- #3 出库操作
    @Test
    @DisplayName("#3 仓库1的仓管员不能确认、取消、查看仓库2的出库单")
    void warehouseManagerCannotTouchOtherWarehouse() {
        Long outbound = newPendingOutbound(WH_MARKETING);
        loginAs(3L, "WAREHOUSE");
        assertThatThrownBy(() -> outboundService.confirmOutbound(outbound)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> outboundService.cancelOutbound(outbound, "x")).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> outboundService.getOutboundById(outbound)).isInstanceOf(BusinessException.class);
        assertThat(outboundMapper.selectById(outbound).getStatus()).isEqualTo(OutboundStatus.PENDING_PICKUP);
    }

    @Test
    @DisplayName("#3 仓库1的仓管员不能从仓库2直接出库，仓库2库存不变")
    void warehouseManagerCannotDirectOutboundFromOtherWarehouse() {
        loginAs(1L, "ADMIN");
        inventoryService.increaseInventory(WH_MARKETING, 1L, new BigDecimal("10"), "T", null, 1L);
        BigDecimal before = inventoryService.getInventory(WH_MARKETING, 1L).getQuantity();

        loginAs(3L, "WAREHOUSE");
        OutboundDTO.OutboundDetailDTO d = new OutboundDTO.OutboundDetailDTO();
        d.setMaterialId(1L);
        d.setQuantity(new BigDecimal("5"));
        OutboundDTO dto = new OutboundDTO();
        dto.setWarehouseId(WH_MARKETING);
        dto.setOutboundType(1);
        dto.setOutboundTime(LocalDateTime.now());
        dto.setDetails(List.of(d));

        assertThatThrownBy(() -> outboundService.createOutboundDirect(dto)).isInstanceOf(BusinessException.class);
        assertThat(inventoryService.getInventory(WH_MARKETING, 1L).getQuantity()).isEqualByComparingTo(before);
    }

    @Test
    @DisplayName("#3 自己管理的仓库照常可以查看出库单")
    void warehouseManagerCanViewOwnWarehouse() {
        Long outbound = newPendingOutbound(WH_NETWORK);
        loginAs(3L, "WAREHOUSE");
        assertThatCode(() -> outboundService.getOutboundById(outbound)).doesNotThrowAnyException();
        loginAs(2L, "DEPT_ADMIN");   // 同部门的部门管理员
        assertThatCode(() -> outboundService.getOutboundById(outbound)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("定时任务的 system 账户（ROLE_ADMIN）可以取消任意出库单")
    void systemAccountCanCancel() {
        Long outbound = newPendingOutbound(WH_MARKETING);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "system", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        assertThatCode(() -> outboundService.cancelOutbound(outbound, "超时")).doesNotThrowAnyException();
        assertThat(outboundMapper.selectById(outbound).getStatus()).isEqualTo(OutboundStatus.CANCELED);
    }

    @Test
    @DisplayName("非用户主体且没有 ROLE_ADMIN：拒绝")
    void anonymousPrincipalRejected() {
        Long outbound = newPendingOutbound(WH_NETWORK);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "someone", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))));
        assertThatThrownBy(() -> outboundService.getOutboundById(outbound)).isInstanceOf(BusinessException.class);
    }
}
