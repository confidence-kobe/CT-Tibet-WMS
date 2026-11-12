package com.ct.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ct.wms.common.api.PageResult;
import com.ct.wms.common.api.Result;
import com.ct.wms.dto.InboundDTO;
import com.ct.wms.dto.InboundUpdateDTO;
import com.ct.wms.dto.InboundVoidDTO;
import com.ct.wms.entity.Inbound;
import com.ct.wms.service.InboundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import javax.validation.constraints.Max;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 入库管理Controller
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/inbounds")
@RequiredArgsConstructor
@Tag(name = "入库管理", description = "入库单的创建、查询、编辑、作废接口")
public class InboundController {

    private final InboundService inboundService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_ADMIN', 'WAREHOUSE')")
    @Operation(summary = "分页查询入库单列表", description = "支持多条件筛选")
    public PageResult<Inbound> listInbounds(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @Max(100) @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "仓库ID") @RequestParam(required = false) Long warehouseId,
            @Parameter(description = "入库类型") @RequestParam(required = false) Integer inboundType,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "操作人ID") @RequestParam(required = false) Long operatorId,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword) {

        log.info("查询入库单列表: pageNum={}, pageSize={}, warehouseId={}, inboundType={}",
                pageNum, pageSize, warehouseId, inboundType);

        Page<Inbound> page = inboundService.listInbounds(pageNum, pageSize, warehouseId,
                inboundType, startDate, endDate, operatorId, keyword);

        return PageResult.of(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_ADMIN', 'WAREHOUSE')")
    @Operation(summary = "查询入库单详情", description = "根据ID查询入库单详细信息（含明细）")
    public Result<Inbound> getInboundById(
            @Parameter(description = "入库单ID") @PathVariable Long id) {
        log.info("查询入库单详情: id={}", id);
        Inbound inbound = inboundService.getInboundById(id);
        return Result.success(inbound);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_ADMIN', 'WAREHOUSE')")
    @Operation(summary = "创建入库单", description = "仓管员创建入库单，自动增加库存")
    public Result<Long> createInbound(@Validated @RequestBody InboundDTO dto) {
        log.info("创建入库单: dto={}", dto);
        Long inboundId = inboundService.createInbound(dto);
        return Result.success(inboundId, "入库成功");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_ADMIN', 'WAREHOUSE')")
    @Operation(summary = "编辑入库单", description = "只能修改入库类型、时间、备注和单价；数量录错请作废后重新入库")
    public Result<Void> updateInbound(
            @Parameter(description = "入库单ID") @PathVariable Long id,
            @Validated @RequestBody InboundUpdateDTO dto) {
        log.info("编辑入库单: id={}, dto={}", id, dto);
        inboundService.updateInbound(id, dto);
        return Result.success(null, "保存成功");
    }

    @PostMapping("/{id}/void")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_ADMIN', 'WAREHOUSE')")
    @Operation(summary = "作废入库单", description = "作废入库单并扣回其增加的库存；库存已被领用或锁定时不能作废")
    public Result<Void> voidInbound(
            @Parameter(description = "入库单ID") @PathVariable Long id,
            @Validated @RequestBody InboundVoidDTO dto) {
        log.info("作废入库单: id={}, reason={}", id, dto.getReason());
        inboundService.voidInbound(id, dto.getReason());
        return Result.success(null, "作废成功");
    }
}
