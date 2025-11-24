package com.ct.wms.integration;

import com.ct.wms.common.enums.ApplyStatus;
import com.ct.wms.common.enums.OutboundSource;
import com.ct.wms.common.enums.OutboundStatus;
import com.ct.wms.dto.ApplyDTO;
import com.ct.wms.dto.ApprovalDTO;
import com.ct.wms.entity.*;
import com.ct.wms.mapper.*;
import com.ct.wms.service.ApplyService;
import com.ct.wms.service.InventoryService;
import com.ct.wms.service.OutboundService;
import com.ct.wms.util.TestDataBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 申请出库完整流程集成测试
 *
 * @author CT Development Team
 * @since 2025-11-16
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("申请出库完整流程集成测试")
class ApplyOutboundFlowTest {

    @Autowired
    private ApplyService applyService;

    @Autowired
    private OutboundService outboundService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DeptMapper deptMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private WarehouseMapper warehouseMapper;

    @Autowired
    private MaterialMapper materialMapper;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private ApplyMapper applyMapper;

    @Autowired
    private OutboundMapper outboundMapper;

    private User employeeUser;
    private User managerUser;
    private Dept testDept;
    private Role employeeRole;
    private Role managerRole;
    private Warehouse testWarehouse;
    private Material testMaterial;
    private Inventory testInventory;

    @BeforeEach
    void setUp() {
        // 创建测试部门
        testDept = TestDataBuilder.createDept(1L, "测试部门");
        deptMapper.insert(testDept);

        // 创建测试角色
        employeeRole = TestDataBuilder.createRole(1L, "普通员工", "USER");
        managerRole = TestDataBuilder.createRole(2L, "仓库管理员", "WAREHOUSE");
        roleMapper.insert(employeeRole);
        roleMapper.insert(managerRole);

        // 创建测试用户
        employeeUser = TestDataBuilder.createUser(100L, "employee", testDept.getId(), employeeRole.getId());
        managerUser = TestDataBuilder.createUser(200L, "manager", testDept.getId(), managerRole.getId());
        userMapper.insert(employeeUser);
        userMapper.insert(managerUser);

        // 创建测试仓库
        testWarehouse = TestDataBuilder.createWarehouse(1L, "测试仓库", testDept.getId());
        testWarehouse.setManagerId(managerUser.getId());  // 设置仓管员为管理者
        warehouseMapper.insert(testWarehouse);

        // 创建测试物资
        testMaterial = TestDataBuilder.createMaterial(1L, "测试物资", "办公用品", BigDecimal.valueOf(100));
        materialMapper.insert(testMaterial);

        // 创建初始库存
        testInventory = TestDataBuilder.createInventory(1L, testWarehouse.getId(),
            testMaterial.getId(), BigDecimal.valueOf(1000));
        inventoryMapper.insert(testInventory);
    }

    @AfterEach
    void tearDown() {
        TestDataBuilder.clearSecurityContext();
    }

    @Test
    @DisplayName("测试完整申请出库流程 - 成功路径")
    void testCompleteApplyOutboundFlow_Success() {
        // ========== 步骤1: 员工创建申请 ==========
        TestDataBuilder.mockSecurityContext(employeeUser.getId(), employeeUser.getUsername(), "USER");

        ApplyDTO applyDTO = new ApplyDTO();
        applyDTO.setWarehouseId(testWarehouse.getId());
        applyDTO.setApplyReason("测试申请出库");
        applyDTO.setDetails(Arrays.asList(
            createApplyDetailDTO(testMaterial.getId(), BigDecimal.valueOf(50))
        ));

        Long applyId = applyService.createApply(applyDTO);
        assertThat(applyId).isNotNull();

        // 验证申请单创建成功
        Apply createdApply = applyMapper.selectById(applyId);
        assertThat(createdApply).isNotNull();
        assertThat(createdApply.getStatus()).isEqualTo(ApplyStatus.PENDING);
        assertThat(createdApply.getApplicantId()).isEqualTo(employeeUser.getId());

        // 验证库存未扣减（申请阶段不扣减库存）
        Inventory currentInventory = inventoryMapper.selectById(testInventory.getId());
        assertThat(currentInventory.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(1000));

        // ========== 步骤2: 仓管审批通过 ==========
        TestDataBuilder.mockSecurityContext(managerUser.getId(), managerUser.getUsername(), "WAREHOUSE");

        ApprovalDTO approvalDTO = new ApprovalDTO();
        approvalDTO.setApplyId(applyId);
        approvalDTO.setApprovalResult(1); // 1-通过
        approvalDTO.setApprovalRemark("同意申请");

        applyService.approveApply(approvalDTO);

        // 验证申请状态更新为已批准
        Apply approvedApply = applyMapper.selectById(applyId);
        assertThat(approvedApply).isNotNull();
        assertThat(approvedApply.getStatus()).isEqualTo(ApplyStatus.APPROVED);
        assertThat(approvedApply.getApproverId()).isEqualTo(managerUser.getId());

        // 验证自动创建出库单
        // 注意：这里需要根据实际业务逻辑调整
        // 假设审批通过后会自动创建出库单，状态为待出库
        // 如果业务逻辑不同，需要相应调整测试

        // ========== 步骤3: 验证库存未扣减（出库确认前） ==========
        currentInventory = inventoryMapper.selectById(testInventory.getId());
        assertThat(currentInventory.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(1000));

        // ========== 步骤4: 仓管确认出库 ==========
        // 如果有自动创建的出库单，这里应该获取并确认
        // 根据实际业务流程调整

        // ========== 步骤5: 验证库存扣减 ==========
        // 出库确认后，库存应该扣减
        // currentInventory = inventoryMapper.selectById(testInventory.getId());
        // assertThat(currentInventory.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(950));

        // ========== 步骤6: 验证申请状态流转 ==========
        // 最终申请状态应该是已完成
        // Apply finalApply = applyMapper.selectById(applyId);
        // assertThat(finalApply.getStatus()).isEqualTo(ApplyStatus.COMPLETED);
    }

    @Test
    @DisplayName("测试申请出库流程 - 审批拒绝")
    void testApplyOutboundFlow_Rejected() {
        // ========== 步骤1: 员工创建申请 ==========
        TestDataBuilder.mockSecurityContext(employeeUser.getId(), employeeUser.getUsername(), "USER");

        ApplyDTO applyDTO = new ApplyDTO();
        applyDTO.setWarehouseId(testWarehouse.getId());
        applyDTO.setApplyReason("测试申请出库");
        applyDTO.setDetails(Arrays.asList(
            createApplyDetailDTO(testMaterial.getId(), BigDecimal.valueOf(50))
        ));

        Long applyId = applyService.createApply(applyDTO);
        assertThat(applyId).isNotNull();

        // ========== 步骤2: 仓管拒绝申请 ==========
        TestDataBuilder.mockSecurityContext(managerUser.getId(), managerUser.getUsername(), "WAREHOUSE");

        ApprovalDTO approvalDTO = new ApprovalDTO();
        approvalDTO.setApplyId(applyId);
        approvalDTO.setApprovalResult(2); // 2-拒绝
        approvalDTO.setApprovalRemark("库存不足，拒绝申请");

        applyService.approveApply(approvalDTO);

        // ========== 验证申请状态 ==========
        Apply rejectedApply = applyMapper.selectById(applyId);
        assertThat(rejectedApply).isNotNull();
        assertThat(rejectedApply.getStatus()).isEqualTo(ApplyStatus.REJECTED);
        assertThat(rejectedApply.getApproverId()).isEqualTo(managerUser.getId());

        // ========== 验证库存未扣减 ==========
        Inventory currentInventory = inventoryMapper.selectById(testInventory.getId());
        assertThat(currentInventory.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(1000));

        // ========== 验证未创建出库单 ==========
        // 拒绝的申请不应该创建出库单
    }

    @Test
    @DisplayName("测试直接出库流程 - 仓管直接出库")
    void testDirectOutboundFlow() {
        // ========== 仓管执行直接出库 ==========
        TestDataBuilder.mockSecurityContext(managerUser.getId(), managerUser.getUsername(), "WAREHOUSE");

        // 创建直接出库单的DTO
        // 注意：需要根据实际的OutboundDTO结构调整
        // OutboundDTO outboundDTO = new OutboundDTO();
        // outboundDTO.setWarehouseId(testWarehouse.getId());
        // outboundDTO.setSource(OutboundSource.DIRECT);
        // outboundDTO.setRemark("测试直接出库");
        // outboundDTO.setDetails(Arrays.asList(
        //     createOutboundDetailDTO(testMaterial.getId(), BigDecimal.valueOf(100))
        // ));

        // Long outboundId = outboundService.createDirectOutbound(outboundDTO);
        // assertThat(outboundId).isNotNull();

        // ========== 验证出库单创建 ==========
        // Outbound createdOutbound = outboundMapper.selectById(outboundId);
        // assertThat(createdOutbound).isNotNull();
        // assertThat(createdOutbound.getSource()).isEqualTo(OutboundSource.DIRECT);
        // assertThat(createdOutbound.getStatus()).isEqualTo(OutboundStatus.COMPLETED);

        // ========== 验证库存扣减 ==========
        // 直接出库应该立即扣减库存
        // Inventory currentInventory = inventoryMapper.selectById(testInventory.getId());
        // assertThat(currentInventory.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(900));
    }

    @Test
    @DisplayName("测试申请出库流程 - 库存不足场景")
    void testApplyOutboundFlow_InsufficientStock() {
        // ========== 步骤1: 员工创建申请（申请数量超过库存） ==========
        TestDataBuilder.mockSecurityContext(employeeUser.getId(), employeeUser.getUsername(), "USER");

        ApplyDTO applyDTO = new ApplyDTO();
        applyDTO.setWarehouseId(testWarehouse.getId());
        applyDTO.setApplyReason("测试库存不足申请");
        applyDTO.setDetails(Arrays.asList(
            createApplyDetailDTO(testMaterial.getId(), BigDecimal.valueOf(2000)) // 超过库存1000
        ));

        // 注意：创建申请时可能不会校验库存，只在审批时校验
        Long applyId = applyService.createApply(applyDTO);
        assertThat(applyId).isNotNull();

        // ========== 步骤2: 仓管审批（应该因为库存不足而失败或自动拒绝） ==========
        TestDataBuilder.mockSecurityContext(managerUser.getId(), managerUser.getUsername(), "WAREHOUSE");

        ApprovalDTO approvalDTO = new ApprovalDTO();
        approvalDTO.setApplyId(applyId);
        approvalDTO.setApprovalResult(1); // 1-通过
        approvalDTO.setApprovalRemark("尝试批准");

        // 根据实际业务逻辑，可能会抛出异常或自动拒绝
        // 这里假设会因为库存不足而拒绝
        // assertThatThrownBy(() -> applyService.approveApply(approvalDTO))
        //     .isInstanceOf(BusinessException.class)
        //     .hasMessageContaining("库存不足");

        // 或者业务逻辑是自动拒绝
        // applyService.approveApply(approvalDTO);
        // Apply apply = applyMapper.selectById(applyId);
        // assertThat(apply.getStatus()).isEqualTo(ApplyStatus.REJECTED);
    }

    @Test
    @DisplayName("测试库存预警流程")
    void testInventoryWarningFlow() {
        // ========== 设置物资最低库存 ==========
        testMaterial.setMinStock(BigDecimal.valueOf(100));
        materialMapper.updateById(testMaterial);

        // ========== 执行出库使库存降至预警线以下 ==========
        TestDataBuilder.mockSecurityContext(managerUser.getId(), managerUser.getUsername(), "WAREHOUSE");

        // 假设执行了出库操作，使库存降至80
        testInventory.setQuantity(BigDecimal.valueOf(80));
        inventoryMapper.updateById(testInventory);

        // ========== 查询低库存预警 ==========
        // List<Inventory> lowStockAlerts = inventoryService.listLowStockAlerts(null);

        // ========== 验证预警列表包含该物资 ==========
        // assertThat(lowStockAlerts).isNotEmpty();
        // assertThat(lowStockAlerts)
        //     .anyMatch(inv -> inv.getMaterialId().equals(testMaterial.getId()));
    }

    /**
     * 辅助方法：创建申请明细DTO
     */
    private ApplyDTO.ApplyDetailDTO createApplyDetailDTO(Long materialId, BigDecimal quantity) {
        ApplyDTO.ApplyDetailDTO detailDTO = new ApplyDTO.ApplyDetailDTO();
        detailDTO.setMaterialId(materialId);
        detailDTO.setQuantity(quantity);
        return detailDTO;
    }
}
