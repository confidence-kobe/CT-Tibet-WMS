package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ct.wms.dto.OutboundDTO;
import com.ct.wms.entity.InventoryLog;
import com.ct.wms.entity.Outbound;
import com.ct.wms.mapper.InventoryLogMapper;
import com.ct.wms.mapper.OutboundMapper;
import com.ct.wms.service.OutboundService;
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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 直接出库：领取人信息与库存流水操作人
 *
 * 测试数据（data.sql）：用户3是部门2的仓管员（Warehouse Mgr），仓库1属于部门2，物资1有库存
 *
 * @author CT Development Team
 * @since 2026-11-12
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("直接出库领取人测试")
class OutboundDirectReceiverTest {

    @Autowired
    private OutboundService outboundService;

    @Autowired
    private OutboundMapper outboundMapper;

    @Autowired
    private InventoryLogMapper inventoryLogMapper;

    @BeforeEach
    void setUp() {
        TestDataBuilder.mockSecurityContext(3L, "warehouse", "WAREHOUSE");
    }

    @AfterEach
    void tearDown() {
        TestDataBuilder.clearSecurityContext();
    }

    private OutboundDTO dto() {
        OutboundDTO dto = new OutboundDTO();
        dto.setWarehouseId(1L);
        dto.setOutboundType(1);
        dto.setOutboundTime(LocalDateTime.now());
        OutboundDTO.OutboundDetailDTO detail = new OutboundDTO.OutboundDetailDTO();
        detail.setMaterialId(1L);
        detail.setQuantity(BigDecimal.ONE);
        dto.setDetails(Collections.singletonList(detail));
        return dto;
    }

    @Test
    @DisplayName("手工填写的领取人姓名和电话会保存")
    void typedReceiver_saved() {
        OutboundDTO dto = dto();
        dto.setReceiverName("张强");
        dto.setReceiverPhone("13800000003");

        Outbound outbound = outboundMapper.selectById(outboundService.createOutboundDirect(dto));

        assertThat(outbound.getReceiverName()).isEqualTo("张强");
        assertThat(outbound.getReceiverPhone()).isEqualTo("13800000003");
    }

    @Test
    @DisplayName("未填写领取人时视为仓管员本人领用")
    void noReceiver_defaultsToOperator() {
        Outbound outbound = outboundMapper.selectById(outboundService.createOutboundDirect(dto()));

        assertThat(outbound.getReceiverId()).isEqualTo(3L);
        assertThat(outbound.getReceiverName()).isEqualTo("Warehouse Mgr");
    }

    @Test
    @DisplayName("锁定库存的流水记录当前操作人")
    void lockLog_recordsCurrentOperator() {
        outboundService.createOutboundDirect(dto());

        List<InventoryLog> lockLogs = inventoryLogMapper.selectList(new LambdaQueryWrapper<InventoryLog>()
                .eq(InventoryLog::getRemark, "锁定库存"));
        assertThat(lockLogs).isNotEmpty();
        assertThat(lockLogs).allSatisfy(l -> assertThat(l.getOperatorId()).isEqualTo(3L));
    }
}
