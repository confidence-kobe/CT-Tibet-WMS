package com.ct.wms.service.impl;

import com.ct.wms.entity.Inventory;
import com.ct.wms.mapper.InventoryMapper;
import com.ct.wms.service.InventoryService;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 库存查询：物资信息填充与库存状态筛选
 *
 * @author CT Development Team
 * @since 2026-11-17
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("库存查询测试")
class InventoryQueryTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryMapper inventoryMapper;

    @BeforeEach
    void setUp() {
        TestDataBuilder.mockSecurityContext(1L, "admin", "ADMIN");
        // 造一条缺货、一条低库存（物资1最低库存为100）
        Inventory out = inventoryMapper.selectById(2L);
        out.setQuantity(BigDecimal.ZERO);
        out.setAvailableQuantity(BigDecimal.ZERO);
        inventoryMapper.updateById(out);
        Inventory low = inventoryMapper.selectById(1L);
        low.setQuantity(new BigDecimal("10"));
        low.setAvailableQuantity(new BigDecimal("10"));
        inventoryMapper.updateById(low);
    }

    @AfterEach
    void tearDown() {
        TestDataBuilder.clearSecurityContext();
    }

    private List<Inventory> list(Integer status) {
        return inventoryService.listInventories(1, 100, null, null, null, status).getRecords();
    }

    @Test
    @DisplayName("列表返回类别、单价、最低库存和库存金额")
    void listFillsMaterialInfo() {
        Inventory row = list(null).stream().filter(i -> i.getId().equals(1L)).findFirst().orElseThrow();
        assertThat(row.getCategory()).isNotBlank();
        assertThat(row.getPrice()).isNotNull();
        assertThat(row.getMinStock()).isNotNull();
        assertThat(row.getStockValue()).isEqualByComparingTo(row.getPrice().multiply(row.getQuantity()));
    }

    @Test
    @DisplayName("库存状态筛选：缺货 / 低库存 / 正常 各自符合规则，且合起来等于全部")
    void stockStatusFilter() {
        List<Inventory> normal = list(0);
        List<Inventory> low = list(1);
        List<Inventory> out = list(2);

        assertThat(out).isNotEmpty().allSatisfy(i -> assertThat(i.getQuantity()).isLessThanOrEqualTo(BigDecimal.ZERO));
        assertThat(low).isNotEmpty().allSatisfy(i -> {
            assertThat(i.getQuantity()).isPositive();
            assertThat(i.getQuantity()).isLessThan(i.getMinStock());
        });
        assertThat(normal).isNotEmpty().allSatisfy(i -> {
            assertThat(i.getQuantity()).isPositive();
            if (i.getMinStock() != null) {
                assertThat(i.getQuantity()).isGreaterThanOrEqualTo(i.getMinStock());
            }
        });
        assertThat(normal.size() + low.size() + out.size()).isEqualTo(list(null).size());
    }
}
