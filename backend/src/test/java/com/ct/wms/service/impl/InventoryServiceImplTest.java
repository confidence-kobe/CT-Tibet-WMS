package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ct.wms.entity.Inventory;
import com.ct.wms.entity.InventoryLog;
import com.ct.wms.entity.Material;
import com.ct.wms.entity.Warehouse;
import com.ct.wms.mapper.InventoryLogMapper;
import com.ct.wms.mapper.InventoryMapper;
import com.ct.wms.mapper.MaterialMapper;
import com.ct.wms.mapper.WarehouseMapper;
import com.ct.wms.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 库存服务测试
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@SpringBootTest
@Transactional
public class InventoryServiceImplTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private InventoryLogMapper inventoryLogMapper;

    @Autowired
    private MaterialMapper materialMapper;

    @Autowired
    private WarehouseMapper warehouseMapper;

    private Long testWarehouseId;
    private Long testMaterialId;

    @BeforeEach
    public void setUp() {
        // 查找测试数据和仓库
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
     * 测试增加库存
     */
    @Test
    public void testIncreaseInventory() {
        if (testWarehouseId == null || testMaterialId == null) {
            return;
        }

        BigDecimal initialQuantity = BigDecimal.valueOf(100);
        BigDecimal increaseQuantity = BigDecimal.valueOf(50);

        // 增加库存
        inventoryService.increaseInventory(
                testWarehouseId,
                testMaterialId,
                increaseQuantity,
                "RK_TEST_001",
                1L,
                1L
        );

        // 验证库存记录
        Inventory inventory = inventoryService.getInventory(testWarehouseId, testMaterialId);
        assertNotNull(inventory);
        assertEquals(initialQuantity.add(increaseQuantity), inventory.getQuantity());
    }

    /**
     * 测试减少库存
     */
    @Test
    public void testDecreaseInventory() {
        if (testWarehouseId == null || testMaterialId == null) {
            return;
        }

        BigDecimal decreaseQuantity = BigDecimal.valueOf(30);

        // 先增加库存
        inventoryService.increaseInventory(
                testWarehouseId,
                testMaterialId,
                BigDecimal.valueOf(100),
                "RK_TEST_002",
                1L,
                1L
        );

        // 减少库存
        inventoryService.decreaseInventory(
                testWarehouseId,
                testMaterialId,
                decreaseQuantity,
                "CK_TEST_001",
                1L,
                1L
        );

        // 验证库存记录
        Inventory inventory = inventoryService.getInventory(testWarehouseId, testMaterialId);
        assertNotNull(inventory);
        assertTrue(inventory.getQuantity().compareTo(BigDecimal.valueOf(70)) >= 0);
    }

    /**
     * 测试库存不足
     */
    @Test
    public void testInsufficientInventory() {
        if (testWarehouseId == null || testMaterialId == null) {
            return;
        }

        // 确保库存较少
        Inventory inventory = inventoryService.getInventory(testWarehouseId, testMaterialId);
        if (inventory == null) {
            inventoryService.increaseInventory(
                    testWarehouseId,
                    testMaterialId,
                    BigDecimal.valueOf(10),
                    "RK_TEST_003",
                    1L,
                    1L
            );
        }

        // 尝试减少超过库存的数量
        assertThrows(Exception.class, () -> {
            inventoryService.decreaseInventory(
                    testWarehouseId,
                    testMaterialId,
                    BigDecimal.valueOf(999999),
                    "CK_TEST_002",
                    1L,
                    1L
            );
        });
    }

    /**
     * 测试负数数量校验
     */
    @Test
    public void testNegativeQuantityValidation() {
        if (testWarehouseId == null || testMaterialId == null) {
            return;
        }

        // 尝试使用负数增加库存
        assertThrows(Exception.class, () -> {
            inventoryService.increaseInventory(
                    testWarehouseId,
                    testMaterialId,
                    BigDecimal.valueOf(-10),
                    "RK_TEST_004",
                    1L,
                    1L
            );
        });
    }

    /**
     * 测试检查库存
     */
    @Test
    public void testCheckInventory() {
        if (testWarehouseId == null || testMaterialId == null) {
            return;
        }

        // 先增加库存
        inventoryService.increaseInventory(
                testWarehouseId,
                testMaterialId,
                BigDecimal.valueOf(100),
                "RK_TEST_005",
                1L,
                1L
        );

        // 检查库存充足
        assertTrue(inventoryService.checkInventory(testWarehouseId, testMaterialId, BigDecimal.valueOf(50)));
        assertTrue(inventoryService.checkInventory(testWarehouseId, testMaterialId, BigDecimal.valueOf(100)));
        assertFalse(inventoryService.checkInventory(testWarehouseId, testMaterialId, BigDecimal.valueOf(150)));
    }

    /**
     * 测试库存流水记录
     */
    @Test
    public void testInventoryLog() {
        if (testWarehouseId == null || testMaterialId == null) {
            return;
        }

        // 增加库存
        inventoryService.increaseInventory(
                testWarehouseId,
                testMaterialId,
                BigDecimal.valueOf(50),
                "RK_TEST_006",
                1L,
                1L
        );

        // 查询流水记录
        LambdaQueryWrapper<InventoryLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryLog::getWarehouseId, testWarehouseId);
        wrapper.eq(InventoryLog::getMaterialId, testMaterialId);
        wrapper.orderByDesc(InventoryLog::getCreateTime);
        wrapper.last("LIMIT 1");

        InventoryLog log = inventoryLogMapper.selectOne(wrapper);
        assertNotNull(log);
        assertEquals(Integer.valueOf(1), log.getChangeType()); // 1=入库
    }

    /**
     * 测试库存列表查询
     */
    @Test
    public void testListInventories() {
        var page = inventoryService.listInventories(1, 10, testWarehouseId, null, null);
        assertNotNull(page);
        assertTrue(page.getTotal() >= 0);
    }

    /**
     * 测试低库存预警
     */
    @Test
    public void testLowStockAlerts() {
        List<Inventory> alerts = inventoryService.listLowStockAlerts(testWarehouseId);
        assertNotNull(alerts);
    }
}
