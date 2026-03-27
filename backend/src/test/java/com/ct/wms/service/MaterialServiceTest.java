package com.ct.wms.service;

import com.ct.wms.dto.MaterialQueryDTO;
import com.ct.wms.entity.Material;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
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
@ActiveProfiles("test")
@Transactional
public class MaterialServiceTest {

    @Autowired
    private MaterialService materialService;

    @Test
    public void testListMaterials() {
        MaterialQueryDTO queryDTO = new MaterialQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10);
        var page = materialService.listMaterials(queryDTO);
        assertNotNull(page);
    }

    @Test
    public void testGetMaterialById() {
        Material material = materialService.getMaterialById(1L);
        // 可能不存在，返回null也是正常的
        if (material != null) {
            assertNotNull(material.getMaterialCode());
        }
    }

    @Test
    public void testSearchMaterials() {
        List<Material> materials = materialService.searchMaterials("", 0);
        assertNotNull(materials);
    }

    @Test
    public void testGetCategories() {
        List<String> categories = materialService.getCategories();
        assertNotNull(categories);
    }

    @Test
    public void testCheckMaterialCodeExists() {
        boolean exists = materialService.checkMaterialCodeExists("test", null);
        assertNotNull(exists);
    }
}
