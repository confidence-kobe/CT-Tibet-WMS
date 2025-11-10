package com.ct.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ct.wms.common.api.PageResult;
import com.ct.wms.common.api.Result;
import com.ct.wms.dto.ApplyDTO;
import com.ct.wms.dto.ApprovalDTO;
import com.ct.wms.entity.Apply;
import com.ct.wms.service.ApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 申请审批Controller
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@RestController
@RequestMapping("/api/applies")
@RequiredArgsConstructor
@Tag(name = "申请审批管理", description = "物资申请、审批接口")
public class ApplyController {

    private final ApplyService applyService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_ADMIN', 'WAREHOUSE')")
    @Operation(summary = "分页查询申请单列表", description = "支持多条件筛选（管理员/仓管员）")
    public Result<PageResult<Apply>> listApplies(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "仓库ID") @RequestParam(required = false) Long warehouseId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "申请人ID") @RequestParam(required = false) Long applicantId,
            @Parameter(description = "审批人ID") @RequestParam(required = false) Long approverId,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword) {

        log.info("查询申请单列表: pageNum={}, pageSize={}, warehouseId={}, status={}",
                pageNum, pageSize, warehouseId, status);

        Page<Apply> page = applyService.listApplies(pageNum, pageSize, warehouseId,
                status, startDate, endDate, applicantId, approverId, keyword);

        return Result.success(PageResult.of(page));
    }

    @GetMapping("/my")
    @Operation(summary = "查询我的申请单列表", description = "普通员工查看自己的申请")
    public Result<PageResult<Apply>> listMyApplies(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {

        log.info("查询我的申请单: pageNum={}, pageSize={}, status={}", pageNum, pageSize, status);

        Page<Apply> page = applyService.listMyApplies(pageNum, pageSize, status);

        return Result.success(PageResult.of(page));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_ADMIN', 'WAREHOUSE')")
    @Operation(summary = "查询待审批列表", description = "仓管员查看待审批的申请")
    public Result<PageResult<Apply>> listPendingApplies(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer pageSize) {

        log.info("查询待审批列表: pageNum={}, pageSize={}", pageNum, pageSize);

        Page<Apply> page = applyService.listPendingApplies(pageNum, pageSize);

        return Result.success(PageResult.of(page));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询申请单详情", description = "根据ID查询申请单详细信息（含明细）")
    public Result<Apply> getApplyById(
            @Parameter(description = "申请单ID") @PathVariable Long id) {
        log.info("查询申请单详情: id={}", id);
        Apply apply = applyService.getApplyById(id);
        return Result.success(apply);
    }

    @PostMapping
    @Operation(summary = "创建申请单", description = "普通员工提交物资申请")
    public Result<Long> createApply(@Validated @RequestBody ApplyDTO dto) {
        log.info("创建申请单: dto={}", dto);
        Long applyId = applyService.createApply(dto);
        return Result.success(applyId, "申请提交成功");
    }

    @PostMapping("/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_ADMIN', 'WAREHOUSE')")
    @Operation(summary = "审批申请单", description = "仓管员审批申请单，通过后自动创建出库单")
    public Result<Void> approveApply(@Validated @RequestBody ApprovalDTO dto) {
        log.info("审批申请单: dto={}", dto);
        applyService.approveApply(dto);
        return Result.success(null, "审批成功");
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消申请单", description = "申请人取消自己的申请")
    public Result<Void> cancelApply(
            @Parameter(description = "申请单ID") @PathVariable Long id) {
        log.info("取消申请单: id={}", id);
        applyService.cancelApply(id);
        return Result.success(null, "取消成功");
    }
}
