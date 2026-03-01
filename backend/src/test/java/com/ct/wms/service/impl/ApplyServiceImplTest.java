package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ct.wms.dto.ApplyDTO;
import com.ct.wms.entity.Apply;
import com.ct.wms.entity.Material;
import com.ct.wms.entity.Warehouse;
import com.ct.wms.mapper.ApplyMapper;
import com.ct.wms.mapper.MaterialMapper;
import com.ct.wms.mapper.WarehouseMapper;
import com.ct.wms.service.ApplyService;
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
 * 申请服务测试
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@SpringBootTest
@Transactional
public class ApplyServiceImplTest {

    @Autowired
    private ApplyService applyService;

    @Autowired
    private ApplyMapper applyMapper;

    @Autowired
    private MaterialMapper materialMapper;

    @Autowired
    private WarehouseMapper warehouseMapper;

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
     * 测试创建申请单
     */
    @Test
    public void testCreateApply() {
        if (testWarehouseId == null || testMaterialId == null) {
            return;
        }

        ApplyDTO dto = new ApplyDTO();
        dto.setWarehouseId(testWarehouseId);
        dto.setApplyTime(LocalDateTime.now());
        dto.setApplyReason("测试申请");

        // 添加明细
        List<ApplyDTO.ApplyDetailDTO> details = new ArrayList<>();
        ApplyDTO.ApplyDetailDTO detail = new ApplyDTO.ApplyDetailDTO();
        detail.setMaterialId(testMaterialId);
        detail.setQuantity(BigDecimal.valueOf(5));
        detail.setRemark("测试明细");
        details.add(detail);
        dto.setDetails(details);

        // 创建申请单
        Long applyId = applyService.createApply(dto);
        assertNotNull(applyId);

        // 验证申请单
        Apply apply = applyMapper.selectById(applyId);
        assertNotNull(apply);
        assertNotNull(apply.getApplyNo());
    }

    /**
     * 测试查询申请单列表
     */
    @Test
    public void testListApplies() {
        var page = applyService.listApplies(1, 10, null, null, null, null, null, null, null);
        assertNotNull(page);
        assertTrue(page.getTotal() >= 0);
    }

    /**
     * 测试获取申请单详情
     */
    @Test
    public void testGetApplyById() {
        // 先创建申请单
        if (testWarehouseId != null && testMaterialId != null) {
            ApplyDTO dto = new ApplyDTO();
            dto.setWarehouseId(testWarehouseId);
            dto.setApplyTime(LocalDateTime.now());
            dto.setApplyReason("测试详情");

            List<ApplyDTO.ApplyDetailDTO> details = new ArrayList<>();
            ApplyDTO.ApplyDetailDTO detail = new ApplyDTO.ApplyDetailDTO();
            detail.setMaterialId(testMaterialId);
            detail.setQuantity(BigDecimal.valueOf(3));
            details.add(detail);
            dto.setDetails(details);

            Long applyId = applyService.createApply(dto);

            // 查询详情
            Apply apply = applyService.getApplyById(applyId);
            assertNotNull(apply);
            assertNotNull(apply.getDetails());
            assertTrue(apply.getDetails().size() > 0);
        }
    }

    /**
     * 测试查询我的申请
     */
    @Test
    public void testListMyApplies() {
        var page = applyService.listMyApplies(1, 10, null);
        assertNotNull(page);
    }

    /**
     * 测试查询待审批申请
     */
    @Test
    public void testListPendingApplies() {
        var page = applyService.listPendingApplies(1, 10);
        assertNotNull(page);
    }

    /**
     * 测试申请单号生成
     */
    @Test
    public void testApplyNoGeneration() {
        if (testWarehouseId == null || testMaterialId == null) {
            return;
        }

        ApplyDTO dto = new ApplyDTO();
        dto.setWarehouseId(testWarehouseId);
        dto.setApplyTime(LocalDateTime.now());
        dto.setApplyReason("测试单号");

        List<ApplyDTO.ApplyDetailDTO> details = new ArrayList<>();
        ApplyDTO.ApplyDetailDTO detail = new ApplyDTO.ApplyDetailDTO();
        detail.setMaterialId(testMaterialId);
        detail.setQuantity(BigDecimal.valueOf(1));
        details.add(detail);
        dto.setDetails(details);

        Long applyId = applyService.createApply(dto);
        Apply apply = applyMapper.selectById(applyId);

        // 验证单号格式: SQ_部门编码_YYYYMMDD_流水号
        assertNotNull(apply.getApplyNo());
        assertTrue(apply.getApplyNo().startsWith("SQ_"));
    }
}
