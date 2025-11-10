package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ct.wms.common.exception.BusinessException;
import com.ct.wms.dto.WarehouseDTO;
import com.ct.wms.entity.Dept;
import com.ct.wms.entity.User;
import com.ct.wms.entity.Warehouse;
import com.ct.wms.mapper.DeptMapper;
import com.ct.wms.mapper.UserMapper;
import com.ct.wms.mapper.WarehouseMapper;
import com.ct.wms.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 仓库Service实现类
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseMapper warehouseMapper;
    private final DeptMapper deptMapper;
    private final UserMapper userMapper;

    @Override
    public List<Warehouse> listWarehouses(Long deptId, Integer status) {
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();

        if (deptId != null) {
            wrapper.eq(Warehouse::getDeptId, deptId);
        }

        if (status != null) {
            wrapper.eq(Warehouse::getStatus, status);
        }

        wrapper.orderByDesc(Warehouse::getCreateTime);

        List<Warehouse> warehouses = warehouseMapper.selectList(wrapper);

        // 填充部门名称和管理员名称
        warehouses.forEach(warehouse -> {
            Dept dept = deptMapper.selectById(warehouse.getDeptId());
            if (dept != null) {
                warehouse.setDeptName(dept.getDeptName());
            }

            User manager = userMapper.selectById(warehouse.getManagerId());
            if (manager != null) {
                warehouse.setManagerName(manager.getRealName());
            }
        });

        return warehouses;
    }

    @Override
    public Warehouse getWarehouseById(Long id) {
        Warehouse warehouse = warehouseMapper.selectById(id);
        if (warehouse == null) {
            throw new BusinessException(404, "仓库不存在");
        }

        // 填充部门名称和管理员名称
        Dept dept = deptMapper.selectById(warehouse.getDeptId());
        if (dept != null) {
            warehouse.setDeptName(dept.getDeptName());
        }

        User manager = userMapper.selectById(warehouse.getManagerId());
        if (manager != null) {
            warehouse.setManagerName(manager.getRealName());
        }

        return warehouse;
    }

    @Override
    public Long createWarehouse(WarehouseDTO dto) {
        // 检查仓库编码是否重复
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Warehouse::getWarehouseCode, dto.getWarehouseCode());
        if (warehouseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(409, "仓库编码已存在");
        }

        // 检查部门是否存在
        Dept dept = deptMapper.selectById(dto.getDeptId());
        if (dept == null) {
            throw new BusinessException(404, "部门不存在");
        }

        // 检查管理员是否存在
        User manager = userMapper.selectById(dto.getManagerId());
        if (manager == null) {
            throw new BusinessException(404, "管理员不存在");
        }

        Warehouse warehouse = new Warehouse();
        BeanUtils.copyProperties(dto, warehouse);
        warehouse.setStatus(0); // 默认启用

        warehouseMapper.insert(warehouse);
        log.info("创建仓库成功: id={}, warehouseCode={}", warehouse.getId(), warehouse.getWarehouseCode());

        return warehouse.getId();
    }

    @Override
    public void updateWarehouse(Long id, WarehouseDTO dto) {
        Warehouse warehouse = getWarehouseById(id);

        // 检查仓库编码是否重复（排除自己）
        if (!warehouse.getWarehouseCode().equals(dto.getWarehouseCode())) {
            LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Warehouse::getWarehouseCode, dto.getWarehouseCode());
            wrapper.ne(Warehouse::getId, id);
            if (warehouseMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(409, "仓库编码已存在");
            }
        }

        BeanUtils.copyProperties(dto, warehouse);
        warehouse.setId(id);

        warehouseMapper.updateById(warehouse);
        log.info("更新仓库成功: id={}, warehouseCode={}", id, warehouse.getWarehouseCode());
    }

    @Override
    public void deleteWarehouse(Long id) {
        Warehouse warehouse = getWarehouseById(id);

        // TODO: 检查仓库是否有库存

        warehouseMapper.deleteById(id);
        log.info("删除仓库成功: id={}, warehouseCode={}", id, warehouse.getWarehouseCode());
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Warehouse warehouse = getWarehouseById(id);

        warehouse.setStatus(status);
        warehouseMapper.updateById(warehouse);

        log.info("更新仓库状态成功: id={}, status={}", id, status);
    }
}
