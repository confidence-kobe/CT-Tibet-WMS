package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ct.wms.common.exception.BusinessException;
import com.ct.wms.dto.InboundDTO;
import com.ct.wms.dto.InboundUpdateDTO;
import com.ct.wms.entity.*;
import com.ct.wms.mapper.*;
import com.ct.wms.security.UserDetailsImpl;
import com.ct.wms.service.InboundService;
import com.ct.wms.service.InventoryService;
import com.ct.wms.utils.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 入库Service实现类
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InboundServiceImpl implements InboundService {

    private final InboundMapper inboundMapper;
    private final InboundDetailMapper inboundDetailMapper;
    private final WarehouseMapper warehouseMapper;
    private final MaterialMapper materialMapper;
    private final DeptMapper deptMapper;
    private final UserMapper userMapper;
    private final InventoryService inventoryService;
    private final IdGenerator idGenerator;

    @Override
    public Page<Inbound> listInbounds(Integer pageNum, Integer pageSize, Long warehouseId,
                                     Integer inboundType, String startDate, String endDate,
                                     Long operatorId, String keyword) {
        Page<Inbound> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Inbound> wrapper = new LambdaQueryWrapper<>();

        if (warehouseId != null) {
            wrapper.eq(Inbound::getWarehouseId, warehouseId);
        }

        if (inboundType != null) {
            wrapper.eq(Inbound::getInboundType, inboundType);
        }

        if (StringUtils.hasText(startDate)) {
            wrapper.ge(Inbound::getInboundTime, startDate + " 00:00:00");
        }

        if (StringUtils.hasText(endDate)) {
            wrapper.le(Inbound::getInboundTime, endDate + " 23:59:59");
        }

        if (operatorId != null) {
            wrapper.eq(Inbound::getOperatorId, operatorId);
        }

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Inbound::getInboundNo, keyword);
        }

        wrapper.orderByDesc(Inbound::getCreateTime);

        Page<Inbound> result = inboundMapper.selectPage(page, wrapper);

        // 填充关联数据（防御性检查，避免空指针）
        if (result != null && result.getRecords() != null && !result.getRecords().isEmpty()) {
            result.getRecords().forEach(this::fillInboundInfo);
        }

        return result;
    }

    @Override
    public Inbound getInboundById(Long id) {
        Inbound inbound = inboundMapper.selectById(id);
        if (inbound == null) {
            throw new BusinessException(404, "入库单不存在");
        }

        // 填充关联数据
        fillInboundInfo(inbound);

        // 查询明细
        LambdaQueryWrapper<InboundDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InboundDetail::getInboundId, id);
        List<InboundDetail> details = inboundDetailMapper.selectList(wrapper);

        // 填充明细物资信息
        details.forEach(detail -> {
            Material material = materialMapper.selectById(detail.getMaterialId());
            if (material != null) {
                detail.setMaterialName(material.getMaterialName());
                detail.setMaterialCode(material.getMaterialCode());
                detail.setSpec(material.getSpec());
                detail.setUnit(material.getUnit());
            }
        });

        inbound.setDetails(details);

        return inbound;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createInbound(InboundDTO dto) {
        // 获取当前用户
        Long operatorId = getCurrentUserId();
        User operator = userMapper.selectById(operatorId);

        // 检查仓库是否存在
        Warehouse warehouse = warehouseMapper.selectById(dto.getWarehouseId());
        if (warehouse == null) {
            throw new BusinessException(404, "仓库不存在");
        }

        // 查询部门信息
        Dept dept = deptMapper.selectById(warehouse.getDeptId());
        if (dept == null) {
            throw new BusinessException(404, "部门不存在");
        }

        // 生成入库单号: RK_部门编码_YYYYMMDD_流水号
        String inboundNo = generateInboundNo(dept.getDeptCode());

        // 计算总金额
        BigDecimal totalAmount = dto.getDetails().stream()
                .map(detail -> {
                    BigDecimal unitPrice = detail.getUnitPrice() != null ?
                            detail.getUnitPrice() : BigDecimal.ZERO;
                    return unitPrice.multiply(detail.getQuantity());
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 创建入库单
        Inbound inbound = new Inbound();
        inbound.setInboundNo(inboundNo);
        inbound.setWarehouseId(dto.getWarehouseId());
        inbound.setInboundType(dto.getInboundType());
        inbound.setOperatorId(operatorId);
        inbound.setOperatorName(operator != null ? operator.getRealName() : null);
        inbound.setInboundTime(dto.getInboundTime());
        inbound.setTotalAmount(totalAmount);
        inbound.setRemark(dto.getRemark());
        inbound.setStatus(Inbound.STATUS_NORMAL);

        inboundMapper.insert(inbound);
        log.info("创建入库单: id={}, inboundNo={}", inbound.getId(), inboundNo);

        // 创建入库明细并更新库存
        for (InboundDTO.InboundDetailDTO detailDTO : dto.getDetails()) {
            // 检查物资是否存在
            Material material = materialMapper.selectById(detailDTO.getMaterialId());
            if (material == null) {
                throw new BusinessException(404, "物资不存在: " + detailDTO.getMaterialId());
            }

            // 创建明细
            InboundDetail detail = new InboundDetail();
            detail.setInboundId(inbound.getId());
            detail.setMaterialId(detailDTO.getMaterialId());
            detail.setMaterialName(material.getMaterialName());
            detail.setMaterialCode(material.getMaterialCode());
            detail.setSpec(material.getSpec());
            detail.setUnit(material.getUnit());
            detail.setQuantity(detailDTO.getQuantity());
            detail.setUnitPrice(detailDTO.getUnitPrice());

            BigDecimal amount = detailDTO.getUnitPrice() != null ?
                    detailDTO.getUnitPrice().multiply(detailDTO.getQuantity()) : BigDecimal.ZERO;
            detail.setAmount(amount);
            detail.setRemark(detailDTO.getRemark());

            inboundDetailMapper.insert(detail);

            // 更新库存（增加）
            inventoryService.increaseInventory(
                    dto.getWarehouseId(),
                    detailDTO.getMaterialId(),
                    detailDTO.getQuantity(),
                    inboundNo,
                    inbound.getId(),
                    operatorId
            );

            log.info("入库明细: materialId={}, quantity={}", detailDTO.getMaterialId(), detailDTO.getQuantity());
        }

        log.info("入库单创建成功: inboundNo={}, totalAmount={}", inboundNo, totalAmount);

        return inbound.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInbound(Long id, InboundUpdateDTO dto) {
        Inbound inbound = getEditableInbound(id);

        List<InboundDetail> details = inboundDetailMapper.selectList(
                new LambdaQueryWrapper<InboundDetail>().eq(InboundDetail::getInboundId, id));
        Map<Long, InboundDetail> detailMap = details.stream()
                .collect(Collectors.toMap(InboundDetail::getId, Function.identity()));

        // 更新明细单价/备注（数量和物资不允许修改）
        if (dto.getDetails() != null) {
            for (InboundUpdateDTO.DetailUpdateDTO item : dto.getDetails()) {
                InboundDetail detail = detailMap.get(item.getId());
                if (detail == null) {
                    throw new BusinessException(400, "明细不属于该入库单: " + item.getId());
                }
                detail.setUnitPrice(item.getUnitPrice());
                detail.setAmount(item.getUnitPrice() != null
                        ? item.getUnitPrice().multiply(detail.getQuantity()) : BigDecimal.ZERO);
                detail.setRemark(item.getRemark());
                inboundDetailMapper.updateById(detail);
            }
        }

        // 总金额按全部明细重新计算
        BigDecimal totalAmount = details.stream()
                .map(d -> d.getAmount() != null ? d.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        inbound.setInboundType(dto.getInboundType());
        inbound.setInboundTime(dto.getInboundTime());
        inbound.setRemark(dto.getRemark());
        inbound.setTotalAmount(totalAmount);
        inboundMapper.updateById(inbound);

        log.info("编辑入库单: id={}, inboundNo={}, totalAmount={}", id, inbound.getInboundNo(), totalAmount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void voidInbound(Long id, String reason) {
        Inbound inbound = getEditableInbound(id);
        Long operatorId = getCurrentUserId();

        // 作废原因记在备注中，便于在单据上直接看到
        String voidNote = "【作废原因】" + reason.trim();
        String remark = StringUtils.hasText(inbound.getRemark())
                ? inbound.getRemark() + " " + voidNote : voidNote;
        if (remark.length() > 500) {
            remark = remark.substring(remark.length() - 500);
        }

        // 条件更新：只有未作废的单据才能作废，防止并发重复作废导致库存被扣两次
        int updated = inboundMapper.update(null, new LambdaUpdateWrapper<Inbound>()
                .set(Inbound::getStatus, Inbound.STATUS_VOID)
                .set(Inbound::getRemark, remark)
                .eq(Inbound::getId, id)
                .and(w -> w.ne(Inbound::getStatus, Inbound.STATUS_VOID).or().isNull(Inbound::getStatus)));
        if (updated == 0) {
            throw new BusinessException(400, "入库单已作废");
        }

        // 逐条回退库存，任一物资库存不足则整体回滚
        List<InboundDetail> details = inboundDetailMapper.selectList(
                new LambdaQueryWrapper<InboundDetail>().eq(InboundDetail::getInboundId, id));
        for (InboundDetail detail : details) {
            inventoryService.revertInboundInventory(inbound.getWarehouseId(), detail.getMaterialId(),
                    detail.getQuantity(), inbound.getInboundNo(), id, operatorId);
        }

        log.info("作废入库单: id={}, inboundNo={}, operatorId={}, reason={}",
                id, inbound.getInboundNo(), operatorId, reason);
    }

    /**
     * 查询可修改的入库单：存在、未作废、且当前用户有权操作该仓库
     */
    private Inbound getEditableInbound(Long id) {
        Inbound inbound = inboundMapper.selectById(id);
        if (inbound == null) {
            throw new BusinessException(404, "入库单不存在");
        }
        if (Integer.valueOf(Inbound.STATUS_VOID).equals(inbound.getStatus())) {
            throw new BusinessException(400, "入库单已作废，不能再修改");
        }
        checkWarehousePermission(inbound.getWarehouseId());
        return inbound;
    }

    /**
     * 系统管理员可操作所有仓库；部门管理员和仓管员只能操作本部门仓库
     */
    private void checkWarehousePermission(Long warehouseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
        if (isAdmin) {
            return;
        }

        User currentUser = userMapper.selectById(getCurrentUserId());
        Warehouse warehouse = warehouseMapper.selectById(warehouseId);
        if (currentUser == null || warehouse == null || currentUser.getDeptId() == null
                || !currentUser.getDeptId().equals(warehouse.getDeptId())) {
            throw new BusinessException(403, "无权操作其他部门仓库的入库单");
        }
    }

    /**
     * 填充入库单关联信息
     */
    private void fillInboundInfo(Inbound inbound) {
        // 填充仓库名称
        Warehouse warehouse = warehouseMapper.selectById(inbound.getWarehouseId());
        if (warehouse != null) {
            inbound.setWarehouseName(warehouse.getWarehouseName());
        }

        // 填充操作人姓名
        User operator = userMapper.selectById(inbound.getOperatorId());
        if (operator != null) {
            inbound.setOperatorName(operator.getRealName());
        }
    }

    /**
     * 生成入库单号（线程安全）
     * 注意：实际生产环境建议使用分布式ID生成器或数据库序列
     */
    private String generateInboundNo(String deptCode) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "RK_" + deptCode + "_" + today + "_";
        // 使用雪花算法生成的ID作为流水号
        long sequence = idGenerator.nextId() % 100000;
        return prefix + String.format("%05d", sequence);
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return userDetails.getId();
        }
        throw new BusinessException(401, "未登录");
    }
}
