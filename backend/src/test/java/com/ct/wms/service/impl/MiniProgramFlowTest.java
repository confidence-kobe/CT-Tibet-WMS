package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ct.wms.common.enums.ApplyStatus;
import com.ct.wms.common.enums.OutboundStatus;
import com.ct.wms.dto.ApplyDTO;
import com.ct.wms.dto.ApprovalDTO;
import com.ct.wms.dto.OutboundDTO;
import com.ct.wms.entity.Apply;
import com.ct.wms.entity.ApplyDetail;
import com.ct.wms.entity.Inventory;
import com.ct.wms.entity.Outbound;
import com.ct.wms.mapper.ApplyMapper;
import com.ct.wms.mapper.InventoryMapper;
import com.ct.wms.mapper.OutboundMapper;
import com.ct.wms.service.ApplyService;
import com.ct.wms.service.InventoryService;
import com.ct.wms.service.OutboundService;
import com.ct.wms.service.UserService;
import com.ct.wms.util.TestDataBuilder;
import com.ct.wms.vo.UserOptionVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 小程序核心业务流程测试：员工申请 → 仓管审批 → 待领取列表 → 确认/取消 → 快速出库
 *
 * 测试数据（data.sql）：仓库1属于部门2；用户3是部门2的仓管员；用户4（employee1）属于部门2，
 * 用户5（employee2）属于部门3；仓库1中物资1库存 500
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("小程序核心业务流程测试")
class MiniProgramFlowTest {

    private static final Long WAREHOUSE_ID = 1L;
    private static final Long MATERIAL_ID = 1L;

    @Autowired
    private ApplyService applyService;
    @Autowired
    private OutboundService outboundService;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private UserService userService;
    @Autowired
    private ApplyMapper applyMapper;
    @Autowired
    private OutboundMapper outboundMapper;
    @Autowired
    private InventoryMapper inventoryMapper;

    @AfterEach
    void tearDown() {
        TestDataBuilder.clearSecurityContext();
    }

    private void asEmployee() {
        TestDataBuilder.mockSecurityContext(4L, "employee1", "USER");
    }

    private void asWarehouseManager() {
        TestDataBuilder.mockSecurityContext(3L, "warehouse", "WAREHOUSE");
    }

    private Long submitApply(String quantity) {
        asEmployee();
        ApplyDTO dto = new ApplyDTO();
        dto.setWarehouseId(WAREHOUSE_ID);
        dto.setApplyReason("基站维修");
        ApplyDTO.ApplyDetailDTO detail = new ApplyDTO.ApplyDetailDTO();
        detail.setMaterialId(MATERIAL_ID);
        detail.setQuantity(new BigDecimal(quantity));
        dto.setDetails(Collections.singletonList(detail));
        return applyService.createApply(dto);
    }

    private void approve(Long applyId, int result, String remark) {
        asWarehouseManager();
        ApprovalDTO dto = new ApprovalDTO();
        dto.setApplyId(applyId);
        dto.setApprovalResult(result);
        dto.setApprovalRemark(remark);
        applyService.approveApply(dto);
    }

    private Inventory stock() {
        return inventoryMapper.selectOne(new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getWarehouseId, WAREHOUSE_ID)
                .eq(Inventory::getMaterialId, MATERIAL_ID));
    }

    @Test
    @DisplayName("审批通过：回写出库单ID、保存审批意见、锁定库存")
    void approveLinksOutboundAndSavesOpinion() {
        Long applyId = submitApply("5");
        approve(applyId, 1, "同意，尽快领取");

        Apply apply = applyMapper.selectById(applyId);
        assertThat(apply.getStatus()).isEqualTo(ApplyStatus.APPROVED);
        assertThat(apply.getOutboundId()).isNotNull();
        assertThat(apply.getApprovalOpinion()).isEqualTo("同意，尽快领取");

        Outbound outbound = outboundMapper.selectById(apply.getOutboundId());
        assertThat(outbound.getApplyId()).isEqualTo(applyId);
        assertThat(outbound.getPurpose()).isEqualTo("基站维修");
        assertThat(outbound.getReceiverPhone()).isEqualTo("13800000013");
        assertThat(stock().getLockedQuantity()).isEqualByComparingTo("5");
    }

    @Test
    @DisplayName("审批拒绝：保存拒绝原因")
    void rejectSavesReason() {
        Long applyId = submitApply("5");
        approve(applyId, 2, "库存要留给抢修");

        Apply apply = applyMapper.selectById(applyId);
        assertThat(apply.getStatus()).isEqualTo(ApplyStatus.REJECTED);
        assertThat(apply.getRejectReason()).isEqualTo("库存要留给抢修");
    }

    @Test
    @DisplayName("申请详情：仓管能看到可用库存和是否充足，员工看不到库存数字")
    void applyDetailStockCheckOnlyForManagers() {
        Long enough = submitApply("5");
        Long tooMuch = submitApply("9999");

        asWarehouseManager();
        ApplyDetail ok = applyService.getApplyById(enough).getDetails().get(0);
        assertThat(ok.getCurrentStock()).isEqualByComparingTo("500");
        assertThat(ok.getIsStockSufficient()).isTrue();
        ApplyDetail short1 = applyService.getApplyById(tooMuch).getDetails().get(0);
        assertThat(short1.getIsStockSufficient()).isFalse();

        asEmployee();
        ApplyDetail hidden = applyService.getApplyById(enough).getDetails().get(0);
        assertThat(hidden.getCurrentStock()).isNull();
        assertThat(hidden.getIsStockSufficient()).isNull();
    }

    @Test
    @DisplayName("已处理筛选：只返回审批过的申请")
    void processedFilterExcludesPending() {
        Long pending = submitApply("1");
        Long approved = submitApply("1");
        approve(approved, 1, null);

        asWarehouseManager();
        List<Long> ids = applyService.listApplies(1, 100, null, null, null, null, null, null, null, true)
                .getRecords().stream().map(Apply::getId).collect(Collectors.toList());
        assertThat(ids).contains(approved).doesNotContain(pending);
    }

    @Test
    @DisplayName("待领取列表带出物资明细、来源申请单号和用途；确认后扣库存")
    void pendingListShowsDetailsThenConfirmDeducts() {
        Long applyId = submitApply("5");
        approve(applyId, 1, null);
        Long outboundId = applyMapper.selectById(applyId).getOutboundId();

        asWarehouseManager();
        Outbound row = outboundService.listOutbounds(1, 100, null, null, 0, null, null, null, null, null)
                .getRecords().stream().filter(o -> o.getId().equals(outboundId)).findFirst().orElseThrow();
        assertThat(row.getDetails()).hasSize(1);
        assertThat(row.getDetails().get(0).getQuantity()).isEqualByComparingTo("5");
        assertThat(row.getItemCount()).isEqualTo(1);
        assertThat(row.getApplyNo()).isEqualTo(applyMapper.selectById(applyId).getApplyNo());
        assertThat(row.getPurpose()).isEqualTo("基站维修");

        outboundService.confirmOutbound(outboundId);
        assertThat(stock().getQuantity()).isEqualByComparingTo("495");
        assertThat(stock().getLockedQuantity()).isEqualByComparingTo("0");
        assertThat(applyMapper.selectById(applyId).getStatus()).isEqualTo(ApplyStatus.COMPLETED);
    }

    @Test
    @DisplayName("取消待领取出库单：释放锁定库存，原因写入出库单和申请单")
    void cancelReleasesLockAndSavesReason() {
        Long applyId = submitApply("5");
        approve(applyId, 1, null);
        Long outboundId = applyMapper.selectById(applyId).getOutboundId();

        asWarehouseManager();
        outboundService.cancelOutbound(outboundId, "员工不再需要");

        Outbound outbound = outboundMapper.selectById(outboundId);
        assertThat(outbound.getStatus()).isEqualTo(OutboundStatus.CANCELED);
        assertThat(outbound.getCancelReason()).isEqualTo("员工不再需要");
        Apply apply = applyMapper.selectById(applyId);
        assertThat(apply.getStatus()).isEqualTo(ApplyStatus.CANCELED);
        assertThat(apply.getCancelReason()).contains("员工不再需要");
        assertThat(apply.getCancelTime()).isNotNull();
        assertThat(stock().getLockedQuantity()).isEqualByComparingTo("0");
        assertThat(stock().getQuantity()).isEqualByComparingTo("500");
    }

    @Test
    @DisplayName("快速出库：保存用途和领用人，立即扣库存")
    void directOutboundSavesPurpose() {
        asWarehouseManager();
        OutboundDTO dto = new OutboundDTO();
        dto.setWarehouseId(WAREHOUSE_ID);
        dto.setOutboundType(1);
        dto.setOutboundTime(LocalDateTime.now());
        dto.setReceiverId(4L);
        dto.setPurpose("机房应急");
        OutboundDTO.OutboundDetailDTO detail = new OutboundDTO.OutboundDetailDTO();
        detail.setMaterialId(MATERIAL_ID);
        detail.setQuantity(new BigDecimal("3"));
        dto.setDetails(Collections.singletonList(detail));

        Long id = outboundService.createOutboundDirect(dto);

        Outbound outbound = outboundMapper.selectById(id);
        assertThat(outbound.getPurpose()).isEqualTo("机房应急");
        assertThat(outbound.getReceiverName()).isEqualTo("Emp Zhang");
        assertThat(stock().getQuantity()).isEqualByComparingTo("497");
    }

    @Test
    @DisplayName("领用人下拉：仓管只看到本部门人员，管理员看到全部")
    void userOptionsScopedToDept() {
        asWarehouseManager();
        List<UserOptionVO> mine = userService.listUserOptions(null);
        assertThat(mine).extracting(UserOptionVO::getDeptId).containsOnly(2L);
        assertThat(mine).extracting(UserOptionVO::getId).contains(4L).doesNotContain(5L);

        List<UserOptionVO> byKeyword = userService.listUserOptions("Zhang");
        assertThat(byKeyword).extracting(UserOptionVO::getId).containsExactly(4L);

        TestDataBuilder.mockSecurityContext(1L, "admin", "ADMIN");
        assertThat(userService.listUserOptions(null)).extracting(UserOptionVO::getId).contains(4L, 5L);
    }

    @Test
    @DisplayName("库存列表：按分类筛选并带出库存状态")
    void inventoryCategoryFilterAndStatus() {
        asWarehouseManager();
        List<Inventory> cables = inventoryService.listInventories(1, 100, null, null, null, null, "Cable")
                .getRecords();
        assertThat(cables).isNotEmpty();
        assertThat(cables).extracting(Inventory::getMaterialId).containsOnly(1L, 2L);
        assertThat(cables).allSatisfy(inv -> assertThat(inv.getStockStatus()).isNotNull());
    }
}
