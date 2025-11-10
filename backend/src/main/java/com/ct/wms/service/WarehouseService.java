package com.ct.wms.service;

import com.ct.wms.dto.WarehouseDTO;
import com.ct.wms.entity.Warehouse;

import java.util.List;

/**
 * 仓库Service接口
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
public interface WarehouseService {

    /**
     * 查询仓库列表
     *
     * @param deptId 部门ID（可选）
     * @param status 状态（可选）
     * @return 仓库列表
     */
    List<Warehouse> listWarehouses(Long deptId, Integer status);

    /**
     * 根据ID获取仓库详情
     *
     * @param id 仓库ID
     * @return 仓库详情
     */
    Warehouse getWarehouseById(Long id);

    /**
     * 创建仓库
     *
     * @param dto 仓库DTO
     * @return 仓库ID
     */
    Long createWarehouse(WarehouseDTO dto);

    /**
     * 更新仓库
     *
     * @param id  仓库ID
     * @param dto 仓库DTO
     */
    void updateWarehouse(Long id, WarehouseDTO dto);

    /**
     * 删除仓库
     *
     * @param id 仓库ID
     */
    void deleteWarehouse(Long id);

    /**
     * 启用/停用仓库
     *
     * @param id     仓库ID
     * @param status 状态(0-启用 1-禁用)
     */
    void updateStatus(Long id, Integer status);
}
