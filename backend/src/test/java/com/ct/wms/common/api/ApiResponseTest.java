package com.ct.wms.common.api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 通用响应类测试
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
public class ApiResponseTest {

    @Test
    public void testResultSuccess() {
        Result<String> result = Result.success("test data", "操作成功");
        
        assertEquals(200, result.getCode());
        assertEquals("test data", result.getData());
        assertEquals("操作成功", result.getMessage());
    }

    @Test
    public void testResultError() {
        Result<Void> result = Result.error(500, "服务器错误");
        
        assertEquals(500, result.getCode());
        assertEquals("服务器错误", result.getMessage());
    }

    @Test
    public void testPageResult() {
        PageResult<String> pageResult = new PageResult<>();
        pageResult.setTotal(100L);
        pageResult.setPageNum(1);
        pageResult.setPageSize(10);
        pageResult.setPages(10);
        
        assertEquals(100L, pageResult.getTotal());
        assertEquals(1, pageResult.getPageNum());
        assertEquals(10, pageResult.getPageSize());
    }

    @Test
    public void testResultWithData() {
        Result<Integer> result = Result.success(42);
        
        assertEquals(200, result.getCode());
        assertEquals(42, result.getData());
    }

    @Test
    public void testResultMessage() {
        Result<Void> result = Result.success("删除成功");
        
        assertEquals("删除成功", result.getMessage());
    }
}
