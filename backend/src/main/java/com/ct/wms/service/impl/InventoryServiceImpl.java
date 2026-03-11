package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ct.wms.common.exception.BusinessException;
import com.ct.wms.entity.*;
import com.ct.wms.mapper.*;
import com.ct.wms.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存Service实现类
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryMapper inventoryMapper;
    private final InventoryLogMapper inventoryLogMapper;
    private final WarehouseMapper warehouseMapper;
    private final MaterialMapper materialMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void increaseInventory(Long warehouseId, Long materialId, BigDecimal quantity,
                                  String relatedNo, Long relatedId, Long operatorId) {
        // 查询库存记录
        Inventory inventory = getInventory(warehouseId, materialId);

        BigDecimal beforeQuantity;
        BigDecimal afterQuantity;

        if (inventory == null) {
            // 库存不存在，创建新记录
            inventory = new Inventory();
            inventory.setWarehouseId(warehouseId);
            inventory.setMaterialId(materialId);
            inventory.setQuantity(quantity);
            inventory.setLockedQuantity(BigDecimal.ZERO);
            inventory.setAvailableQuantity(quantity);
            inventory.setLastInboundTime(LocalDateTime.now());
            inventory.setVersion(0);

            inventoryMapper.insert(inventory);

            beforeQuantity = BigDecimal.ZERO;
            afterQuantity = quantity;

            log.info("创建库存记录: warehouseId={}, materialId={}, quantity={}",
                    warehouseId, materialId, quantity);
        } else {
            // 库存存在，增加数量（使用乐观锁）
            beforeQuantity = inventory.getQuantity();
            afterQuantity = beforeQuantity.add(quantity);

            inventory.setQuantity(afterQuantity);
            inventory.setAvailableQuantity(afterQuantity.subtract(inventory.getLockedQuantity()));
            inventory.setLastInboundTime(LocalDateTime.now());
            inventory.setVersion(inventory.getVersion() + 1);

            // 使用乐观锁更新
            LambdaQueryWrapper<Inventory> updateWrapper = new LambdaQueryWrapper<>();
            updateWrapper.eq(Inventory::getId, inventory.getId())
                    .eq(Inventory::getVersion, inventory.getVersion() - 1);
            int updated = inventoryMapper.update(inventory, updateWrapper);
            if (updated == 0) {
                throw new BusinessException(500, "库存更新失败，请重试");
            }

            log.info("增加库存: warehouseId={}, materialId={}, before={}, after={}",
                    warehouseId, materialId, beforeQuantity, afterQuantity);
        }

        // 记录库存流水
        saveInventoryLog(warehouseId, materialId, 1, quantity, beforeQuantity, afterQuantity,
                relatedNo, 1, relatedId, operatorId, "入库");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decreaseInventory(Long warehouseId, Long materialId, BigDecimal quantity,
                                  String relatedNo, Long relatedId, Long operatorId) {
        // 查询库存记录
        Inventory inventory = getInventory(warehouseId, materialId);

        if (inventory == null) {
            throw new BusinessException(1001, "库存不存在");
        }

        // 检查库存是否充足
        if (inventory.getQuantity().compareTo(quantity) < 0) {
            throw new BusinessException(1001, "库存不足，当前库存：" + inventory.getQuantity());
        }

        // 检查数量是否为正数
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(400, "数量必须大于0");
        }

        BigDecimal beforeQuantity = inventory.getQuantity();
        BigDecimal afterQuantity = beforeQuantity.subtract(quantity);

        // 减少库存（使用乐观锁）
        inventory.setQuantity(afterQuantity);
        inventory.setAvailableQuantity(afterQuantity.subtract(inventory.getLockedQuantity()));
        inventory.setLastOutboundTime(LocalDateTime.now());
        inventory.setVersion(inventory.getVersion() + 1);

        // 使用乐观锁更新
        LambdaQueryWrapper<Inventory> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(Inventory::getId, inventory.getId())
                .eq(Inventory::getVersion, inventory.getVersion() - 1);
        int updated = inventoryMapper.update(inventory, updateWrapper);
        if (updated == 0) {
            throw new BusinessException(500, "库存更新失败，请重试");
        }

        log.info("减少库存: warehouseId={}, materialId={}, before={}, after={}",
                warehouseId, materialId, beforeQuantity, afterQuantity);

        // 记录库存流水
        saveInventoryLog(warehouseId, materialId, 2, quantity.negate(), beforeQuantity, afterQuantity,
                relatedNo, 2, relatedId, operatorId, "出库");
    }

    @Override
    public Inventory getInventory(Long warehouseId, Long materialId) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inventory::getWarehouseId, warehouseId);
        wrapper.eq(Inventory::getMaterialId, materialId);

        return inventoryMapper.selectOne(wrapper);
    }

    @Override
    public boolean checkInventory(Long warehouseId, Long materialId, BigDecimal quantity) {
        Inventory inventory = getInventory(warehouseId, materialId);
        if (inventory == null) {
            return false;
        }

        return inventory.getQuantity().compareTo(quantity) >= 0;
    }

    @Override
    public Page<Inventory> listInventories(Integer pageNum, Integer pageSize, Long warehouseId,
                                           Long materialId, String keyword) {
        Page<Inventory> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();

        if (warehouseId != null) {
            wrapper.eq(Inventory::getWarehouseId, warehouseId);
        }

        if (materialId != null) {
            wrapper.eq(Inventory::getMaterialId, materialId);
        }

        // 关键词搜索（需要联表查询物资名称，这里简化处理）
        if (StringUtils.hasText(keyword)) {
            // 先查询匹配的物资ID
            LambdaQueryWrapper<Material> materialWrapper = new LambdaQueryWrapper<>();
            materialWrapper.like(Material::getMaterialName, keyword)
                    .or().like(Material::getMaterialCode, keyword);
            List<Material> materials = materialMapper.selectList(materialWrapper);

            if (!materials.isEmpty()) {
                List<Long> materialIds = materials.stream()
                        .map(Material::getId)
                        .collect(java.util.stream.Collectors.toList());
                wrapper.in(Inventory::getMaterialId, materialIds);
            } else {
                // 如果没有匹配的物资，返回空结果
                return page;
            }
        }

        wrapper.orderByDesc(Inventory::getUpdateTime);

        Page<Inventory> result = inventoryMapper.selectPage(page, wrapper);

        // 填充关联数据（防御性检查，避免空指针）
        if (result != null && result.getRecords() != null && !result.getRecords().isEmpty()) {
            result.getRecords().forEach(this::fillInventoryInfo);
        }

        return result;
    }

    @Override
    public Page<InventoryLog> listInventoryLogs(Integer pageNum, Integer pageSize, Long warehouseId,
                                                Long materialId, Integer changeType, String startDate, String endDate) {
        Page<InventoryLog> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<InventoryLog> wrapper = new LambdaQueryWrapper<>();

        if (warehouseId != null) {
            wrapper.eq(InventoryLog::getWarehouseId, warehouseId);
        }

        if (materialId != null) {
            wrapper.eq(InventoryLog::getMaterialId, materialId);
        }

        if (changeType != null) {
            wrapper.eq(InventoryLog::getChangeType, changeType);
        }

        if (StringUtils.hasText(startDate)) {
            wrapper.ge(InventoryLog::getCreateTime, startDate + " 00:00:00");
        }

        if (StringUtils.hasText(endDate)) {
            wrapper.le(InventoryLog::getCreateTime, endDate + " 23:59:59");
        }

        wrapper.orderByDesc(InventoryLog::getCreateTime);

        Page<InventoryLog> result = inventoryLogMapper.selectPage(page, wrapper);

        // 填充关联数据（防御性检查，避免空指针）
        if (result != null && result.getRecords() != null && !result.getRecords().isEmpty()) {
            result.getRecords().forEach(this::fillInventoryLogInfo);
        }

        return result;
    }

    @Override
    public List<Inventory> listLowStockAlerts(Long warehouseId) {
        // 查询所有库存
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();

        if (warehouseId != null) {
            wrapper.eq(Inventory::getWarehouseId, warehouseId);
        }

        List<Inventory> inventories = inventoryMapper.selectList(wrapper);

        // 过滤出低于最低库存的记录
        List<Inventory> lowStockList = new java.util.ArrayList<>();
        for (Inventory inventory : inventories) {
            Material material = materialMapper.selectById(inventory.getMaterialId());
            if (material != null && material.getMinStock() != null) {
                if (inventory.getQuantity().compareTo(material.getMinStock()) < 0) {
                    // 填充关联信息
                    fillInventoryInfo(inventory);
                    lowStockList.add(inventory);
                }
            }
        }

        return lowStockList;
    }

    /**
     * 填充库存关联信息
     */
    private void fillInventoryInfo(Inventory inventory) {
        // 填充仓库名称
        Warehouse warehouse = warehouseMapper.selectById(inventory.getWarehouseId());
        if (warehouse != null) {
            inventory.setWarehouseName(warehouse.getWarehouseName());
        }

        // 填充物资信息
        Material material = materialMapper.selectById(inventory.getMaterialId());
        if (material != null) {
            inventory.setMaterialName(material.getMaterialName());
            inventory.setMaterialCode(material.getMaterialCode());
            inventory.setSpec(material.getSpec());
            inventory.setUnit(material.getUnit());
        }
    }

    /**
     * 填充库存流水关联信息
     */
    private void fillInventoryLogInfo(InventoryLog log) {
        // 填充仓库名称
        Warehouse warehouse = warehouseMapper.selectById(log.getWarehouseId());
        if (warehouse != null) {
            log.setWarehouseName(warehouse.getWarehouseName());
        }

        // 填充物资信息
        Material material = materialMapper.selectById(log.getMaterialId());
        if (material != null) {
            log.setMaterialName(material.getMaterialName());
            log.setMaterialCode(material.getMaterialCode());
            log.setSpec(material.getSpec());
            log.setUnit(material.getUnit());
        }

        // 填充操作人姓名
        if (log.getOperatorId() != null) {
            User operator = userMapper.selectById(log.getOperatorId());
            if (operator != null) {
                log.setOperatorName(operator.getRealName());
            }
        }
    }

    /**
     * 保存库存流水
     */
    private void saveInventoryLog(Long warehouseId, Long materialId, Integer changeType,
                                  BigDecimal changeQuantity, BigDecimal beforeQuantity,
                                  BigDecimal afterQuantity, String relatedNo, Integer relatedType,
                                  Long relatedId, Long operatorId, String remark) {
        InventoryLog log = new InventoryLog();
        log.setWarehouseId(warehouseId);
        log.setMaterialId(materialId);
        log.setChangeType(changeType);
        log.setChangeQuantity(changeQuantity);
        log.setBeforeQuantity(beforeQuantity);
        log.setAfterQuantity(afterQuantity);
        log.setRelatedNo(relatedNo);
        log.setRelatedType(relatedType);
        log.setRelatedId(relatedId);
        log.setOperatorId(operatorId);
        log.setRemark(remark);

        inventoryLogMapper.insert(log);
    }

    @Override
    public Inventory getById(Long id) {
        return inventoryMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean lockInventory(Long warehouseId, Long materialId, BigDecimal quantity) {
        // 使用乐观锁原子性地锁定库存
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            // 查询当前库存（使用FOR UPDATE加行锁）
            Inventory inventory = getInventoryWithLock(warehouseId, materialId);
            if (inventory == null) {
                throw new BusinessException(1001, "库存不存在");
            }

            // 检查可用库存是否充足
            if (inventory.getAvailableQuantity().compareTo(quantity) < 0) {
                return false; // 库存不足
            }

            // 计算锁定后的数量
            BigDecimal newLockedQuantity = inventory.getLockedQuantity().add(quantity);
            BigDecimal newAvailableQuantity = inventory.getQuantity().subtract(newLockedQuantity);

            // 使用乐观锁更新
            int updated = inventoryMapper.lockOrUnlockInventory(
                    inventory.getId(),
                    inventory.getVersion(),
                    newLockedQuantity,
                    newAvailableQuantity
            );

            if (updated > 0) {
                // 记录库存流水
                BigDecimal beforeQuantity = inventory.getQuantity();
                saveInventoryLog(warehouseId, materialId, 3, BigDecimal.ZERO, beforeQuantity, beforeQuantity,
                        null, 3, null, null, "锁定库存");
                log.info("锁定库存成功: warehouseId={}, materialId={}, quantity={}, available={}",
                        warehouseId, materialId, quantity, newAvailableQuantity);
                return true;
            }

            // 乐观锁冲突，重试
            log.warn("锁定库存冲突，重试第{}次", i + 1);
        }

        throw new BusinessException(500, "锁定库存失败，请重试");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unlockInventory(Long warehouseId, Long materialId, BigDecimal quantity) {
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            Inventory inventory = getInventoryWithLock(warehouseId, materialId);
            if (inventory == null) {
                throw new BusinessException(1001, "库存不存在");
            }

            // 检查锁定数量是否足够解锁
            if (inventory.getLockedQuantity().compareTo(quantity) < 0) {
                throw new BusinessException(400, "锁定数量不足，无法解锁");
            }

            // 计算解锁后的数量
            BigDecimal newLockedQuantity = inventory.getLockedQuantity().subtract(quantity);
            BigDecimal newAvailableQuantity = inventory.getQuantity().subtract(newLockedQuantity);

            // 使用乐观锁更新
            int updated = inventoryMapper.lockOrUnlockInventory(
                    inventory.getId(),
                    inventory.getVersion(),
                    newLockedQuantity,
                    newAvailableQuantity
            );

            if (updated > 0) {
                // 记录库存流水
                BigDecimal beforeQuantity = inventory.getQuantity();
                saveInventoryLog(warehouseId, materialId, 4, BigDecimal.ZERO, beforeQuantity, beforeQuantity,
                        null, 4, null, null, "解锁库存");
                log.info("解锁库存成功: warehouseId={}, materialId={}, quantity={}, available={}",
                        warehouseId, materialId, quantity, newAvailableQuantity);
                return true;
            }

            // 乐观锁冲突，重试
            log.warn("解锁库存冲突，重试第{}次", i + 1);
        }

        throw new BusinessException(500, "解锁库存失败，请重试");
    }

    /**
     * 使用悲观锁查询库存（FOR UPDATE）
     */
    private Inventory getInventoryWithLock(Long warehouseId, Long materialId) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inventory::getWarehouseId, warehouseId);
        wrapper.eq(Inventory::getMaterialId, materialId);
        wrapper.last("FOR UPDATE");
        return inventoryMapper.selectOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void commitInventory(Long warehouseId, Long materialId, BigDecimal quantity,
                                String relatedNo, Long relatedId, Long operatorId) {
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            Inventory inventory = getInventoryWithLock(warehouseId, materialId);
            if (inventory == null) {
                throw new BusinessException(1001, "库存不存在");
            }

            // 检查锁定数量是否足够
            if (inventory.getLockedQuantity().compareTo(quantity) < 0) {
                throw new BusinessException(1001, "锁定库存不足");
            }

            BigDecimal beforeQuantity = inventory.getQuantity();

            // 扣减总库存和锁定数量
            BigDecimal newQuantity = inventory.getQuantity().subtract(quantity);
            BigDecimal newLockedQuantity = inventory.getLockedQuantity().subtract(quantity);
            BigDecimal newAvailableQuantity = newQuantity.subtract(newLockedQuantity);

            // 使用乐观锁更新
            int updated = inventoryMapper.commitInventory(
                    inventory.getId(),
                    inventory.getVersion(),
                    newQuantity,
                    newLockedQuantity,
                    newAvailableQuantity
            );

            if (updated > 0) {
                // 记录库存流水
                saveInventoryLog(warehouseId, materialId, 2, quantity.negate(), beforeQuantity, newQuantity,
                        relatedNo, 2, relatedId, operatorId, "出库(锁定转扣减)");
                log.info("提交库存（锁定转扣减）: warehouseId={}, materialId={}, quantity={}, before={}, after={}",
                        warehouseId, materialId, quantity, beforeQuantity, newQuantity);
                return;
            }

            // 乐观锁冲突，重试
            log.warn("提交库存冲突，重试第{}次", i + 1);
        }

        throw new BusinessException(500, "提交库存失败，请重试");
    }
}
