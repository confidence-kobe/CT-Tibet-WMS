package com.ct.wms.util;

import com.ct.wms.common.enums.*;
import com.ct.wms.entity.*;
import com.ct.wms.security.UserDetailsImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

/**
 * 测试数据构建工具类
 *
 * @author CT Development Team
 * @since 2025-11-16
 */
public class TestDataBuilder {

    /**
     * 创建测试用户
     */
    public static User createUser(Long id, String username, Long deptId, Long roleId) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword("$2a$10$test.encrypted.password");
        user.setRealName("测试用户" + id);
        user.setDeptId(deptId);
        user.setRoleId(roleId);
        user.setPhone("138" + String.format("%08d", id));
        user.setEmail("test" + id + "@example.com");
        user.setStatus(UserStatus.ENABLED);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        return user;
    }

    /**
     * 创建测试部门
     */
    public static Dept createDept(Long id, String deptName) {
        Dept dept = new Dept();
        dept.setId(id);
        dept.setDeptName(deptName);
        dept.setDeptCode("DEPT" + id);
        dept.setCreateTime(LocalDateTime.now());
        dept.setUpdateTime(LocalDateTime.now());
        return dept;
    }

    /**
     * 创建测试角色
     */
    public static Role createRole(Long id, String roleName, String roleCode) {
        Role role = new Role();
        role.setId(id);
        role.setRoleName(roleName);
        role.setRoleCode(roleCode);
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        return role;
    }

    /**
     * 创建测试物资
     */
    public static Material createMaterial(Long id, String name, String category, BigDecimal price) {
        Material material = new Material();
        material.setId(id);
        material.setMaterialName(name);
        material.setMaterialCode("MAT" + id);
        material.setCategory(category);
        material.setUnit("个");
        material.setPrice(price);
        material.setMinStock(BigDecimal.valueOf(10));
        material.setCreateTime(LocalDateTime.now());
        material.setUpdateTime(LocalDateTime.now());
        return material;
    }

    /**
     * 创建测试仓库
     */
    public static Warehouse createWarehouse(Long id, String name, Long deptId) {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(id);
        warehouse.setWarehouseName(name);
        warehouse.setWarehouseCode("WH" + id);
        warehouse.setDeptId(deptId);
        warehouse.setAddress("测试地址" + id);
        warehouse.setCreateTime(LocalDateTime.now());
        warehouse.setUpdateTime(LocalDateTime.now());
        return warehouse;
    }

    /**
     * 创建测试入库单
     */
    public static Inbound createInbound(Long id, Long warehouseId, LocalDateTime inboundTime,
                                       BigDecimal totalAmount) {
        Inbound inbound = new Inbound();
        inbound.setId(id);
        inbound.setInboundNo("IB" + System.currentTimeMillis() + id);
        inbound.setWarehouseId(warehouseId);
        inbound.setInboundTime(inboundTime);
        inbound.setTotalAmount(totalAmount);
        inbound.setOperatorId(1L);
        inbound.setRemark("测试入库单");
        inbound.setCreateTime(LocalDateTime.now());
        inbound.setUpdateTime(LocalDateTime.now());
        return inbound;
    }

    /**
     * 创建测试入库明细
     */
    public static InboundDetail createInboundDetail(Long id, Long inboundId, Long materialId,
                                                   BigDecimal quantity, BigDecimal unitPrice) {
        InboundDetail detail = new InboundDetail();
        detail.setId(id);
        detail.setInboundId(inboundId);
        detail.setMaterialId(materialId);
        detail.setQuantity(quantity);
        detail.setUnitPrice(unitPrice);
        detail.setAmount(quantity.multiply(unitPrice));
        detail.setCreateTime(LocalDateTime.now());
        detail.setUpdateTime(LocalDateTime.now());
        return detail;
    }

    /**
     * 创建测试出库单
     */
    public static Outbound createOutbound(Long id, Long warehouseId, OutboundSource source,
                                         LocalDateTime outboundTime, BigDecimal totalAmount) {
        Outbound outbound = new Outbound();
        outbound.setId(id);
        outbound.setOutboundNo("OB" + System.currentTimeMillis() + id);
        outbound.setWarehouseId(warehouseId);
        outbound.setSource(source);
        outbound.setOutboundTime(outboundTime);
        outbound.setTotalAmount(totalAmount);
        outbound.setOperatorId(1L);
        outbound.setRemark("测试出库单");
        outbound.setCreateTime(LocalDateTime.now());
        outbound.setUpdateTime(LocalDateTime.now());
        return outbound;
    }

    /**
     * 创建测试出库明细
     */
    public static OutboundDetail createOutboundDetail(Long id, Long outboundId, Long materialId,
                                                     BigDecimal quantity, BigDecimal unitPrice) {
        OutboundDetail detail = new OutboundDetail();
        detail.setId(id);
        detail.setOutboundId(outboundId);
        detail.setMaterialId(materialId);
        detail.setQuantity(quantity);
        detail.setUnitPrice(unitPrice);
        detail.setAmount(quantity.multiply(unitPrice));
        detail.setCreateTime(LocalDateTime.now());
        detail.setUpdateTime(LocalDateTime.now());
        return detail;
    }

    /**
     * 创建测试库存
     */
    public static Inventory createInventory(Long id, Long warehouseId, Long materialId,
                                           BigDecimal quantity) {
        Inventory inventory = new Inventory();
        inventory.setId(id);
        inventory.setWarehouseId(warehouseId);
        inventory.setMaterialId(materialId);
        inventory.setQuantity(quantity);
        inventory.setCreateTime(LocalDateTime.now());
        inventory.setUpdateTime(LocalDateTime.now());
        return inventory;
    }

    /**
     * 创建测试消息
     */
    public static Message createMessage(Long id, Long receiverId, MessageType type,
                                       String title, String content, Integer isRead) {
        Message message = new Message();
        message.setId(id);
        message.setReceiverId(receiverId);
        message.setType(type);
        message.setTitle(title);
        message.setContent(content);
        message.setIsRead(isRead);
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());
        return message;
    }

    /**
     * 创建测试申请单
     */
    public static Apply createApply(Long id, Long applicantId, Long warehouseId,
                                   ApplyStatus status) {
        Apply apply = new Apply();
        apply.setId(id);
        apply.setApplyNo("AP" + System.currentTimeMillis() + id);
        apply.setApplicantId(applicantId);
        apply.setWarehouseId(warehouseId);
        apply.setStatus(status);
        apply.setRemark("测试申请单");
        apply.setCreateTime(LocalDateTime.now());
        apply.setUpdateTime(LocalDateTime.now());
        return apply;
    }

    /**
     * 模拟登录用户到SecurityContext
     */
    public static void mockSecurityContext(Long userId, String username, String roleCode) {
        UserDetailsImpl userDetails = new UserDetailsImpl();
        userDetails.setId(userId);
        userDetails.setUsername(username);
        userDetails.setPassword("test");
        userDetails.setAuthorities(Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + roleCode)
        ));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    /**
     * 清除SecurityContext
     */
    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}
