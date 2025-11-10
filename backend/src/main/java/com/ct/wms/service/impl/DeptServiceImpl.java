package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ct.wms.common.exception.BusinessException;
import com.ct.wms.dto.DeptDTO;
import com.ct.wms.entity.Dept;
import com.ct.wms.entity.User;
import com.ct.wms.mapper.DeptMapper;
import com.ct.wms.mapper.UserMapper;
import com.ct.wms.service.DeptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门Service实现类
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeptServiceImpl implements DeptService {

    private final DeptMapper deptMapper;
    private final UserMapper userMapper;

    @Override
    public List<Dept> listDeptTree() {
        // 查询所有部门
        LambdaQueryWrapper<Dept> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Dept::getParentId, Dept::getId);
        List<Dept> allDepts = deptMapper.selectList(wrapper);

        // 填充关联信息
        allDepts.forEach(this::fillDeptInfo);

        // 构建树形结构
        return buildDeptTree(allDepts, 0L);
    }

    @Override
    public List<Dept> listAllDepts() {
        LambdaQueryWrapper<Dept> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Dept::getParentId, Dept::getId);
        List<Dept> depts = deptMapper.selectList(wrapper);

        // 填充关联信息
        depts.forEach(this::fillDeptInfo);

        return depts;
    }

    @Override
    public Dept getDeptById(Long id) {
        Dept dept = deptMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException(404, "部门不存在");
        }

        // 填充关联信息
        fillDeptInfo(dept);

        return dept;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDept(DeptDTO dto) {
        // 检查部门编码是否存在
        if (checkDeptCodeExists(dto.getDeptCode(), null)) {
            throw new BusinessException(400, "部门编码已存在");
        }

        // 如果有上级部门，检查是否存在
        if (dto.getParentId() != null && dto.getParentId() > 0) {
            Dept parent = deptMapper.selectById(dto.getParentId());
            if (parent == null) {
                throw new BusinessException(404, "上级部门不存在");
            }
        }

        // 创建部门
        Dept dept = new Dept();
        dept.setDeptName(dto.getDeptName());
        dept.setDeptCode(dto.getDeptCode().toUpperCase());
        dept.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        dept.setLeaderId(dto.getLeaderId());
        dept.setPhone(dto.getPhone());
        dept.setRemark(dto.getRemark());

        deptMapper.insert(dept);

        log.info("创建部门: id={}, deptCode={}", dept.getId(), dept.getDeptCode());

        return dept.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDept(Long id, DeptDTO dto) {
        // 查询部门
        Dept dept = deptMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException(404, "部门不存在");
        }

        // 检查部门编码是否重复
        if (!dept.getDeptCode().equals(dto.getDeptCode().toUpperCase())) {
            if (checkDeptCodeExists(dto.getDeptCode(), id)) {
                throw new BusinessException(400, "部门编码已存在");
            }
        }

        // 如果有上级部门，检查是否存在
        if (dto.getParentId() != null && dto.getParentId() > 0) {
            // 不能设置自己为上级部门
            if (dto.getParentId().equals(id)) {
                throw new BusinessException(400, "不能设置自己为上级部门");
            }

            Dept parent = deptMapper.selectById(dto.getParentId());
            if (parent == null) {
                throw new BusinessException(404, "上级部门不存在");
            }

            // 检查是否会形成循环引用（简单检查：上级部门的parentId不能是当前部门）
            if (parent.getParentId().equals(id)) {
                throw new BusinessException(400, "不能形成循环引用");
            }
        }

        // 更新部门
        dept.setDeptName(dto.getDeptName());
        dept.setDeptCode(dto.getDeptCode().toUpperCase());
        dept.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        dept.setLeaderId(dto.getLeaderId());
        dept.setPhone(dto.getPhone());
        dept.setRemark(dto.getRemark());

        deptMapper.updateById(dept);

        log.info("更新部门: id={}, deptCode={}", id, dept.getDeptCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDept(Long id) {
        // 查询部门
        Dept dept = deptMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException(404, "部门不存在");
        }

        // 检查是否有子部门
        LambdaQueryWrapper<Dept> deptWrapper = new LambdaQueryWrapper<>();
        deptWrapper.eq(Dept::getParentId, id);
        long deptCount = deptMapper.selectCount(deptWrapper);

        if (deptCount > 0) {
            throw new BusinessException(400, "该部门下还有子部门，无法删除");
        }

        // 检查是否有用户
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getDeptId, id);
        long userCount = userMapper.selectCount(userWrapper);

        if (userCount > 0) {
            throw new BusinessException(400, "该部门下还有用户，无法删除");
        }

        // 删除部门
        deptMapper.deleteById(id);

        log.info("删除部门: id={}, deptCode={}", id, dept.getDeptCode());
    }

    @Override
    public boolean checkDeptCodeExists(String deptCode, Long excludeId) {
        LambdaQueryWrapper<Dept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dept::getDeptCode, deptCode.toUpperCase());

        if (excludeId != null) {
            wrapper.ne(Dept::getId, excludeId);
        }

        return deptMapper.selectCount(wrapper) > 0;
    }

    /**
     * 填充部门关联信息
     */
    private void fillDeptInfo(Dept dept) {
        // 填充负责人姓名
        if (dept.getLeaderId() != null) {
            User leader = userMapper.selectById(dept.getLeaderId());
            if (leader != null) {
                dept.setLeaderName(leader.getRealName());
            }
        }
    }

    /**
     * 构建部门树
     *
     * @param allDepts 所有部门
     * @param parentId 父部门ID
     * @return 部门树
     */
    private List<Dept> buildDeptTree(List<Dept> allDepts, Long parentId) {
        List<Dept> tree = new ArrayList<>();

        for (Dept dept : allDepts) {
            if (dept.getParentId().equals(parentId)) {
                // 递归查找子部门
                List<Dept> children = buildDeptTree(allDepts, dept.getId());
                dept.setChildren(children);
                tree.add(dept);
            }
        }

        return tree;
    }
}
