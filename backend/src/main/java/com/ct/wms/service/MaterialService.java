package com.ct.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ct.wms.dto.MaterialDTO;
import com.ct.wms.dto.MaterialQueryDTO;
import com.ct.wms.entity.Material;

import java.util.List;

/**
 * 物资Service接口
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
public interface MaterialService {

    /**
     * 分页查询物资列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<Material> listMaterials(MaterialQueryDTO queryDTO);

    /**
     * 根据ID获取物资详情
     *
     * @param id 物资ID
     * @return 物资详情
     */
    Material getMaterialById(Long id);

    /**
     * 创建物资
     *
     * @param dto 物资DTO
     * @return 物资ID
     */
    Long createMaterial(MaterialDTO dto);

    /**
     * 更新物资
     *
     * @param id  物资ID
     * @param dto 物资DTO
     */
    void updateMaterial(Long id, MaterialDTO dto);

    /**
     * 删除物资（软删除）
     *
     * @param id 物资ID
     */
    void deleteMaterial(Long id);

    /**
     * 启用/停用物资
     *
     * @param id     物资ID
     * @param status 状态(0-启用 1-停用)
     */
    void updateStatus(Long id, Integer status);

    /**
     * 获取所有物资类别
     *
     * @return 类别列表
     */
    List<String> getCategories();

    /**
     * 检查物资编码是否存在
     *
     * @param materialCode 物资编码
     * @param excludeId    排除的ID（更新时使用）
     * @return true-存在 false-不存在
     */
    boolean checkMaterialCodeExists(String materialCode, Long excludeId);
}
