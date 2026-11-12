package com.ct.wms.service.impl;

import com.ct.wms.common.enums.InboundType;
import com.ct.wms.common.exception.BusinessException;
import com.ct.wms.dto.InboundDTO;
import com.ct.wms.dto.InboundUpdateDTO;
import com.ct.wms.entity.Inbound;
import com.ct.wms.entity.InboundDetail;
import com.ct.wms.entity.Inventory;
import com.ct.wms.mapper.InboundMapper;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 入库单编辑、作废测试
 *
 * 测试数据（data.sql）：仓库1属于部门2，仓库2属于部门3；用户3是部门2的仓管员
 *
 * @author CT Development Team
 * @since 2026-11-12
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("入库单编辑与作废测试")
class InboundUpdateVoidTest {

    private static final Long WAREHOUSE_DEPT2 = 1L;
    private static final Long WAREHOUSE_DEPT3 = 2L;
    private static final Long MATERIAL_ID = 1L;

    @Autowired
    private InboundService inboundService;

    @Autowired
    private InboundMapper inboundMapper;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private StatisticsService statisticsService;

    @BeforeEach
    void setUp() {
        TestDataBuilder.mockSecurityContext(1L, "admin", "ADMIN");
    }

    @AfterEach
    void tearDown() {
        TestDataBuilder.clearSecurityContext();
    }

    private Long createInbound(Long warehouseId, String quantity, String unitPrice) {
        InboundDTO dto = new InboundDTO();
        dto.setWarehouseId(warehouseId);
        dto.setInboundType(InboundType.PURCHASE);
        dto.setInboundTime(LocalDateTime.now());
        dto.setRemark("原备注");
        InboundDTO.InboundDetailDTO detail = new InboundDTO.InboundDetailDTO();
        detail.setMaterialId(MATERIAL_ID);
        detail.setQuantity(new BigDecimal(quantity));
        detail.setUnitPrice(new BigDecimal(unitPrice));
        dto.setDetails(Collections.singletonList(detail));
        return inboundService.createInbound(dto);
    }

    private BigDecimal stockOf(Long warehouseId) {
        Inventory inventory = inventoryService.getInventory(warehouseId, MATERIAL_ID);
        return inventory == null ? BigDecimal.ZERO : inventory.getQuantity();
    }

    @Test
    @DisplayName("作废入库单 - 库存扣回，单据标记为已作废")
    void voidInbound_revertsStock() {
        BigDecimal before = stockOf(WAREHOUSE_DEPT2);
        Long id = createInbound(WAREHOUSE_DEPT2, "30", "10");
        assertThat(stockOf(WAREHOUSE_DEPT2)).isEqualByComparingTo(before.add(new BigDecimal("30")));

        inboundService.voidInbound(id, "数量录错");

        Inbound inbound = inboundMapper.selectById(id);
        assertThat(inbound.getStatus()).isEqualTo(Inbound.STATUS_VOID);
        assertThat(inbound.getRemark()).contains("原备注").contains("【作废原因】数量录错");
        assertThat(stockOf(WAREHOUSE_DEPT2)).isEqualByComparingTo(before);
    }

    @Test
    @DisplayName("作废入库单 - 不能重复作废")
    void voidInbound_twice_rejected() {
        Long id = createInbound(WAREHOUSE_DEPT2, "30", "10");
        inboundService.voidInbound(id, "录错");

        assertThatThrownBy(() -> inboundService.voidInbound(id, "再次作废"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已作废");
    }

    @Test
    @DisplayName("作废入库单 - 货已被领走时拒绝作废")
    void voidInbound_stockAlreadyUsed_rejected() {
        BigDecimal before = stockOf(WAREHOUSE_DEPT2);
        Long id = createInbound(WAREHOUSE_DEPT2, "30", "10");
        // 把这批货之外的库存加这批货的一部分领走，使剩余库存不足 30
        inventoryService.decreaseInventory(WAREHOUSE_DEPT2, MATERIAL_ID,
                before.add(new BigDecimal("20")), "CK_TEST", 999L, 1L);

        assertThatThrownBy(() -> inboundService.voidInbound(id, "录错"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("可用库存不足");
    }

    @Test
    @DisplayName("作废入库单 - 库存被审批锁定时拒绝作废")
    void voidInbound_stockLocked_rejected() {
        BigDecimal before = stockOf(WAREHOUSE_DEPT2);
        Long id = createInbound(WAREHOUSE_DEPT2, "30", "10");
        // 锁定后可用库存只剩 20
        inventoryService.lockInventory(WAREHOUSE_DEPT2, MATERIAL_ID, before.add(new BigDecimal("10")));

        assertThatThrownBy(() -> inboundService.voidInbound(id, "录错"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("可用库存不足");
    }

    @Test
    @DisplayName("作废入库单 - 不能作废其他部门仓库的单据")
    void voidInbound_otherDept_forbidden() {
        Long id = createInbound(WAREHOUSE_DEPT3, "30", "10");
        TestDataBuilder.mockSecurityContext(3L, "warehouse", "WAREHOUSE");

        assertThatThrownBy(() -> inboundService.voidInbound(id, "录错"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权");
    }

    @Test
    @DisplayName("作废入库单 - 本部门仓管员可以作废")
    void voidInbound_sameDeptWarehouseManager_allowed() {
        Long id = createInbound(WAREHOUSE_DEPT2, "30", "10");
        TestDataBuilder.mockSecurityContext(3L, "warehouse", "WAREHOUSE");

        inboundService.voidInbound(id, "录错");

        assertThat(inboundMapper.selectById(id).getStatus()).isEqualTo(Inbound.STATUS_VOID);
    }

    @Test
    @DisplayName("编辑入库单 - 修改信息和单价，总金额重算，数量和库存不变")
    void updateInbound_success() {
        Long id = createInbound(WAREHOUSE_DEPT2, "30", "10");
        BigDecimal stock = stockOf(WAREHOUSE_DEPT2);
        InboundDetail detail = inboundService.getInboundById(id).getDetails().get(0);

        InboundUpdateDTO dto = new InboundUpdateDTO();
        dto.setInboundType(InboundType.RETURN);
        LocalDateTime newTime = LocalDateTime.now().minusDays(1).withNano(0);
        dto.setInboundTime(newTime);
        dto.setRemark("新备注");
        InboundUpdateDTO.DetailUpdateDTO item = new InboundUpdateDTO.DetailUpdateDTO();
        item.setId(detail.getId());
        item.setUnitPrice(new BigDecimal("12.5"));
        dto.setDetails(Collections.singletonList(item));

        inboundService.updateInbound(id, dto);

        Inbound updated = inboundService.getInboundById(id);
        assertThat(updated.getInboundType()).isEqualTo(InboundType.RETURN);
        assertThat(updated.getInboundTime()).isEqualTo(newTime);
        assertThat(updated.getRemark()).isEqualTo("新备注");
        assertThat(updated.getTotalAmount()).isEqualByComparingTo("375");
        assertThat(updated.getDetails().get(0).getQuantity()).isEqualByComparingTo("30");
        assertThat(updated.getDetails().get(0).getAmount()).isEqualByComparingTo("375");
        assertThat(stockOf(WAREHOUSE_DEPT2)).isEqualByComparingTo(stock);
    }

    @Test
    @DisplayName("编辑入库单 - 明细不属于该单据时拒绝")
    void updateInbound_foreignDetail_rejected() {
        Long id = createInbound(WAREHOUSE_DEPT2, "30", "10");
        Long otherId = createInbound(WAREHOUSE_DEPT2, "5", "10");
        InboundDetail otherDetail = inboundService.getInboundById(otherId).getDetails().get(0);

        InboundUpdateDTO dto = new InboundUpdateDTO();
        dto.setInboundType(InboundType.PURCHASE);
        dto.setInboundTime(LocalDateTime.now());
        InboundUpdateDTO.DetailUpdateDTO item = new InboundUpdateDTO.DetailUpdateDTO();
        item.setId(otherDetail.getId());
        item.setUnitPrice(BigDecimal.ONE);
        dto.setDetails(Collections.singletonList(item));

        assertThatThrownBy(() -> inboundService.updateInbound(id, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不属于");
    }

    @Test
    @DisplayName("编辑入库单 - 已作废的单据不能编辑")
    void updateInbound_voided_rejected() {
        Long id = createInbound(WAREHOUSE_DEPT2, "30", "10");
        inboundService.voidInbound(id, "录错");

        InboundUpdateDTO dto = new InboundUpdateDTO();
        dto.setInboundType(InboundType.PURCHASE);
        dto.setInboundTime(LocalDateTime.now());

        assertThatThrownBy(() -> inboundService.updateInbound(id, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已作废");
    }

    @Test
    @DisplayName("入库统计 - 不计入已作废的单据")
    void statistics_excludeVoided() {
        LocalDate today = LocalDate.now();
        int before = statisticsService.getInboundStatistics(today, today, WAREHOUSE_DEPT2).getTotalCount();
        Long id = createInbound(WAREHOUSE_DEPT2, "30", "10");
        assertThat(statisticsService.getInboundStatistics(today, today, WAREHOUSE_DEPT2).getTotalCount())
                .isEqualTo(before + 1);

        inboundService.voidInbound(id, "录错");

        assertThat(statisticsService.getInboundStatistics(today, today, WAREHOUSE_DEPT2).getTotalCount())
                .isEqualTo(before);
    }
}
