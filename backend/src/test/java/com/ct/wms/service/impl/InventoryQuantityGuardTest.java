package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ct.wms.common.exception.BusinessException;
import com.ct.wms.dto.OutboundDTO;
import com.ct.wms.entity.Inventory;
import com.ct.wms.entity.Material;
import com.ct.wms.entity.User;
import com.ct.wms.entity.Warehouse;
import com.ct.wms.mapper.MaterialMapper;
import com.ct.wms.mapper.UserMapper;
import com.ct.wms.mapper.WarehouseMapper;
import com.ct.wms.service.InventoryService;
import com.ct.wms.service.OutboundService;
import com.ct.wms.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validation;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 回归测试：锁定/解锁/提交库存必须拒绝非正数数量。
 * <p>
 * 2026-09-29 审查发现（W2 评审报告 #1）：OutboundDTO 明细数量只有 @NotNull，
 * lockInventory/commitInventory 也不校验正负——直接出库传 -100，库存反而凭空增加 100，流水还记成“出库”。
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("库存数量正负防护")
class InventoryQuantityGuardTest {

    @Autowired private InventoryService inventoryService;
    @Autowired private OutboundService outboundService;
    @Autowired private UserMapper userMapper;
    @Autowired private WarehouseMapper warehouseMapper;
    @Autowired private MaterialMapper materialMapper;

    private Long warehouseId;
    private Long materialId;
    private Long operatorId;

    @BeforeEach
    void setUp() {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().last("LIMIT 1"));
        operatorId = user.getId();
        TestDataBuilder.mockSecurityContext(user.getId(), user.getUsername(), "ADMIN");
        Warehouse warehouse = warehouseMapper.selectOne(new LambdaQueryWrapper<Warehouse>().last("LIMIT 1"));
        Material material = materialMapper.selectOne(new LambdaQueryWrapper<Material>().last("LIMIT 1"));
        warehouseId = warehouse.getId();
        materialId = material.getId();
        // 保证有 100 可用库存
        inventoryService.increaseInventory(warehouseId, materialId, new BigDecimal("100"), "TEST", null, operatorId);
    }

    private Inventory current() {
        return inventoryService.getInventory(warehouseId, materialId);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-100", "0"})
    @DisplayName("锁定库存：非正数量被拒绝，库存不变")
    void lockRejectsNonPositive(String qty) {
        Inventory before = current();
        assertThatThrownBy(() -> inventoryService.lockInventory(warehouseId, materialId, new BigDecimal(qty)))
                .isInstanceOf(BusinessException.class);
        Inventory after = current();
        assertThat(after.getLockedQuantity()).isEqualByComparingTo(before.getLockedQuantity());
        assertThat(after.getAvailableQuantity()).isEqualByComparingTo(before.getAvailableQuantity());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-50", "0"})
    @DisplayName("解锁、提交库存：非正数量被拒绝")
    void unlockAndCommitRejectNonPositive(String qty) {
        BigDecimal q = new BigDecimal(qty);
        assertThatThrownBy(() -> inventoryService.unlockInventory(warehouseId, materialId, q))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> inventoryService.commitInventory(warehouseId, materialId, q, "T", null, operatorId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("直接出库传负数：不能凭空增加库存（原漏洞的端到端复现）")
    void directOutboundWithNegativeQuantityCannotCreateStock() {
        BigDecimal quantityBefore = current().getQuantity();

        OutboundDTO.OutboundDetailDTO d = new OutboundDTO.OutboundDetailDTO();
        d.setMaterialId(materialId);
        d.setQuantity(new BigDecimal("-100"));
        OutboundDTO dto = new OutboundDTO();
        dto.setWarehouseId(warehouseId);
        dto.setOutboundType(1);
        dto.setOutboundTime(LocalDateTime.now());
        dto.setDetails(List.of(d));

        assertThatThrownBy(() -> outboundService.createOutboundDirect(dto)).isInstanceOf(BusinessException.class);
        assertThat(current().getQuantity()).isEqualByComparingTo(quantityBefore);
    }

    @Test
    @DisplayName("正常路径：锁定 30 → 可用 -30；提交 30 → 总量 -30、锁定归零")
    void lockThenCommitHappyPath() {
        Inventory before = current();
        assertThat(inventoryService.lockInventory(warehouseId, materialId, new BigDecimal("30"))).isTrue();
        assertThat(current().getAvailableQuantity())
                .isEqualByComparingTo(before.getAvailableQuantity().subtract(new BigDecimal("30")));
        inventoryService.commitInventory(warehouseId, materialId, new BigDecimal("30"), "T", null, operatorId);
        Inventory after = current();
        assertThat(after.getQuantity()).isEqualByComparingTo(before.getQuantity().subtract(new BigDecimal("30")));
        assertThat(after.getLockedQuantity()).isEqualByComparingTo(before.getLockedQuantity());
    }

    @Test
    @DisplayName("接口层：OutboundDTO 明细数量必须大于 0")
    void outboundDetailDtoRejectsNonPositiveQuantity() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        OutboundDTO.OutboundDetailDTO d = new OutboundDTO.OutboundDetailDTO();
        d.setMaterialId(1L);
        d.setQuantity(new BigDecimal("-1"));
        assertThat(validator.validate(d)).isNotEmpty();
        d.setQuantity(BigDecimal.ZERO);
        assertThat(validator.validate(d)).isNotEmpty();
        d.setQuantity(new BigDecimal("0.5"));
        assertThat(validator.validate(d)).isEmpty();
    }
}
