package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ct.wms.dto.InboundDTO;
import com.ct.wms.entity.Inbound;
import com.ct.wms.entity.Material;
import com.ct.wms.entity.Warehouse;
import com.ct.wms.mapper.InboundMapper;
import com.ct.wms.mapper.MaterialMapper;
import com.ct.wms.mapper.WarehouseMapper;
import com.ct.wms.service.InboundService;
import com.ct.wms.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 入库服务测试
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@SpringBootTest
@Transactional
public class InboundServiceImplTest {

    @Autowired
    private InboundService inboundService;

    @Autowired
    private InboundMapper inboundMapper;

    @Autowired
    private MaterialMapper materialMapper;

    @Autowired
    private WarehouseMapper warehouseMapper;

    @Autowired
    private InventoryService inventoryService;

    private Long testWarehouseId;
    private Long testMaterialId;

    @BeforeEach
    public void setUp() {
        // 查找测试数据
        LambdaQueryWrapper<Warehouse> warehouseWrapper = new LambdaQueryWrapper<>();
        warehouseWrapper.last("LIMIT 1");
        Warehouse warehouse = warehouseMapper.selectOne(warehouseWrapper);
        if (warehouse != null) {
            testWarehouseId = warehouse.getId();
        }

        LambdaQueryWrapper<Material> materialWrapper = new LambdaQueryWrapper<>();
        materialWrapper.last("LIMIT 1");
        Material material = materialMapper.selectOne(materialWrapper);
        if (material != null) {
            testMaterialId = material.getId();
        }
    }

    /**
     * 测试创建入库单
     */
    @Test
    public void testCreateInbound() {
        if (testWarehouseId == null || testMaterialId == null) {
            return;
        }

        InboundDTO dto = new InboundDTO();
        dto.setWarehouseId(testWarehouseId);
        dto.setInboundType(1); // 采购入库
        dto.setInboundTime(LocalDateTime.now());
        dto.setRemark("测试入库单");

        // 添加明细
        List<InboundDTO.InboundDetailDTO> details = new ArrayList<>();
        InboundDTO.InboundDetailDTO detail = new InboundDTO.InboundDetailDTO();
        detail.setMaterialId(testMaterialId);
        detail.setQuantity(BigDecimal.valueOf(100));
        detail.setUnitPrice(BigDecimal.valueOf(10.5));
        detail.setRemark("测试明细");
        details.add(detail);
        dto.setDetails(details);

        // 创建入库单
        Long inboundId = inboundService.createInbound(dto);
        assertNotNull(inboundId);

        // 验证入库单
        Inbound inbound = inboundMapper.selectById(inboundId);
        assertNotNull(inbound);
        assertNotNull(inbound.getInboundNo());
        assertEquals(testWarehouseId, inbound.getWarehouseId());
    }

    /**
     * 测试查询入库单列表
     */
    @Test
    public void testListInbounds() {
        var page = inboundService.listInbounds(1, 10, null, null, null, null, null, null);
        assertNotNull(page);
        assertTrue(page.getTotal() >= 0);
    }

    /**
     * 测试获取入库单详情
     */
    @Test
    public void testGetInboundById() {
        // 先创建入库单
        if (testWarehouseId != null && testMaterialId != null) {
            InboundDTO dto = new InboundDTO();
            dto.setWarehouseId(testWarehouseId);
            dto.setInboundType(1);
            dto.setInboundTime(LocalDateTime.now());
            dto.setRemark("测试详情");

            List<InboundDTO.InboundDetailDTO> details = new ArrayList<>();
            InboundDTO.InboundDetailDTO detail = new InboundDTO.InboundDetailDTO();
            detail.setMaterialId(testMaterialId);
            detail.setQuantity(BigDecimal.valueOf(50));
            detail.setUnitPrice(BigDecimal.valueOf(20));
            details.add(detail);
            dto.setDetails(details);

            Long inboundId = inboundService.createInbound(dto);

            // 查询详情
            Inbound inbound = inboundService.getInboundById(inboundId);
            assertNotNull(inbound);
            assertNotNull(inbound.getDetails());
            assertTrue(inbound.getDetails().size() > 0);
        }
    }

    /**
     * 测试入库后库存增加
     */
    @Test
    public void testInventoryIncreaseAfterInbound() {
        if (testWarehouseId == null || testMaterialId == null) {
            return;
        }

        // 获取入库前库存
        var beforeInventory = inventoryService.getInventory(testWarehouseId, testMaterialId);
        BigDecimal beforeQuantity = beforeInventory != null ? beforeInventory.getQuantity() : BigDecimal.ZERO;

        // 创建入库单
        InboundDTO dto = new InboundDTO();
        dto.setWarehouseId(testWarehouseId);
        dto.setInboundType(1);
        dto.setInboundTime(LocalDateTime.now());

        List<InboundDTO.InboundDetailDTO> details = new ArrayList<>();
        InboundDTO.InboundDetailDTO detail = new InboundDTO.InboundDetailDTO();
        detail.setMaterialId(testMaterialId);
        detail.setQuantity(BigDecimal.valueOf(30));
        detail.setUnitPrice(BigDecimal.valueOf(15));
        details.add(detail);
        dto.setDetails(details);

        inboundService.createInbound(dto);

        // 验证库存增加
        var afterInventory = inventoryService.getInventory(testWarehouseId, testMaterialId);
        assertNotNull(afterInventory);
        assertEquals(beforeQuantity.add(BigDecimal.valueOf(30)), afterInventory.getQuantity());
    }
}
