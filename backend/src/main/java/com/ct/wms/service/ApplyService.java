package com.ct.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ct.wms.dto.ApplyDTO;
import com.ct.wms.dto.ApprovalDTO;
import com.ct.wms.entity.Apply;

/**
 * 申请Service接口
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
public interface ApplyService {

    /**
     * 分页查询申请单列表
     *
     * @param pageNum     页码
     * @param pageSize    每页条数
     * @param warehouseId 仓库ID（可选）
     * @param status      状态（可选）
     * @param startDate   开始日期（可选）
     * @param endDate     结束日期（可选）
     * @param applicantId 申请人ID（可选）
     * @param approverId  审批人ID（可选）
     * @param keyword     关键词（可选）
     * @return 分页结果
     */
    Page<Apply> listApplies(Integer pageNum, Integer pageSize, Long warehouseId,
                            Integer status, String startDate, String endDate,
                            Long applicantId, Long approverId, String keyword);

    /**
     * 查询我的申请单列表
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param status   状态（可选）
     * @return 分页结果
     */
    Page<Apply> listMyApplies(Integer pageNum, Integer pageSize, Integer status);

    /**
     * 查询待审批列表（仓管员）
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    Page<Apply> listPendingApplies(Integer pageNum, Integer pageSize);

    /**
     * 根据ID获取申请单详情
     *
     * @param id 申请单ID
     * @return 申请单详情（包含明细）
     */
    Apply getApplyById(Long id);

    /**
     * 创建申请单
     *
     * @param dto 申请单DTO
     * @return 申请单ID
     */
    Long createApply(ApplyDTO dto);

    /**
     * 审批申请单
     * 通过时自动创建出库单
     *
     * @param dto 审批DTO
     */
    void approveApply(ApprovalDTO dto);

    /**
     * 取消申请单
     *
     * @param id 申请单ID
     */
    void cancelApply(Long id);
}
