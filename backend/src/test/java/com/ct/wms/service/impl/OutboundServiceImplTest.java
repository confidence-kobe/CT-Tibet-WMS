package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ct.wms.dto.OutboundDTO;
import com.ct.wms.entity.Material;
import com.ct.wms.entity.Outbound;
import com.ct.wms.entity.Warehouse;
import com.ct.wms.mapper.MaterialMapper;
import com.ct.wms.mapper.OutboundMapper;
import com.ct.wms.mapper.WarehouseMapper;
import com.ct.wms.service.InventoryService;
import com.ct.wms.service.OutboundService;
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
 * 出库服务测试
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@SpringBootTest
@Transactional
public class OutboundServiceImplTest {

    @Autowired
    private OutboundService outboundService;

    @Autowired
    private OutboundMapper outboundMapper;

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

        // 确保有足够库存
        if (testWarehouseId != null && testMaterialId != null) {
            try {
                inventoryService.increaseInventory(
                        testWarehouseId,
                        testMaterialId,
                        BigDecimal.valueOf(1000),
                        "RK_TEST_OUTBOUND",
                        1L,
                        1L
                );
            } catch (Exception e) {
                // 忽略库存已存在错误
            }
        }
    }

    /**
     * 测试创建直接出库单
     */
    @Test
    public void testCreateOutboundDirect() {
        if (testWarehouseId == null || testMaterialId == null) {
            return;
        }

        OutboundDTO dto = new OutboundDTO();
        dto.setWarehouseId(testWarehouseId);
        dto.setOutboundType(1); // 领用
        dto.setOutboundTime(LocalDateTime.now());
        dto.setReceiverId(1L);
        dto.setRemark("测试直接出库");

        // 添加明细
        List<OutboundDTO.OutboundDetailDTO> details = new ArrayList<>();
        OutboundDTO.OutboundDetailDTO detail = new OutboundDTO.OutboundDetailDTO();
        detail.setMaterialId(testMaterialId);
        detail.setQuantity(BigDecimal.valueOf(10));
        detail.setUnitPrice(BigDecimal.valueOf(5.0));
        detail.setRemark("测试明细");
        details.add(detail);
        dto.setDetails(details);

        // 创建出库单
        Long outboundId = outboundService.createOutboundDirect(dto);
        assertNotNull(outboundId);

        // 验证出库单
        Outbound outbound = outboundMapper.selectById(outboundId);
        assertNotNull(outbound);
        assertNotNull(outbound.getOutboundNo());
        assertTrue(outbound.getOutboundNo().startsWith("CK_"));
    }

    /**
     * 测试查询出库单列表
     */
    @Test
    public void testListOutbounds() {
        var page = outboundService.listOutbounds(1, 10, null, null, null, null, null, null, null);
        assertNotNull(page);
        assertTrue(page.getTotal() >= 0);
    }

    /**
     * 测试获取出库单详情
     */
    @Test
    public void testGetOutboundById() {
        // 先创建出库单
        if (testWarehouseId != null && testMaterialId != null) {
            OutboundDTO dto = new OutboundDTO();
            dto.setWarehouseId(testWarehouseId);
            dto.setOutboundType(1);
            dto.setOutboundTime(LocalDateTime.now());
            dto.setReceiverId(1L);

            List<OutboundDTO.OutboundDetailDTO> details = new ArrayList<>();
            OutboundDTO.OutboundDetailDTO detail = new OutboundDTO.OutboundDetailDTO();
            detail.setMaterialId(testMaterialId);
            detail.setQuantity(BigDecimal.valueOf(5));
            detail.setUnitPrice(BigDecimal.valueOf(10));
            details.add(detail);
            dto.setDetails(details);

            Long outboundId = outboundService.createOutboundDirect(dto);

            // 查询详情
            Outbound outbound = outboundService.getOutboundById(outboundId);
            assertNotNull(outbound);
            assertNotNull(outbound.getDetails());
        }
    }

    /**
     * 测试出库单号生成
     */
    @Test
    public void testOutboundNoGeneration() {
        if (testWarehouseId == null || testMaterialId == null) {
            return;
        }

        OutboundDTO dto = new OutboundDTO();
        dto.setWarehouseId(testWarehouseId);
        dto.setOutboundType(1);
        dto.setOutboundTime(LocalDateTime.now());
        dto.setReceiverId(1L);

        List<OutboundDTO.OutboundDetailDTO> details = new ArrayList<>();
        OutboundDTO.OutboundDetailDTO detail = new OutboundDTO.OutboundDetailDTO();
        detail.setMaterialId(testMaterialId);
        detail.setQuantity(BigDecimal.valueOf(1));
        details.add(detail);
        dto.setDetails(details);

        Long outboundId = outboundService.createOutboundDirect(dto);
        Outbound outbound = outboundMapper.selectById(outboundId);

        // 验证单号格式: CK_部门编码_YYYYMMDD_流水号
        assertNotNull(outbound.getOutboundNo());
        assertTrue(outbound.getOutboundNo().startsWith("CK_"));
    }

    /**
     * 测试库存扣减
     */
    @Test
    public void testInventoryDecreaseAfterOutbound() {
        if (testWarehouseId == null || testMaterialId == null) {
            return;
        }

        // 获取出库前库存
        var beforeInventory = inventoryService.getInventory(testWarehouseId, testMaterialId);
        BigDecimal beforeQuantity = beforeInventory != null ? beforeInventory.getQuantity() : BigDecimal.ZERO;

        // 创建出库单
        OutboundDTO dto = new OutboundDTO();
        dto.setWarehouseId(testWarehouseId);
        dto.setOutboundType(1);
        dto.setOutboundTime(LocalDateTime.now());
        dto.setReceiverId(1L);

        List<OutboundDTO.OutboundDetailDTO> details = new ArrayList<>();
        OutboundDTO.OutboundDetailDTO detail = new OutboundDTO.OutboundDetailDTO();
        detail.setMaterialId(testMaterialId);
        detail.setQuantity(BigDecimal.valueOf(20));
        detail.setUnitPrice(BigDecimal.valueOf(8));
        details.add(detail);
        dto.setDetails(details);

        outboundService.createOutboundDirect(dto);

        // 验证库存扣减
        var afterInventory = inventoryService.getInventory(testWarehouseId, testMaterialId);
        assertNotNull(afterInventory);
        assertEquals(beforeQuantity.subtract(BigDecimal.valueOf(20)), afterInventory.getQuantity());
    }

    /**
     * 测试库存不足时出库失败
     */
    @Test
    public void testOutboundFailsWithInsufficientInventory() {
        if (testWarehouseId == null || testMaterialId == null) {
            return;
        }

        OutboundDTO dto = new OutboundDTO();
        dto.setWarehouseId(testWarehouseId);
        dto.setOutboundType(1);
        dto.setOutboundTime(LocalDateTime.now());
        dto.setReceiverId(1L);

        List<OutboundDTO.OutboundDetailDTO> details = new ArrayList<>();
        OutboundDTO.OutboundDetailDTO detail = new OutboundDTO.OutboundDetailDTO();
        detail.setMaterialId(testMaterialId);
        detail.setQuantity(BigDecimal.valueOf(999999)); // 远超库存
        details.add(detail);
        dto.setDetails(details);

        // 应该抛出异常
        assertThrows(Exception.class, () -> {
            outboundService.createOutboundDirect(dto);
        });
    }
}
