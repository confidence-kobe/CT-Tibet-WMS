package com.ct.wms.integration;

import com.ct.wms.common.enums.ApplyStatus;
import com.ct.wms.common.exception.BusinessException;
import com.ct.wms.dto.ApplyDTO;
import com.ct.wms.dto.ApprovalDTO;
import com.ct.wms.dto.InventoryStatisticsDTO;
import com.ct.wms.dto.OutboundDTO;
import com.ct.wms.entity.Apply;
import com.ct.wms.entity.ApplyDetail;
import com.ct.wms.entity.Inventory;
import com.ct.wms.entity.Outbound;
import com.ct.wms.entity.Warehouse;
import com.ct.wms.mapper.ApplyMapper;
import com.ct.wms.service.ApplyService;
import com.ct.wms.service.InboundService;
import com.ct.wms.service.InventoryService;
import com.ct.wms.service.OutboundService;
import com.ct.wms.service.StatisticsService;
import com.ct.wms.service.UserService;
import com.ct.wms.service.WarehouseService;
import com.ct.wms.util.TestDataBuilder;
import com.ct.wms.vo.MiniProgramDashboardVO;
import com.ct.wms.vo.UserOptionVO;
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
import java.time.LocalDateTime;
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

    @Autowired
    private InboundService inboundService;

    @Autowired
    private UserService userService;

    @Autowired
    private OutboundService outboundService;

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

    // ==================== 申请单：待审批、明细、访问控制 ====================

    @Test
    @DisplayName("部门管理员的待审批列表包含本部门申请，且带明细和当前库存")
    void deptAdminPendingListIncludesDetailsAndStock() {
        Long applyId = createApply("employee1", DEPT2_WAREHOUSE_ID, 1L);

        loginAs("dept_admin");
        List<Apply> pending = applyService.listPendingApplies(1, 20).getRecords();
        Apply apply = pending.stream().filter(a -> a.getId().equals(applyId)).findFirst().orElseThrow();

        assertThat(apply.getDetails()).hasSize(1);
        ApplyDetail detail = apply.getDetails().get(0);
        assertThat(detail.getMaterialName()).isEqualTo("Cable 12 Core");
        assertThat(detail.getCurrentStock()).isEqualByComparingTo("500");
        assertThat(detail.getIsStockSufficient()).isTrue();
    }

    @Test
    @DisplayName("仓管的待审批列表只包含自己管理仓库的申请")
    void warehouseManagerPendingListOnlyOwnWarehouses() {
        Long ownApply = createApply("employee1", DEPT2_WAREHOUSE_ID, 1L);
        Long otherApply = createApply("employee2", DEPT3_WAREHOUSE_ID, 7L);

        loginAs("warehouse");
        List<Long> ids = applyService.listPendingApplies(1, 20).getRecords().stream()
                .map(Apply::getId).collect(Collectors.toList());

        assertThat(ids).contains(ownApply).doesNotContain(otherApply);
    }

    @Test
    @DisplayName("我的申请列表带有物资明细")
    void myAppliesIncludeDetails() {
        createApply("employee1", DEPT2_WAREHOUSE_ID, 1L);

        List<Apply> mine = applyService.listMyApplies(1, 20, null).getRecords();
        assertThat(mine).isNotEmpty();
        assertThat(mine.get(0).getDetails()).extracting(ApplyDetail::getMaterialName).contains("Cable 12 Core");
    }

    @Test
    @DisplayName("员工不能查看他人的申请单，本人可以查看")
    void employeeCannotViewOthersApply() {
        Long applyId = createApply("employee1", DEPT2_WAREHOUSE_ID, 1L);
        assertThat(applyService.getApplyById(applyId).getDetails()).hasSize(1);

        loginAs("employee2");
        assertThatThrownBy(() -> applyService.getApplyById(applyId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("入库、出库列表按部门隔离")
    void inboundOutboundListsAreScopedByDept() {
        loginAs("warehouse");
        assertThatThrownBy(() -> inboundService.listInbounds(1, 20, DEPT3_WAREHOUSE_ID, null, null, null, null, null))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> outboundService.listOutbounds(1, 20, DEPT3_WAREHOUSE_ID,
                null, null, null, null, null, null, null))
                .isInstanceOf(BusinessException.class);
    }

    // ==================== 直接出库领用人 ====================

    private OutboundDTO directOutbound() {
        OutboundDTO dto = new OutboundDTO();
        dto.setWarehouseId(DEPT2_WAREHOUSE_ID);
        dto.setOutboundType(1);
        dto.setOutboundTime(LocalDateTime.now());
        OutboundDTO.OutboundDetailDTO detail = new OutboundDTO.OutboundDetailDTO();
        detail.setMaterialId(1L);
        detail.setQuantity(BigDecimal.ONE);
        detail.setUnitPrice(BigDecimal.TEN);
        dto.setDetails(List.of(detail));
        return dto;
    }

    @Test
    @DisplayName("直接出库：外部领用人可只填姓名电话；两者都不填时给出明确提示")
    void directOutboundReceiver() {
        loginAs("warehouse");

        OutboundDTO external = directOutbound();
        external.setReceiverName("施工队 王师傅");
        external.setReceiverPhone("13900000000");
        external.setPurpose("拉萨小区光缆施工");
        Outbound saved = outboundService.getOutboundById(outboundService.createOutboundDirect(external));
        assertThat(saved.getReceiverName()).isEqualTo("施工队 王师傅");
        assertThat(saved.getReceiverPhone()).isEqualTo("13900000000");
        assertThat(saved.getPurpose()).isEqualTo("拉萨小区光缆施工");

        OutboundDTO internal = directOutbound();
        internal.setReceiverId(4L);
        Outbound savedInternal = outboundService.getOutboundById(outboundService.createOutboundDirect(internal));
        assertThat(savedInternal.getReceiverName()).isEqualTo("Emp Zhang");
        assertThat(savedInternal.getReceiverPhone()).isEqualTo("13800000013");

        // 未填单价时按物资标准单价计算金额（物资1 单价1500）
        OutboundDTO noPrice = directOutbound();
        noPrice.setReceiverId(4L);
        noPrice.getDetails().get(0).setUnitPrice(null);
        Outbound savedNoPrice = outboundService.getOutboundById(outboundService.createOutboundDirect(noPrice));
        assertThat(savedNoPrice.getTotalAmount()).isEqualByComparingTo("1500");

        assertThatThrownBy(() -> outboundService.createOutboundDirect(directOutbound()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("领用人");
    }

    @Test
    @DisplayName("领用人选项按部门隔离")
    void userOptionsAreScopedByDept() {
        loginAs("warehouse");
        List<String> names = userService.listUserOptions(null).stream()
                .map(UserOptionVO::getRealName).collect(Collectors.toList());
        assertThat(names).contains("Emp Zhang").doesNotContain("Emp Li");

        loginAs("admin");
        assertThat(userService.listUserOptions(null)).extracting(UserOptionVO::getRealName).contains("Emp Li");
    }

    // ==================== 审批意见与拒绝原因 ====================

    @Test
    @DisplayName("审批意见和拒绝原因会被保存；拒绝时必须填写原因")
    void approvalOpinionAndRejectReasonArePersisted() {
        Long approvedId = createApply("employee1", DEPT2_WAREHOUSE_ID, 1L);
        Long rejectedId = createApply("employee1", DEPT2_WAREHOUSE_ID, 3L);

        loginAs("warehouse");
        ApprovalDTO approve = new ApprovalDTO();
        approve.setApplyId(approvedId);
        approve.setApprovalResult(1);
        approve.setApprovalRemark("同意，请尽快领取");
        applyService.approveApply(approve);

        ApprovalDTO rejectWithoutReason = new ApprovalDTO();
        rejectWithoutReason.setApplyId(rejectedId);
        rejectWithoutReason.setApprovalResult(2);
        assertThatThrownBy(() -> applyService.approveApply(rejectWithoutReason))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("拒绝原因");

        ApprovalDTO reject = new ApprovalDTO();
        reject.setApplyId(rejectedId);
        reject.setApprovalResult(2);
        reject.setApprovalRemark("本月配额已用完");
        applyService.approveApply(reject);

        assertThat(applyMapper.selectById(approvedId).getApprovalOpinion()).isEqualTo("同意，请尽快领取");
        loginAs("employee1");
        Apply approvedDetail = applyService.getApplyById(approvedId);
        assertThat(approvedDetail.getOutboundNo()).startsWith("CK_");
        assertThat(approvedDetail.getWarehouseName()).isEqualTo("Network Warehouse");

        // 待领取的出库单还没有出库时间；确认领取后才记录
        loginAs("warehouse");
        Outbound pendingOutbound = outboundService.getOutboundById(approvedDetail.getOutboundId());
        assertThat(pendingOutbound.getOutboundTime()).isNull();
        assertThat(pendingOutbound.getReceiverPhone()).isEqualTo("13800000013");
        assertThat(pendingOutbound.getPurpose()).isEqualTo("测试申请");
        assertThat(pendingOutbound.getApplyNo()).startsWith("SQ_");
        outboundService.confirmOutbound(pendingOutbound.getId());
        assertThat(outboundService.getOutboundById(pendingOutbound.getId()).getOutboundTime()).isNotNull();
        Apply rejected = applyMapper.selectById(rejectedId);
        assertThat(rejected.getStatus()).isEqualTo(ApplyStatus.REJECTED);
        assertThat(rejected.getRejectReason()).isEqualTo("本月配额已用完");
    }

    @Test
    @DisplayName("库存按状态筛选与汇总")
    void inventoryStatusFilterAndSummary() {
        loginAs("admin");
        // 仓库2 的物资9 库存10，低于最低库存15
        List<Inventory> low = inventoryService.listInventories(1, 20, null, null, null, null, 1).getRecords();
        assertThat(low).extracting(Inventory::getMaterialId).containsExactly(9L);

        var summary = inventoryService.getInventorySummary(null, null, null);
        assertThat(summary.getLowStockCount()).isEqualTo(1);
        assertThat(summary.getTotalMaterials()).isEqualTo(13);

        loginAs("employee1");
        assertThat(inventoryService.getInventorySummary(null, null, null).getTotalMaterials()).isEqualTo(10);
    }
}
