package com.ct.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ct.wms.common.api.PageResult;
import com.ct.wms.common.api.Result;
import com.ct.wms.dto.OutboundDTO;
import com.ct.wms.entity.Outbound;
import com.ct.wms.service.OutboundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 出库管理Controller
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@RestController
@RequestMapping("/api/outbounds")
@RequiredArgsConstructor
@Tag(name = "出库管理", description = "出库单的创建、查询、确认接口")
public class OutboundController {

    private final OutboundService outboundService;

    @GetMapping
    @Operation(summary = "分页查询出库单列表", description = "支持多条件筛选")
    public Result<PageResult<Outbound>> listOutbounds(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "仓库ID") @RequestParam(required = false) Long warehouseId,
            @Parameter(description = "出库类型") @RequestParam(required = false) Integer outboundType,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "操作人ID") @RequestParam(required = false) Long operatorId,
            @Parameter(description = "领用人ID") @RequestParam(required = false) Long receiverId,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword) {

        log.info("查询出库单列表: pageNum={}, pageSize={}, warehouseId={}, outboundType={}, status={}",
                pageNum, pageSize, warehouseId, outboundType, status);

        Page<Outbound> page = outboundService.listOutbounds(pageNum, pageSize, warehouseId,
                outboundType, status, startDate, endDate, operatorId, receiverId, keyword);

        return Result.success(PageResult.of(page));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询出库单详情", description = "根据ID查询出库单详细信息（含明细）")
    public Result<Outbound> getOutboundById(
            @Parameter(description = "出库单ID") @PathVariable Long id) {
        log.info("查询出库单详情: id={}", id);
        Outbound outbound = outboundService.getOutboundById(id);
        return Result.success(outbound);
    }

    @PostMapping("/direct")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_ADMIN', 'WAREHOUSE')")
    @Operation(summary = "创建直接出库单", description = "仓管员创建出库单，立即扣减库存")
    public Result<Long> createOutboundDirect(@Validated @RequestBody OutboundDTO dto) {
        log.info("创建直接出库单: dto={}", dto);
        Long outboundId = outboundService.createOutboundDirect(dto);
        return Result.success(outboundId, "出库成功");
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_ADMIN', 'WAREHOUSE')")
    @Operation(summary = "确认出库", description = "员工取货后，仓管员确认出库并扣减库存")
    public Result<Void> confirmOutbound(
            @Parameter(description = "出库单ID") @PathVariable Long id) {
        log.info("确认出库: id={}", id);
        outboundService.confirmOutbound(id);
        return Result.success(null, "确认出库成功");
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_ADMIN', 'WAREHOUSE')")
    @Operation(summary = "取消出库单", description = "取消待取货的出库单")
    public Result<Void> cancelOutbound(
            @Parameter(description = "出库单ID") @PathVariable Long id,
            @Parameter(description = "取消原因") @RequestParam String reason) {
        log.info("取消出库单: id={}, reason={}", id, reason);
        outboundService.cancelOutbound(id, reason);
        return Result.success(null, "取消成功");
    }
}
