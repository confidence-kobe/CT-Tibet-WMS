package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ct.wms.dto.ApplyDTO;
import com.ct.wms.dto.ApprovalDTO;
import com.ct.wms.entity.InventoryLog;
import com.ct.wms.mapper.InventoryLogMapper;
import com.ct.wms.service.ApplyService;
import com.ct.wms.service.InventoryService;
import com.ct.wms.util.TestDataBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 回归测试（2026-09-29 W2 评审）：
 * #5 多行加锁按 materialId 统一顺序，避免明细顺序相反的并发请求死锁；
 * #6 锁定流水记录实际锁定数量（原先固定写 0，无法对账）。
 * 种子数据：仓库 1 属部门 2、仓管员为用户 3；用户 4 是部门 2 的员工。
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("加锁顺序与锁定流水")
class LockOrderAndAuditTest {

    private static final long WH = 1L;
    private static final long M1 = 1L;
    private static final long M2 = 2L;

    @Autowired private ApplyService applyService;
    @Autowired private InventoryService inventoryService;
    @Autowired private InventoryLogMapper inventoryLogMapper;

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private static ApplyDTO.ApplyDetailDTO line(long materialId, String qty) {
        ApplyDTO.ApplyDetailDTO d = new ApplyDTO.ApplyDetailDTO();
        d.setMaterialId(materialId);
        d.setQuantity(new BigDecimal(qty));
        return d;
    }

    @Test
    @DisplayName("明细顺序为 [物资2, 物资1] 时，锁定按 物资1 → 物资2 进行，流水记录实际数量")
    void approvalLocksInMaterialIdOrderAndLogsQuantity() {
        TestDataBuilder.mockSecurityContext(1L, "admin", "ADMIN");
        inventoryService.increaseInventory(WH, M1, new BigDecimal("100"), "T", null, 1L);
        inventoryService.increaseInventory(WH, M2, new BigDecimal("100"), "T", null, 1L);

        TestDataBuilder.mockSecurityContext(4L, "employee1", "USER");
        ApplyDTO apply = new ApplyDTO();
        apply.setWarehouseId(WH);
        apply.setApplyReason("加锁顺序测试");
        apply.setDetails(List.of(line(M2, "3"), line(M1, "5")));   // 故意逆序
        Long applyId = applyService.createApply(apply);

        Long maxLogIdBefore = inventoryLogMapper.selectList(new LambdaQueryWrapper<InventoryLog>()
                        .orderByDesc(InventoryLog::getId).last("LIMIT 1"))
                .stream().map(InventoryLog::getId).findFirst().orElse(0L);

        TestDataBuilder.mockSecurityContext(3L, "warehouse", "WAREHOUSE");
        ApprovalDTO approval = new ApprovalDTO();
        approval.setApplyId(applyId);
        approval.setApprovalResult(1);
        applyService.approveApply(approval);

        List<InventoryLog> lockLogs = inventoryLogMapper.selectList(new LambdaQueryWrapper<InventoryLog>()
                .gt(InventoryLog::getId, maxLogIdBefore)
                .eq(InventoryLog::getChangeType, 3)
                .orderByAsc(InventoryLog::getId));

        assertThat(lockLogs.stream().map(InventoryLog::getMaterialId).collect(Collectors.toList()))
                .as("按 materialId 升序加锁").containsExactly(M1, M2);
        assertThat(lockLogs.get(0).getChangeQuantity()).as("物资1 锁定 5").isEqualByComparingTo("5");
        assertThat(lockLogs.get(1).getChangeQuantity()).as("物资2 锁定 3").isEqualByComparingTo("3");
    }
}
