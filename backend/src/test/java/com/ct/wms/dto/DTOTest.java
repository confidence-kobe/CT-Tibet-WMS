package com.ct.wms.dto;

import org.junit.jupiter.api.Test;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import static org.junit.jupiter.api.Assertions.*;

/**
 * DTO测试
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
public class DTOTest {

    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();

    @Test
    public void testLoginRequestValidation() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("123456");
        
        var violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testLoginRequestUsernameRequired() {
        LoginRequest request = new LoginRequest();
        request.setPassword("123456");
        
        var violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testLoginRequestPasswordRequired() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        
        var violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testUserDTOSetters() {
        UserDTO dto = new UserDTO();
        dto.setUsername("testuser");
        dto.setRealName("测试用户");
        dto.setPhone("13800000000");
        dto.setDeptId(1L);
        dto.setRoleId(1L);
        
        assertEquals("testuser", dto.getUsername());
        assertEquals("测试用户", dto.getRealName());
        assertEquals("13800000000", dto.getPhone());
    }

    @Test
    public void testMaterialDTOSetters() {
        MaterialDTO dto = new MaterialDTO();
        dto.setMaterialName("测试物资");
        dto.setMaterialCode("M001");
        dto.setCategory("电子类");
        dto.setSpec("10*10");
        dto.setUnit("个");
        
        assertEquals("测试物资", dto.getMaterialName());
        assertEquals("M001", dto.getMaterialCode());
    }

    @Test
    public void testWarehouseDTOSetters() {
        WarehouseDTO dto = new WarehouseDTO();
        dto.setWarehouseName("测试仓库");
        dto.setWarehouseCode("WH001");
        dto.setDeptId(1L);
        
        assertEquals("测试仓库", dto.getWarehouseName());
        assertEquals("WH001", dto.getWarehouseCode());
    }
}
