package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ct.wms.common.enums.UserStatus;
import com.ct.wms.common.exception.BusinessException;
import com.ct.wms.dto.ChangePasswordRequest;
import com.ct.wms.dto.UpdateProfileRequest;
import com.ct.wms.dto.UserDTO;
import com.ct.wms.entity.Dept;
import com.ct.wms.entity.Role;
import com.ct.wms.entity.User;
import com.ct.wms.mapper.DeptMapper;
import com.ct.wms.mapper.RoleMapper;
import com.ct.wms.mapper.UserMapper;
import com.ct.wms.util.TestDataBuilder;
import com.ct.wms.vo.UserProfileVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * UserServiceImpl单元测试
 *
 * @author CT Development Team
 * @since 2025-11-16
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务测试")
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private DeptMapper deptMapper;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private static final Long TEST_USER_ID = 100L;
    private static final String TEST_USERNAME = "testuser";
    private static final Long TEST_DEPT_ID = 1L;
    private static final Long TEST_ROLE_ID = 2L;

    private Dept testDept;
    private Role testRole;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testDept = TestDataBuilder.createDept(TEST_DEPT_ID, "测试部门");
        testRole = TestDataBuilder.createRole(TEST_ROLE_ID, "普通用户", "USER");
        testUser = TestDataBuilder.createUser(TEST_USER_ID, TEST_USERNAME, TEST_DEPT_ID, TEST_ROLE_ID);

        // Mock SecurityContext
        TestDataBuilder.mockSecurityContext(TEST_USER_ID, TEST_USERNAME, "USER");
    }

    @AfterEach
    void tearDown() {
        TestDataBuilder.clearSecurityContext();
    }

    @Test
    @DisplayName("测试查询用户列表 - 无筛选条件")
    void testListUsers_NoFilter() {
        // Given
        Integer pageNum = 1;
        Integer pageSize = 10;

        List<User> mockUsers = Arrays.asList(
            TestDataBuilder.createUser(1L, "user1", TEST_DEPT_ID, TEST_ROLE_ID),
            TestDataBuilder.createUser(2L, "user2", TEST_DEPT_ID, TEST_ROLE_ID)
        );

        Page<User> mockPage = new Page<>(pageNum, pageSize);
        mockPage.setRecords(mockUsers);
        mockPage.setTotal(2L);

        when(userMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
            .thenReturn(mockPage);
        when(deptMapper.selectById(TEST_DEPT_ID)).thenReturn(testDept);
        when(roleMapper.selectById(TEST_ROLE_ID)).thenReturn(testRole);

        // When
        Page<User> result = userService.listUsers(pageNum, pageSize, null, null, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRecords()).hasSize(2);
        assertThat(result.getTotal()).isEqualTo(2L);

        verify(userMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试查询用户列表 - 按部门筛选")
    void testListUsers_FilterByDept() {
        // Given
        Integer pageNum = 1;
        Integer pageSize = 10;
        Long deptId = TEST_DEPT_ID;

        List<User> mockUsers = Arrays.asList(
            TestDataBuilder.createUser(1L, "user1", deptId, TEST_ROLE_ID)
        );

        Page<User> mockPage = new Page<>(pageNum, pageSize);
        mockPage.setRecords(mockUsers);
        mockPage.setTotal(1L);

        when(userMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
            .thenReturn(mockPage);
        when(deptMapper.selectById(deptId)).thenReturn(testDept);
        when(roleMapper.selectById(TEST_ROLE_ID)).thenReturn(testRole);

        // When
        Page<User> result = userService.listUsers(pageNum, pageSize, deptId, null, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getDeptId()).isEqualTo(deptId);

        verify(userMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试查询用户列表 - 关键字搜索")
    void testListUsers_SearchByKeyword() {
        // Given
        Integer pageNum = 1;
        Integer pageSize = 10;
        String keyword = "test";

        List<User> mockUsers = Arrays.asList(
            TestDataBuilder.createUser(1L, "testuser", TEST_DEPT_ID, TEST_ROLE_ID)
        );

        Page<User> mockPage = new Page<>(pageNum, pageSize);
        mockPage.setRecords(mockUsers);
        mockPage.setTotal(1L);

        when(userMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
            .thenReturn(mockPage);
        when(deptMapper.selectById(TEST_DEPT_ID)).thenReturn(testDept);
        when(roleMapper.selectById(TEST_ROLE_ID)).thenReturn(testRole);

        // When
        Page<User> result = userService.listUsers(pageNum, pageSize, null, null, null, keyword);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRecords()).hasSize(1);

        verify(userMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试获取用户详情 - 成功")
    void testGetUserById_Success() {
        // Given
        Long userId = 1L;
        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(deptMapper.selectById(TEST_DEPT_ID)).thenReturn(testDept);
        when(roleMapper.selectById(TEST_ROLE_ID)).thenReturn(testRole);

        // When
        User result = userService.getUserById(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_USER_ID);
        assertThat(result.getDeptName()).isEqualTo(testDept.getDeptName());
        assertThat(result.getRoleName()).isEqualTo(testRole.getRoleName());

        verify(userMapper, times(1)).selectById(userId);
    }

    @Test
    @DisplayName("测试获取用户详情 - 用户不存在")
    void testGetUserById_NotFound() {
        // Given
        Long userId = 999L;
        when(userMapper.selectById(userId)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(userId))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("用户不存在");

        verify(userMapper, times(1)).selectById(userId);
    }

    @Test
    @DisplayName("测试创建用户 - 成功")
    void testCreateUser_Success() {
        // Given
        UserDTO dto = new UserDTO();
        dto.setUsername("newuser");
        dto.setPassword("password123");
        dto.setRealName("新用户");
        dto.setDeptId(TEST_DEPT_ID);
        dto.setRoleId(TEST_ROLE_ID);
        dto.setPhone("13800138000");
        dto.setEmail("newuser@example.com");

        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(deptMapper.selectById(TEST_DEPT_ID)).thenReturn(testDept);
        when(roleMapper.selectById(TEST_ROLE_ID)).thenReturn(testRole);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encoded.password");
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(999L); // Simulate auto-generated ID
            return 1;
        });

        // When
        Long userId = userService.createUser(dto);

        // Then
        assertThat(userId).isNotNull();

        verify(userMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
        verify(userMapper, times(1)).insert(any(User.class));
        verify(passwordEncoder, times(1)).encode("password123");
    }

    @Test
    @DisplayName("测试创建用户 - 用户名已存在")
    void testCreateUser_UsernameExists() {
        // Given
        UserDTO dto = new UserDTO();
        dto.setUsername("existuser");
        dto.setPassword("password123");
        dto.setDeptId(TEST_DEPT_ID);
        dto.setRoleId(TEST_ROLE_ID);

        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(dto))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("用户名已存在");

        verify(userMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("测试创建用户 - 部门不存在")
    void testCreateUser_DeptNotFound() {
        // Given
        UserDTO dto = new UserDTO();
        dto.setUsername("newuser");
        dto.setPassword("password123");
        dto.setDeptId(999L);
        dto.setRoleId(TEST_ROLE_ID);

        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(deptMapper.selectById(999L)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(dto))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("部门不存在");

        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("测试更新用户 - 成功")
    void testUpdateUser_Success() {
        // Given
        Long userId = TEST_USER_ID;
        UserDTO dto = new UserDTO();
        dto.setUsername(TEST_USERNAME);
        dto.setRealName("更新后的姓名");
        dto.setDeptId(TEST_DEPT_ID);
        dto.setRoleId(TEST_ROLE_ID);
        dto.setPhone("13900139000");
        dto.setEmail("updated@example.com");

        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(deptMapper.selectById(TEST_DEPT_ID)).thenReturn(testDept);
        when(roleMapper.selectById(TEST_ROLE_ID)).thenReturn(testRole);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        userService.updateUser(userId, dto);

        // Then
        verify(userMapper, times(1)).selectById(userId);
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    @DisplayName("测试删除用户 - 成功")
    void testDeleteUser_Success() {
        // Given
        Long userId = TEST_USER_ID;
        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(userMapper.deleteById(userId)).thenReturn(1);

        // When
        userService.deleteUser(userId);

        // Then
        verify(userMapper, times(1)).selectById(userId);
        verify(userMapper, times(1)).deleteById(userId);
    }

    @Test
    @DisplayName("测试更新用户状态 - 成功")
    void testUpdateUserStatus_Success() {
        // Given
        Long userId = TEST_USER_ID;
        Integer status = UserStatus.DISABLED.getCode();

        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        userService.updateUserStatus(userId, status);

        // Then
        verify(userMapper, times(1)).selectById(userId);
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    @DisplayName("测试重置密码 - 成功")
    void testResetPassword_Success() {
        // Given
        Long userId = TEST_USER_ID;
        String newPassword = "newpassword123";

        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(passwordEncoder.encode(newPassword)).thenReturn("$2a$10$new.encoded.password");
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        userService.resetPassword(userId, newPassword);

        // Then
        verify(userMapper, times(1)).selectById(userId);
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    @DisplayName("测试检查用户名是否存在")
    void testCheckUsernameExists() {
        // Given
        String username = "testuser";
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // When
        boolean exists = userService.checkUsernameExists(username, null);

        // Then
        assertThat(exists).isTrue();

        verify(userMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试获取当前用户信息")
    void testGetCurrentUserProfile() {
        // Given
        when(userMapper.selectById(TEST_USER_ID)).thenReturn(testUser);
        when(deptMapper.selectById(TEST_DEPT_ID)).thenReturn(testDept);
        when(roleMapper.selectById(TEST_ROLE_ID)).thenReturn(testRole);

        // When
        UserProfileVO profile = userService.getCurrentUserProfile();

        // Then
        assertThat(profile).isNotNull();
        assertThat(profile.getId()).isEqualTo(TEST_USER_ID);
        assertThat(profile.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(profile.getDeptName()).isEqualTo(testDept.getDeptName());
        assertThat(profile.getRoleName()).isEqualTo(testRole.getRoleName());

        verify(userMapper, times(1)).selectById(TEST_USER_ID);
    }

    @Test
    @DisplayName("测试更新个人信息 - 成功")
    void testUpdateCurrentUserProfile_Success() {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setRealName("新姓名");
        request.setPhone("13900139000");
        request.setEmail("newemail@example.com");

        when(userMapper.selectById(TEST_USER_ID)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        userService.updateCurrentUserProfile(request);

        // Then
        verify(userMapper, times(1)).selectById(TEST_USER_ID);
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    @DisplayName("测试修改当前用户密码 - 成功")
    void testChangeCurrentUserPassword_Success() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldpassword");
        request.setNewPassword("newpassword123");

        when(userMapper.selectById(TEST_USER_ID)).thenReturn(testUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode("newpassword123")).thenReturn("$2a$10$new.encoded");
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        userService.changeCurrentUserPassword(request);

        // Then
        verify(userMapper, times(1)).selectById(TEST_USER_ID);
        verify(passwordEncoder, times(1)).matches(eq("oldpassword"), anyString());
        verify(passwordEncoder, times(1)).encode("newpassword123");
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    @DisplayName("测试修改密码 - 旧密码错误")
    void testChangeCurrentUserPassword_WrongOldPassword() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrongpassword");
        request.setNewPassword("newpassword123");

        when(userMapper.selectById(TEST_USER_ID)).thenReturn(testUser);
        when(passwordEncoder.matches("wrongpassword", testUser.getPassword())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.changeCurrentUserPassword(request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("旧密码不正确");

        verify(userMapper, times(1)).selectById(TEST_USER_ID);
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    @DisplayName("测试修改密码 - 新旧密码相同")
    void testChangeCurrentUserPassword_SamePassword() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("samepassword");
        request.setNewPassword("samepassword");

        when(userMapper.selectById(TEST_USER_ID)).thenReturn(testUser);
        when(passwordEncoder.matches("samepassword", testUser.getPassword())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.changeCurrentUserPassword(request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("新密码不能与旧密码相同");

        verify(userMapper, times(1)).selectById(TEST_USER_ID);
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    @DisplayName("测试更新个人信息 - 用户不存在")
    void testUpdateCurrentUserProfile_UserNotFound() {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setRealName("新姓名");

        when(userMapper.selectById(TEST_USER_ID)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> userService.updateCurrentUserProfile(request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("用户不存在");

        verify(userMapper, times(1)).selectById(TEST_USER_ID);
        verify(userMapper, never()).updateById(any(User.class));
    }
}
