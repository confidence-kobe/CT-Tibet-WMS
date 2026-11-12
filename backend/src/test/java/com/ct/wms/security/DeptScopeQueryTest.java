package com.ct.wms.security;

import com.ct.wms.common.enums.InboundType;
import com.ct.wms.common.exception.BusinessException;
import com.ct.wms.dto.ApplyDTO;
import com.ct.wms.dto.InboundDTO;
import com.ct.wms.entity.Apply;
import com.ct.wms.entity.Inventory;
import com.ct.wms.entity.InventoryLog;
import com.ct.wms.service.ApplyService;
import com.ct.wms.service.InboundService;
import com.ct.wms.service.InventoryService;
import com.ct.wms.service.StatisticsService;
import com.ct.wms.util.TestDataBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 库存、统计、申请单的部门隔离测试
 *
 * 测试数据（data.sql）：仓库1属于部门2，仓库2属于部门3；用户3是部门2的仓管员；
 * 用户4（employee1）属于部门2，用户5（employee2）属于部门3
 *
 * @author CT Development Team
 * @since 2026-11-12
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("库存/统计/申请单部门隔离测试")
class DeptScopeQueryTest {

    private static final Long WAREHOUSE_DEPT2 = 1L;
    private static final Long WAREHOUSE_DEPT3 = 2L;
    private static final Long MATERIAL_ID = 1L;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InboundService inboundService;

    @Autowired
    private ApplyService applyService;

    @Autowired
    private StatisticsService statisticsService;

    @BeforeEach
    void setUp() {
        asAdmin();
        // 给部门3的仓库入 50 件（低于物资1的最低库存 100，会产生预警）
        inboundService.createInbound(inboundDto(WAREHOUSE_DEPT3, "50"));
    }

    @AfterEach
    void tearDown() {
        TestDataBuilder.clearSecurityContext();
    }

    private void asAdmin() {
        TestDataBuilder.mockSecurityContext(1L, "admin", "ADMIN");
    }

    private void asWarehouseManager() {
        TestDataBuilder.mockSecurityContext(3L, "warehouse", "WAREHOUSE");
    }

    private InboundDTO inboundDto(Long warehouseId, String quantity) {
        InboundDTO dto = new InboundDTO();
        dto.setWarehouseId(warehouseId);
        dto.setInboundType(InboundType.PURCHASE);
        dto.setInboundTime(LocalDateTime.now());
        InboundDTO.InboundDetailDTO detail = new InboundDTO.InboundDetailDTO();
        detail.setMaterialId(MATERIAL_ID);
        detail.setQuantity(new BigDecimal(quantity));
        detail.setUnitPrice(BigDecimal.TEN);
        dto.setDetails(Collections.singletonList(detail));
        return dto;
    }

    private Long createApply(Long applicantId, String username, Long warehouseId) {
        TestDataBuilder.mockSecurityContext(applicantId, username, "USER");
        ApplyDTO dto = new ApplyDTO();
        dto.setWarehouseId(warehouseId);
        dto.setApplyReason("测试申请");
        ApplyDTO.ApplyDetailDTO detail = new ApplyDTO.ApplyDetailDTO();
        detail.setMaterialId(MATERIAL_ID);
        detail.setQuantity(BigDecimal.ONE);
        dto.setDetails(Collections.singletonList(detail));
        return applyService.createApply(dto);
    }

    private List<Long> warehouseIdsOf(List<Inventory> inventories) {
        return inventories.stream().map(Inventory::getWarehouseId).distinct().collect(Collectors.toList());
    }

    // ========== 库存 ==========

    @Test
    @DisplayName("库存列表 - 仓管员只能看到本部门仓库的库存")
    void listInventories_scopedByDept() {
        assertThat(warehouseIdsOf(inventoryService.listInventories(1, 100, null, null, null).getRecords()))
                .contains(WAREHOUSE_DEPT2, WAREHOUSE_DEPT3);

        asWarehouseManager();
        assertThat(warehouseIdsOf(inventoryService.listInventories(1, 100, null, null, null).getRecords()))
                .containsOnly(WAREHOUSE_DEPT2);
        assertThat(inventoryService.listInventories(1, 100, WAREHOUSE_DEPT3, null, null).getRecords()).isEmpty();
    }

    @Test
    @DisplayName("库存流水 - 仓管员只能看到本部门仓库的流水")
    void listInventoryLogs_scopedByDept() {
        asWarehouseManager();
        List<Long> warehouseIds = inventoryService.listInventoryLogs(1, 100, null, null, null, null, null)
                .getRecords().stream().map(InventoryLog::getWarehouseId).distinct().collect(Collectors.toList());
        assertThat(warehouseIds).doesNotContain(WAREHOUSE_DEPT3);

        asAdmin();
        assertThat(inventoryService.listInventoryLogs(1, 100, WAREHOUSE_DEPT3, null, null, null, null)
                .getRecords()).isNotEmpty();
    }

    @Test
    @DisplayName("低库存预警 - 仓管员看不到其他部门仓库的预警")
    void lowStockAlerts_scopedByDept() {
        assertThat(warehouseIdsOf(inventoryService.listLowStockAlerts(null))).contains(WAREHOUSE_DEPT3);

        asWarehouseManager();
        assertThat(warehouseIdsOf(inventoryService.listLowStockAlerts(null))).doesNotContain(WAREHOUSE_DEPT3);
    }

    @Test
    @DisplayName("库存详情 - 不能查看其他部门仓库的库存")
    void getInventoryById_otherDept_forbidden() {
        Inventory other = inventoryService.getInventory(WAREHOUSE_DEPT3, MATERIAL_ID);
        Inventory own = inventoryService.getInventory(WAREHOUSE_DEPT2, MATERIAL_ID);

        asWarehouseManager();
        assertThat(inventoryService.getById(own.getId())).isNotNull();
        assertThatThrownBy(() -> inventoryService.getById(other.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权查看");
    }

    // ========== 统计 ==========

    @Test
    @DisplayName("统计报表 - 仓管员只统计本部门仓库")
    void statistics_scopedByDept() {
        LocalDate today = LocalDate.now();
        int adminCount = statisticsService.getInboundStatistics(today, today, null).getTotalCount();
        long adminWarehouses = statisticsService.getDashboardStats().getTotalWarehouses();

        asWarehouseManager();
        // setUp 中部门3仓库的入库不应计入
        assertThat(statisticsService.getInboundStatistics(today, today, null).getTotalCount())
                .isEqualTo(adminCount - 1);
        assertThat(statisticsService.getInboundStatistics(today, today, WAREHOUSE_DEPT3).getTotalCount())
                .isZero();
        assertThat(statisticsService.getDashboardStats().getTotalWarehouses()).isEqualTo(adminWarehouses - 1);
        assertThat(statisticsService.getInventoryStatistics(WAREHOUSE_DEPT3).getMaterialCount()).isZero();
    }

    // ========== 申请单 ==========

    @Test
    @DisplayName("申请单列表 - 仓管员只能看到本部门仓库的申请")
    void listApplies_scopedByDept() {
        Long own = createApply(4L, "employee1", WAREHOUSE_DEPT2);
        Long other = createApply(5L, "employee2", WAREHOUSE_DEPT3);

        asWarehouseManager();
        List<Long> ids = applyService.listApplies(1, 100, null, null, null, null, null, null, null)
                .getRecords().stream().map(Apply::getId).collect(Collectors.toList());
        assertThat(ids).contains(own).doesNotContain(other);
    }

    @Test
    @DisplayName("申请单详情 - 申请人和本部门仓管员可以看，其他人不行")
    void getApplyById_permissions() {
        Long own = createApply(4L, "employee1", WAREHOUSE_DEPT2);
        Long other = createApply(5L, "employee2", WAREHOUSE_DEPT3);

        // 申请人本人
        TestDataBuilder.mockSecurityContext(4L, "employee1", "USER");
        assertThat(applyService.getApplyById(own).getId()).isEqualTo(own);

        // 其他部门的员工
        TestDataBuilder.mockSecurityContext(5L, "employee2", "USER");
        assertThatThrownBy(() -> applyService.getApplyById(own))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权查看");

        // 同部门的普通员工（用户3属于部门2，这里按普通员工身份）
        TestDataBuilder.mockSecurityContext(3L, "warehouse", "USER");
        assertThatThrownBy(() -> applyService.getApplyById(own))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权查看");

        // 本部门仓管员可以看本部门的，不能看其他部门的
        asWarehouseManager();
        assertThat(applyService.getApplyById(own).getId()).isEqualTo(own);
        assertThatThrownBy(() -> applyService.getApplyById(other))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权查看");

        // 系统管理员都能看
        asAdmin();
        assertThat(applyService.getApplyById(other).getId()).isEqualTo(other);
    }
}
