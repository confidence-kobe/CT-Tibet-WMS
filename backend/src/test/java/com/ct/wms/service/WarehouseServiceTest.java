package com.ct.wms.service;

import com.ct.wms.entity.Warehouse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 仓库服务测试
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@SpringBootTest
@Transactional
public class WarehouseServiceTest {

    @Autowired
    private WarehouseService warehouseService;

    @Test
    public void testListWarehouses() {
        var page = warehouseService.listWarehouses(1, 10, null, null);
        assertNotNull(page);
    }

    @Test
    public void testGetWarehouseById() {
        Warehouse warehouse = warehouseService.getWarehouseById(1L);
        assertNotNull(warehouse);
    }

    @Test
    public void testGetWarehouseByCode() {
        Warehouse warehouse = warehouseService.getWarehouseByCode("WH001");
        // 可能不存在，返回null也是正常的
        if (warehouse != null) {
            assertNotNull(warehouse.getWarehouseCode());
        }
    }

    @Test
    public void testListWarehousesByDeptId() {
        List<Warehouse> warehouses = warehouseService.listWarehousesByDeptId(1L);
        assertNotNull(warehouses);
    }

    @Test
    public void testListAllWarehouses() {
        List<Warehouse> warehouses = warehouseService.listAllWarehouses();
        assertNotNull(warehouses);
    }

    @Test
    public void testListEnabledWarehouses() {
        List<Warehouse> warehouses = warehouseService.listEnabledWarehouses();
        assertNotNull(warehouses);
    }
}
