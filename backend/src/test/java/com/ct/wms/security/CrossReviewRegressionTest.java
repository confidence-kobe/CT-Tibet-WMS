package com.ct.wms.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ct.wms.common.enums.ApplyStatus;
import com.ct.wms.common.enums.InboundType;
import com.ct.wms.common.exception.BusinessException;
import com.ct.wms.dto.ApplyDTO;
import com.ct.wms.dto.ApprovalDTO;
import com.ct.wms.dto.InboundDTO;
import com.ct.wms.dto.OutboundDTO;
import com.ct.wms.entity.Apply;
import com.ct.wms.entity.InventoryLog;
import com.ct.wms.entity.Role;
import com.ct.wms.mapper.ApplyMapper;
import com.ct.wms.mapper.InventoryLogMapper;
import com.ct.wms.mapper.RoleMapper;
import com.ct.wms.service.ApplyService;
import com.ct.wms.service.InboundService;
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

import javax.validation.Validation;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 回归测试：Codex 对 W2 修复做异源审查后发现的问题（2026-09-29）。
 * <ol>
 *   <li>角色编码大小写：sql/init_data.sql 是小写（admin / dept_admin），测试种子是大写——新守卫曾拒绝真实环境的部门管理员</li>
 *   <li>数量精度：库存列 DECIMAL(10,2)，0.015 会产生残留锁定</li>
 *   <li>入库也要按 materialId 顺序更新库存，否则与审批交错时会死锁</li>
 * </ol>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("异源审查回归")
class CrossReviewRegressionTest {

    private static final long WH = 1L;   // 部门 2，仓管员 = 用户 3

    @Autowired private RoleMapper roleMapper;
    @Autowired private ApplyMapper applyMapper;
    @Autowired private ApplyService applyService;
    @Autowired private InventoryService inventoryService;
    @Autowired private InboundService inboundService;
    @Autowired private InventoryLogMapper inventoryLogMapper;

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    /** 模拟生产初始化数据：角色编码为小写 */
    private void useLowercaseRoleCodes() {
        for (long id = 1; id <= 4; id++) {
            Role r = roleMapper.selectById(id);
            r.setRoleCode(r.getRoleCode().toLowerCase());
            roleMapper.updateById(r);
        }
    }

    @Test
    @DisplayName("小写角色 dept_admin：可以查看并审批本部门申请")
    void lowercaseDeptAdminStillWorks() {
        useLowercaseRoleCodes();
        TestDataBuilder.mockSecurityContext(1L, "admin", "ADMIN");
        inventoryService.increaseInventory(WH, 1L, new BigDecimal("50"), "T", null, 1L);

        TestDataBuilder.mockSecurityContext(4L, "employee1", "USER");
        ApplyDTO.ApplyDetailDTO d = new ApplyDTO.ApplyDetailDTO();
        d.setMaterialId(1L);
        d.setQuantity(new BigDecimal("2"));
        ApplyDTO dto = new ApplyDTO();
        dto.setWarehouseId(WH);
        dto.setDetails(List.of(d));
        Long applyId = applyService.createApply(dto);

        TestDataBuilder.mockSecurityContext(2L, "dept_admin", "DEPT_ADMIN");   // 部门 2 的部门管理员，不是仓管员
        assertThatCode(() -> applyService.getApplyById(applyId)).doesNotThrowAnyException();
        ApprovalDTO approval = new ApprovalDTO();
        approval.setApplyId(applyId);
        approval.setApprovalResult(1);
        assertThatCode(() -> applyService.approveApply(approval)).doesNotThrowAnyException();
        Apply after = applyMapper.selectById(applyId);
        assertThat(after.getStatus()).isEqualTo(ApplyStatus.APPROVED);
    }

    @Test
    @DisplayName("小写角色 dept_admin：跨部门仍然拒绝")
    void lowercaseDeptAdminStillScoped() {
        useLowercaseRoleCodes();
        Apply a = new Apply();
        a.setApplyNo("SQ_T_" + System.nanoTime());
        a.setApplicantId(5L);
        a.setDeptId(3L);
        a.setWarehouseId(2L);
        a.setStatus(ApplyStatus.PENDING);
        a.setApplyTime(LocalDateTime.now());
        applyMapper.insert(a);
        TestDataBuilder.mockSecurityContext(2L, "dept_admin", "DEPT_ADMIN");
        assertThatThrownBy(() -> applyService.getApplyById(a.getId())).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("数量超过两位小数：服务层与三个 DTO 都拒绝")
    void quantityPrecisionRejected() {
        TestDataBuilder.mockSecurityContext(1L, "admin", "ADMIN");
        inventoryService.increaseInventory(WH, 1L, new BigDecimal("10"), "T", null, 1L);
        BigDecimal q = new BigDecimal("0.015");
        assertThatThrownBy(() -> inventoryService.lockInventory(WH, 1L, q)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> inventoryService.increaseInventory(WH, 1L, q, "T", null, 1L)).isInstanceOf(BusinessException.class);
        assertThat(inventoryService.lockInventory(WH, 1L, new BigDecimal("0.50"))).isTrue();   // 尾随 0 不算多余精度

        Validator v = Validation.buildDefaultValidatorFactory().getValidator();
        ApplyDTO.ApplyDetailDTO a = new ApplyDTO.ApplyDetailDTO();
        a.setMaterialId(1L);
        a.setQuantity(q);
        OutboundDTO.OutboundDetailDTO o = new OutboundDTO.OutboundDetailDTO();
        o.setMaterialId(1L);
        o.setQuantity(q);
        InboundDTO.InboundDetailDTO i = new InboundDTO.InboundDetailDTO();
        i.setMaterialId(1L);
        i.setQuantity(q);
        assertThat(v.validate(a)).isNotEmpty();
        assertThat(v.validate(o)).isNotEmpty();
        assertThat(v.validate(i)).isNotEmpty();
    }

    @Test
    @DisplayName("入库明细 [物资2, 物资1]：库存按 物资1 → 物资2 更新")
    void inboundUpdatesInventoryInMaterialIdOrder() {
        TestDataBuilder.mockSecurityContext(1L, "admin", "ADMIN");
        Long maxBefore = inventoryLogMapper.selectList(new LambdaQueryWrapper<InventoryLog>()
                        .orderByDesc(InventoryLog::getId).last("LIMIT 1"))
                .stream().map(InventoryLog::getId).findFirst().orElse(0L);

        InboundDTO dto = new InboundDTO();
        dto.setWarehouseId(WH);
        dto.setInboundType(InboundType.PURCHASE);
        dto.setInboundTime(LocalDateTime.now());
        InboundDTO.InboundDetailDTO m2 = new InboundDTO.InboundDetailDTO();
        m2.setMaterialId(2L);
        m2.setQuantity(new BigDecimal("4"));
        InboundDTO.InboundDetailDTO m1 = new InboundDTO.InboundDetailDTO();
        m1.setMaterialId(1L);
        m1.setQuantity(new BigDecimal("6"));
        dto.setDetails(List.of(m2, m1));
        inboundService.createInbound(dto);

        List<Long> order = inventoryLogMapper.selectList(new LambdaQueryWrapper<InventoryLog>()
                        .gt(InventoryLog::getId, maxBefore).eq(InventoryLog::getChangeType, 1)
                        .orderByAsc(InventoryLog::getId))
                .stream().map(InventoryLog::getMaterialId).collect(Collectors.toList());
        assertThat(order).containsExactly(1L, 2L);
    }
}
