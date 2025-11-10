package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ct.wms.common.exception.BusinessException;
import com.ct.wms.dto.MaterialDTO;
import com.ct.wms.dto.MaterialQueryDTO;
import com.ct.wms.entity.Inventory;
import com.ct.wms.entity.Material;
import com.ct.wms.mapper.InventoryMapper;
import com.ct.wms.mapper.MaterialMapper;
import com.ct.wms.service.MaterialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 物资Service实现类
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialServiceImpl implements MaterialService {

    private final MaterialMapper materialMapper;
    private final InventoryMapper inventoryMapper;

    @Override
    public Page<Material> listMaterials(MaterialQueryDTO queryDTO) {
        Page<Material> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<Material> wrapper = new LambdaQueryWrapper<>();

        // 类别筛选
        if (StringUtils.hasText(queryDTO.getCategory())) {
            wrapper.eq(Material::getCategory, queryDTO.getCategory());
        }

        // 状态筛选
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Material::getStatus, queryDTO.getStatus());
        }

        // 关键词搜索（名称或编码）
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(Material::getMaterialName, queryDTO.getKeyword())
                    .or()
                    .like(Material::getMaterialCode, queryDTO.getKeyword()));
        }

        // 按创建时间倒序
        wrapper.orderByDesc(Material::getCreateTime);

        return materialMapper.selectPage(page, wrapper);
    }

    @Override
    public Material getMaterialById(Long id) {
        Material material = materialMapper.selectById(id);
        if (material == null) {
            throw new BusinessException(404, "物资不存在");
        }
        return material;
    }

    @Override
    public Long createMaterial(MaterialDTO dto) {
        // 检查编码是否重复
        if (checkMaterialCodeExists(dto.getMaterialCode(), null)) {
            throw new BusinessException(409, "物资编码已存在");
        }

        Material material = new Material();
        BeanUtils.copyProperties(dto, material);
        material.setStatus(0); // 默认启用

        materialMapper.insert(material);
        log.info("创建物资成功: id={}, materialCode={}", material.getId(), material.getMaterialCode());

        return material.getId();
    }

    @Override
    public void updateMaterial(Long id, MaterialDTO dto) {
        Material material = getMaterialById(id);

        // 检查编码是否重复（排除自己）
        if (!material.getMaterialCode().equals(dto.getMaterialCode()) &&
                checkMaterialCodeExists(dto.getMaterialCode(), id)) {
            throw new BusinessException(409, "物资编码已存在");
        }

        BeanUtils.copyProperties(dto, material);
        material.setId(id);

        materialMapper.updateById(material);
        log.info("更新物资成功: id={}, materialCode={}", id, material.getMaterialCode());
    }

    @Override
    public void deleteMaterial(Long id) {
        Material material = getMaterialById(id);

        // 检查是否有库存
        LambdaQueryWrapper<Inventory> inventoryWrapper = new LambdaQueryWrapper<>();
        inventoryWrapper.eq(Inventory::getMaterialId, id);
        inventoryWrapper.gt(Inventory::getQuantity, BigDecimal.ZERO);

        Long count = inventoryMapper.selectCount(inventoryWrapper);
        if (count > 0) {
            throw new BusinessException(400, "该物资有库存，无法删除");
        }

        // 软删除
        materialMapper.deleteById(id);
        log.info("删除物资成功: id={}, materialCode={}", id, material.getMaterialCode());
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Material material = getMaterialById(id);

        material.setStatus(status);
        materialMapper.updateById(material);

        log.info("更新物资状态成功: id={}, status={}", id, status);
    }

    @Override
    public List<String> getCategories() {
        LambdaQueryWrapper<Material> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Material::getCategory);
        wrapper.groupBy(Material::getCategory);
        wrapper.eq(Material::getStatus, 0); // 只查询启用的物资

        return materialMapper.selectList(wrapper).stream()
                .map(Material::getCategory)
                .collect(Collectors.toList());
    }

    @Override
    public boolean checkMaterialCodeExists(String materialCode, Long excludeId) {
        LambdaQueryWrapper<Material> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Material::getMaterialCode, materialCode);

        if (excludeId != null) {
            wrapper.ne(Material::getId, excludeId);
        }

        return materialMapper.selectCount(wrapper) > 0;
    }
}
