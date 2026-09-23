package com.ct.wms.integration;

import com.ct.wms.common.enums.ApplyStatus;
import com.ct.wms.common.exception.BusinessException;
import com.ct.wms.dto.ApplyDTO;
import com.ct.wms.dto.ApprovalDTO;
import com.ct.wms.dto.InventoryStatisticsDTO;
import com.ct.wms.entity.Apply;
import com.ct.wms.entity.Inventory;
import com.ct.wms.entity.Warehouse;
import com.ct.wms.mapper.ApplyMapper;
import com.ct.wms.service.ApplyService;
import com.ct.wms.service.InventoryService;
import com.ct.wms.service.StatisticsService;
import com.ct.wms.service.WarehouseService;
import com.ct.wms.util.TestDataBuilder;
import com.ct.wms.vo.MiniProgramDashboardVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 角色判断与部门数据隔离集成测试
 * <p>
 * 测试数据（data.sql）与生产环境一致，角色编码为小写（admin / dept_admin / warehouse / user）。
 * 部门2：仓库1（仓管 warehouse，部门管理员 dept_admin，员工 employee1）
 * 部门3：仓库2（员工 employee2），仓库2 的物资9 库存低于最低库存
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("角色判断与部门数据隔离")
class RoleAndDataScopeTest {

    private static final long DEPT2_WAREHOUSE_ID = 1L;
    private static final long DEPT3_WAREHOUSE_ID = 2L;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private ApplyService applyService;

    @Autowired
    private ApplyMapper applyMapper;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private StatisticsService statisticsService;

    @AfterEach
    void tearDown() {
        TestDataBuilder.clearSecurityContext();
    }

    /**
     * 按真实登录流程构建当前用户（与 JWT 过滤器一致）
     */
    private void loginAs(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
    }

    private Long createApply(String applicant, long warehouseId, long materialId) {
        loginAs(applicant);
        ApplyDTO dto = new ApplyDTO();
        dto.setWarehouseId(warehouseId);
        dto.setApplyReason("测试申请");
        ApplyDTO.ApplyDetailDTO detail = new ApplyDTO.ApplyDetailDTO();
        detail.setMaterialId(materialId);
        detail.setQuantity(BigDecimal.ONE);
        dto.setDetails(List.of(detail));
        return applyService.createApply(dto);
    }

    // ==================== 角色编码大小写 ====================

    @Test
    @DisplayName("部门管理员可以审批本部门申请")
    void deptAdminCanApproveOwnDeptApply() {
        Long applyId = createApply("employee1", DEPT2_WAREHOUSE_ID, 1L);

        loginAs("dept_admin");
        ApprovalDTO approval = new ApprovalDTO();
        approval.setApplyId(applyId);
        approval.setApprovalResult(1);
        applyService.approveApply(approval);

        Apply apply = applyMapper.selectById(applyId);
        assertThat(apply.getStatus()).isEqualTo(ApplyStatus.APPROVED);
    }

    @Test
    @DisplayName("部门管理员只能看到本部门的申请")
    void deptAdminOnlySeesOwnDeptApplies() {
        Long ownDeptApply = createApply("employee1", DEPT2_WAREHOUSE_ID, 1L);
        Long otherDeptApply = createApply("employee2", DEPT3_WAREHOUSE_ID, 7L);

        loginAs("dept_admin");
        List<Long> visibleIds = applyService.listApplies(1, 100, null, null, null, null, null, null, null)
                .getRecords().stream().map(Apply::getId).collect(Collectors.toList());

        assertThat(visibleIds).contains(ownDeptApply).doesNotContain(otherDeptApply);
    }

    @Test
    @DisplayName("系统管理员可以看到所有部门的仓库")
    void adminSeesAllWarehouses() {
        loginAs("admin");
        List<Long> ids = warehouseService.getMyWarehouses().stream()
                .map(Warehouse::getId).collect(Collectors.toList());

        assertThat(ids).contains(DEPT2_WAREHOUSE_ID, DEPT3_WAREHOUSE_ID);
    }

    @Test
    @DisplayName("仓管在小程序首页看到仓管视图")
    void warehouseManagerGetsManagerDashboard() {
        loginAs("warehouse");
        MiniProgramDashboardVO dashboard = statisticsService.getMiniProgramDashboard();

        assertThat(dashboard.getTodayData()).isNotNull();
        assertThat(dashboard.getPendingTasks()).isNotNull();
    }

    // ==================== 部门数据隔离 ====================

    @Test
    @DisplayName("仓管只能查询本部门仓库的库存")
    void warehouseManagerOnlySeesOwnDeptInventory() {
        loginAs("warehouse");
        List<Inventory> records = inventoryService.listInventories(1, 100, null, null, null).getRecords();

        assertThat(records).isNotEmpty();
        assertThat(records).allMatch(inventory -> inventory.getWarehouseId() == DEPT2_WAREHOUSE_ID);
    }

    @Test
    @DisplayName("仓管查询其他部门仓库库存被拒绝")
    void warehouseManagerCannotQueryOtherDeptWarehouse() {
        loginAs("warehouse");

        assertThatThrownBy(() -> inventoryService.listInventories(1, 100, DEPT3_WAREHOUSE_ID, null, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权访问");
        assertThatThrownBy(() -> statisticsService.getInventoryStatistics(DEPT3_WAREHOUSE_ID))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> inventoryService.getById(11L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("低库存预警按部门隔离")
    void lowStockAlertsAreScopedByDept() {
        loginAs("warehouse");
        assertThat(inventoryService.listLowStockAlerts(null)).isEmpty();

        loginAs("admin");
        assertThat(inventoryService.listLowStockAlerts(null))
                .extracting(Inventory::getWarehouseId)
                .contains(DEPT3_WAREHOUSE_ID);
    }

    @Test
    @DisplayName("库存统计按部门隔离")
    void inventoryStatisticsAreScopedByDept() {
        loginAs("warehouse");
        InventoryStatisticsDTO ownDept = statisticsService.getInventoryStatistics(null);

        loginAs("admin");
        InventoryStatisticsDTO all = statisticsService.getInventoryStatistics(null);

        assertThat(ownDept.getWarningCount()).isZero();
        assertThat(all.getWarningCount()).isEqualTo(1);
        assertThat(ownDept.getTotalValue()).isLessThan(all.getTotalValue());
    }
}
