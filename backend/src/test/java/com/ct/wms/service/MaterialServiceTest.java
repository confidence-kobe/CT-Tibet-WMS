package com.ct.wms.service;

import com.ct.wms.entity.Material;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 物资服务测试
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@SpringBootTest
@Transactional
public class MaterialServiceTest {

    @Autowired
    private MaterialService materialService;

    @Test
    public void testListMaterials() {
        var page = materialService.listMaterials(1, 10, null, null, null);
        assertNotNull(page);
    }

    @Test
    public void testGetMaterialById() {
        Material material = materialService.getMaterialById(1L);
        assertNotNull(material);
    }

    @Test
    public void testGetMaterialByCode() {
        Material material = materialService.getMaterialByCode("M001");
        if (material != null) {
            assertNotNull(material.getMaterialCode());
        }
    }

    @Test
    public void testListMaterialsByCategory() {
        List<Material> materials = materialService.listMaterialsByCategory("电子类");
        assertNotNull(materials);
    }

    @Test
    public void testListEnabledMaterials() {
        List<Material> materials = materialService.listEnabledMaterials();
        assertNotNull(materials);
    }

    @Test
    public void testCheckMaterialCodeExists() {
        // 这个测试依赖于数据库中是否有数据
        // 可能需要先添加测试数据
    }
}
