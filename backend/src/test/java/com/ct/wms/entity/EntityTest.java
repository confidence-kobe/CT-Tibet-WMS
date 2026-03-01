package com.ct.wms.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 实体类测试
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
public class EntityTest {

    @Test
    public void testUserEntity() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setRealName("管理员");
        user.setPhone("13800000000");
        user.setDeptId(1L);
        user.setRoleId(1L);
        
        assertEquals(1L, user.getId());
        assertEquals("admin", user.getUsername());
        assertEquals("管理员", user.getRealName());
    }

    @Test
    public void testWarehouseEntity() {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);
        warehouse.setWarehouseName("测试仓库");
        warehouse.setWarehouseCode("WH001");
        warehouse.setDeptId(1L);
        
        assertEquals("测试仓库", warehouse.getWarehouseName());
        assertEquals("WH001", warehouse.getWarehouseCode());
    }

    @Test
    public void testMaterialEntity() {
        Material material = new Material();
        material.setId(1L);
        material.setMaterialName("测试物资");
        material.setMaterialCode("M001");
        material.setCategory("电子类");
        material.setSpec("10*10");
        material.setUnit("个");
        material.setPrice(new java.math.BigDecimal("100.00"));
        material.setMinStock(10);
        
        assertEquals("测试物资", material.getMaterialName());
        assertEquals("M001", material.getMaterialCode());
    }

    @Test
    public void testInventoryEntity() {
        Inventory inventory = new Inventory();
        inventory.setId(1L);
        inventory.setWarehouseId(1L);
        inventory.setMaterialId(1L);
        inventory.setQuantity(new java.math.BigDecimal("100"));
        
        assertEquals(1L, inventory.getWarehouseId());
        assertEquals(1L, inventory.getMaterialId());
    }

    @Test
    public void testApplyEntity() {
        Apply apply = new Apply();
        apply.setId(1L);
        apply.setApplyNo("SQ_TEST_001");
        apply.setWarehouseId(1L);
        apply.setApplicantId(1L);
        
        assertEquals("SQ_TEST_001", apply.getApplyNo());
    }

    @Test
    public void testOutboundEntity() {
        Outbound outbound = new Outbound();
        outbound.setId(1L);
        outbound.setOutboundNo("CK_TEST_001");
        outbound.setWarehouseId(1L);
        
        assertEquals("CK_TEST_001", outbound.getOutboundNo());
    }

    @Test
    public void testInboundEntity() {
        Inbound inbound = new Inbound();
        inbound.setId(1L);
        inbound.setInboundNo("RK_TEST_001");
        inbound.setWarehouseId(1L);
        
        assertEquals("RK_TEST_001", inbound.getInboundNo());
    }
}
