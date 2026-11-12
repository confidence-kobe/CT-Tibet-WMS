package com.ct.wms.security;

import com.ct.wms.common.enums.InboundType;
import com.ct.wms.common.enums.OutboundSource;
import com.ct.wms.common.enums.OutboundStatus;
import com.ct.wms.common.enums.OutboundType;
import com.ct.wms.common.exception.BusinessException;
import com.ct.wms.dto.InboundDTO;
import com.ct.wms.dto.OutboundDTO;
import com.ct.wms.dto.WarehouseDTO;
import com.ct.wms.entity.Outbound;
import com.ct.wms.entity.Warehouse;
import com.ct.wms.mapper.OutboundMapper;
import com.ct.wms.service.InboundService;
import com.ct.wms.service.OutboundService;
import com.ct.wms.service.WarehouseService;
import com.ct.wms.util.TestDataBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 仓库数据权限（部门隔离）测试：出库单、仓库、入库创建
 *
 * 测试数据（data.sql）：仓库1属于部门2，仓库2属于部门3；
 * 用户2是部门2的部门管理员，用户3是部门2的仓管员，用户1是总部的系统管理员
 *
 * @author CT Development Team
 * @since 2026-11-12
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("仓库数据权限测试")
class WarehouseDataScopeTest {

    private static final Long WAREHOUSE_DEPT2 = 1L;
    private static final Long WAREHOUSE_DEPT3 = 2L;
    private static final Long MATERIAL_ID = 1L;

    @Autowired
    private WarehouseDataScope warehouseDataScope;

    @Autowired
    private OutboundService outboundService;

    @Autowired
    private OutboundMapper outboundMapper;

    @Autowired
    private InboundService inboundService;

    @Autowired
    private WarehouseService warehouseService;

    @BeforeEach
    void setUp() {
        asAdmin();
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

    private void asDeptAdmin() {
        TestDataBuilder.mockSecurityContext(2L, "dept_admin", "DEPT_ADMIN");
    }

    private InboundDTO inboundDto(Long warehouseId) {
        InboundDTO dto = new InboundDTO();
        dto.setWarehouseId(warehouseId);
        dto.setInboundType(InboundType.PURCHASE);
        dto.setInboundTime(LocalDateTime.now());
        InboundDTO.InboundDetailDTO detail = new InboundDTO.InboundDetailDTO();
        detail.setMaterialId(MATERIAL_ID);
        detail.setQuantity(new BigDecimal("50"));
        detail.setUnitPrice(BigDecimal.TEN);
        dto.setDetails(Collections.singletonList(detail));
        return dto;
    }

    private OutboundDTO outboundDto(Long warehouseId) {
        OutboundDTO dto = new OutboundDTO();
        dto.setWarehouseId(warehouseId);
        dto.setOutboundType(OutboundType.RECEIVE.getValue());
        dto.setOutboundTime(LocalDateTime.now());
        OutboundDTO.OutboundDetailDTO detail = new OutboundDTO.OutboundDetailDTO();
        detail.setMaterialId(MATERIAL_ID);
        detail.setQuantity(new BigDecimal("1"));
        dto.setDetails(Collections.singletonList(detail));
        return dto;
    }

    /** 以管理员身份入库后直接出库，返回出库单ID */
    private Long createDirectOutbound(Long warehouseId) {
        asAdmin();
        inboundService.createInbound(inboundDto(warehouseId));
        return outboundService.createOutboundDirect(outboundDto(warehouseId));
    }

    /** 直接插入一张待取货的出库单（用于取消/确认测试） */
    private Long insertPendingOutbound(Long warehouseId) {
        Outbound outbound = new Outbound();
        outbound.setOutboundNo("CK_TEST_" + System.nanoTime());
        outbound.setWarehouseId(warehouseId);
        outbound.setOutboundType(OutboundType.RECEIVE);
        outbound.setSource(OutboundSource.FROM_APPLY);
        outbound.setOperatorId(1L);
        outbound.setOperatorName("Admin");
        outbound.setReceiverName("测试领取人");
        outbound.setOutboundTime(LocalDateTime.now());
        outbound.setStatus(OutboundStatus.PENDING_PICKUP);
        outboundMapper.insert(outbound);
        return outbound.getId();
    }

    private List<Long> listOutboundIds(Long warehouseId) {
        return outboundService.listOutbounds(1, 100, warehouseId, null, null, null, null, null, null, null)
                .getRecords().stream().map(Outbound::getId).collect(Collectors.toList());
    }

    private List<Long> ids(List<Warehouse> warehouses) {
        return warehouses.stream().map(Warehouse::getId).collect(Collectors.toList());
    }

    // ========== 出库单 ==========

    @Test
    @DisplayName("出库单列表 - 仓管员只能看到本部门，管理员看到全部")
    void listOutbounds_scopedByDept() {
        Long own = createDirectOutbound(WAREHOUSE_DEPT2);
        Long other = createDirectOutbound(WAREHOUSE_DEPT3);

        assertThat(listOutboundIds(null)).contains(own, other);

        asWarehouseManager();
        assertThat(listOutboundIds(null)).contains(own).doesNotContain(other);
        assertThat(listOutboundIds(WAREHOUSE_DEPT3)).isEmpty();
    }

    @Test
    @DisplayName("出库单详情 - 不能查看其他部门仓库的出库单")
    void getOutboundById_otherDept_forbidden() {
        Long own = createDirectOutbound(WAREHOUSE_DEPT2);
        Long other = createDirectOutbound(WAREHOUSE_DEPT3);

        asWarehouseManager();
        assertThat(outboundService.getOutboundById(own).getId()).isEqualTo(own);
        assertThatThrownBy(() -> outboundService.getOutboundById(other))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权查看");
    }

    @Test
    @DisplayName("直接出库 - 不能从其他部门的仓库出库")
    void createOutboundDirect_otherDept_forbidden() {
        asAdmin();
        inboundService.createInbound(inboundDto(WAREHOUSE_DEPT3));

        asWarehouseManager();
        assertThatThrownBy(() -> outboundService.createOutboundDirect(outboundDto(WAREHOUSE_DEPT3)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权从其他部门的仓库出库");
    }

    @Test
    @DisplayName("确认/取消出库 - 不能操作其他部门仓库的出库单")
    void confirmAndCancel_otherDept_forbidden() {
        Long other = insertPendingOutbound(WAREHOUSE_DEPT3);

        asWarehouseManager();
        assertThatThrownBy(() -> outboundService.confirmOutbound(other))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权操作");
        assertThatThrownBy(() -> outboundService.cancelOutbound(other, "测试"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权操作");
    }

    @Test
    @DisplayName("取消出库 - 本部门仓管员可以取消")
    void cancel_ownDept_allowed() {
        Long own = insertPendingOutbound(WAREHOUSE_DEPT2);

        asWarehouseManager();
        outboundService.cancelOutbound(own, "测试");

        assertThat(outboundMapper.selectById(own).getStatus()).isEqualTo(OutboundStatus.CANCELED);
    }

    @Test
    @DisplayName("定时任务 - 以系统账户运行时不受部门限制")
    void systemTask_unrestricted() {
        // 与 OutboundTimeoutTask / ApplyTimeoutTask 注入的系统账户一致
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("system", null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        assertThat(warehouseDataScope.canAccess(WAREHOUSE_DEPT2)).isTrue();
        assertThat(warehouseDataScope.canAccess(WAREHOUSE_DEPT3)).isTrue();

        // 没有任何登录上下文时同样不受限制
        SecurityContextHolder.clearContext();
        assertThat(warehouseDataScope.getAccessibleWarehouseIds()).isNull();
    }

    // ========== 入库创建 ==========

    @Test
    @DisplayName("创建入库单 - 不能向其他部门的仓库入库")
    void createInbound_otherDept_forbidden() {
        asWarehouseManager();
        assertThatThrownBy(() -> inboundService.createInbound(inboundDto(WAREHOUSE_DEPT3)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权向其他部门的仓库入库");
    }

    // ========== 仓库 ==========

    @Test
    @DisplayName("仓库列表 - 仓管员只能看到本部门仓库，管理员看到全部")
    void listWarehouses_scopedByDept() {
        assertThat(ids(warehouseService.listWarehouses(null, null))).contains(WAREHOUSE_DEPT2, WAREHOUSE_DEPT3);

        asWarehouseManager();
        assertThat(ids(warehouseService.listWarehouses(null, null)))
                .contains(WAREHOUSE_DEPT2).doesNotContain(WAREHOUSE_DEPT3);
        // 指定其他部门也查不到
        assertThat(warehouseService.listWarehouses(3L, null)).isEmpty();
    }

    @Test
    @DisplayName("我的仓库 - 管理员看到全部，仓管员只看到本部门")
    void getMyWarehouses_scopedByDept() {
        assertThat(ids(warehouseService.getMyWarehouses())).contains(WAREHOUSE_DEPT2, WAREHOUSE_DEPT3);

        asWarehouseManager();
        assertThat(ids(warehouseService.getMyWarehouses()))
                .contains(WAREHOUSE_DEPT2).doesNotContain(WAREHOUSE_DEPT3);
    }

    @Test
    @DisplayName("仓库详情 - 不能查看其他部门的仓库")
    void getWarehouseById_otherDept_forbidden() {
        asWarehouseManager();
        assertThat(warehouseService.getWarehouseById(WAREHOUSE_DEPT2).getId()).isEqualTo(WAREHOUSE_DEPT2);
        assertThatThrownBy(() -> warehouseService.getWarehouseById(WAREHOUSE_DEPT3))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权查看");
    }

    @Test
    @DisplayName("部门管理员 - 不能在其他部门建仓库，也不能把本部门仓库改到其他部门")
    void deptAdmin_cannotManageOtherDeptWarehouses() {
        asDeptAdmin();

        WarehouseDTO create = new WarehouseDTO();
        create.setWarehouseName("越权仓库");
        create.setWarehouseCode("WH_TEST_X");
        create.setDeptId(3L);
        create.setManagerId(2L);
        assertThatThrownBy(() -> warehouseService.createWarehouse(create))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("只能管理本部门");

        WarehouseDTO move = new WarehouseDTO();
        move.setWarehouseName("Network Warehouse");
        move.setWarehouseCode("WH_WL_001");
        move.setDeptId(3L);
        move.setManagerId(3L);
        assertThatThrownBy(() -> warehouseService.updateWarehouse(WAREHOUSE_DEPT2, move))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("只能管理本部门");

        assertThatThrownBy(() -> warehouseService.updateStatus(WAREHOUSE_DEPT3, 1))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权");
    }
}
