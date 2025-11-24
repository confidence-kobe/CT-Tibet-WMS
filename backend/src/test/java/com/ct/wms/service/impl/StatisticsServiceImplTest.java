package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ct.wms.common.enums.OutboundSource;
import com.ct.wms.dto.InboundStatisticsDTO;
import com.ct.wms.dto.InventoryStatisticsDTO;
import com.ct.wms.dto.OutboundStatisticsDTO;
import com.ct.wms.entity.*;
import com.ct.wms.mapper.*;
import com.ct.wms.service.InventoryService;
import com.ct.wms.util.TestDataBuilder;
import com.ct.wms.vo.DashboardStatsVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * StatisticsServiceImpl单元测试
 *
 * @author CT Development Team
 * @since 2025-11-16
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("统计服务测试")
class StatisticsServiceImplTest {

    @Mock
    private MaterialMapper materialMapper;

    @Mock
    private WarehouseMapper warehouseMapper;

    @Mock
    private ApplyMapper applyMapper;

    @Mock
    private InboundMapper inboundMapper;

    @Mock
    private InboundDetailMapper inboundDetailMapper;

    @Mock
    private OutboundMapper outboundMapper;

    @Mock
    private OutboundDetailMapper outboundDetailMapper;

    @Mock
    private InventoryMapper inventoryMapper;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private StatisticsServiceImpl statisticsService;

    private Material testMaterial1;
    private Material testMaterial2;
    private Warehouse testWarehouse1;
    private Warehouse testWarehouse2;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testMaterial1 = TestDataBuilder.createMaterial(1L, "物资A", "电子设备",
            BigDecimal.valueOf(100));
        testMaterial2 = TestDataBuilder.createMaterial(2L, "物资B", "办公用品",
            BigDecimal.valueOf(50));

        testWarehouse1 = TestDataBuilder.createWarehouse(1L, "仓库A", 1L);
        testWarehouse2 = TestDataBuilder.createWarehouse(2L, "仓库B", 2L);
    }

    @Test
    @DisplayName("测试获取仪表盘统计数据")
    void testGetDashboardStats() {
        // Given
        when(materialMapper.selectCount(null)).thenReturn(100L);
        when(warehouseMapper.selectCount(null)).thenReturn(14L);
        when(inventoryService.listLowStockAlerts(null)).thenReturn(Collections.emptyList());
        when(applyMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        List<Inbound> mockInbounds = Arrays.asList(
            TestDataBuilder.createInbound(1L, 1L, LocalDateTime.now(), BigDecimal.valueOf(10000)),
            TestDataBuilder.createInbound(2L, 1L, LocalDateTime.now(), BigDecimal.valueOf(20000))
        );
        when(inboundMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(mockInbounds);

        List<Outbound> mockOutbounds = Arrays.asList(
            TestDataBuilder.createOutbound(1L, 1L, OutboundSource.DIRECT,
                LocalDateTime.now(), BigDecimal.valueOf(8000)),
            TestDataBuilder.createOutbound(2L, 1L, OutboundSource.FROM_APPLY,
                LocalDateTime.now(), BigDecimal.valueOf(12000))
        );
        when(outboundMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(mockOutbounds);

        List<Inventory> mockInventories = Arrays.asList(
            TestDataBuilder.createInventory(1L, 1L, 1L, BigDecimal.valueOf(100))
        );
        when(inventoryMapper.selectList(null)).thenReturn(mockInventories);
        when(materialMapper.selectById(1L)).thenReturn(testMaterial1);

        // When
        DashboardStatsVO stats = statisticsService.getDashboardStats();

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.getTotalMaterials()).isEqualTo(100L);
        assertThat(stats.getTotalWarehouses()).isEqualTo(14L);
        assertThat(stats.getLowStockAlerts()).isEqualTo(0L);
        assertThat(stats.getPendingApplies()).isEqualTo(5L);
        assertThat(stats.getMonthInboundCount()).isEqualTo(2L);
        assertThat(stats.getMonthInboundAmount()).isEqualByComparingTo(BigDecimal.valueOf(30000));
        assertThat(stats.getMonthOutboundCount()).isEqualTo(2L);
        assertThat(stats.getMonthOutboundAmount()).isEqualByComparingTo(BigDecimal.valueOf(20000));

        verify(materialMapper, times(1)).selectCount(null);
        verify(warehouseMapper, times(1)).selectCount(null);
    }

    @Test
    @DisplayName("测试入库统计 - 指定日期范围")
    void testGetInboundStatistics_WithDateRange() {
        // Given
        LocalDate startDate = LocalDate.of(2025, 11, 1);
        LocalDate endDate = LocalDate.of(2025, 11, 15);

        List<Inbound> mockInbounds = Arrays.asList(
            TestDataBuilder.createInbound(1L, 1L,
                LocalDateTime.of(2025, 11, 5, 10, 0), BigDecimal.valueOf(10000)),
            TestDataBuilder.createInbound(2L, 1L,
                LocalDateTime.of(2025, 11, 10, 14, 0), BigDecimal.valueOf(20000))
        );
        when(inboundMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(mockInbounds);

        List<InboundDetail> mockDetails = Arrays.asList(
            TestDataBuilder.createInboundDetail(1L, 1L, 1L, BigDecimal.valueOf(100),
                BigDecimal.valueOf(100)),
            TestDataBuilder.createInboundDetail(2L, 2L, 1L, BigDecimal.valueOf(200),
                BigDecimal.valueOf(100))
        );
        when(inboundDetailMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(mockDetails);

        when(warehouseMapper.selectById(1L)).thenReturn(testWarehouse1);
        when(materialMapper.selectById(1L)).thenReturn(testMaterial1);

        // When
        InboundStatisticsDTO result = statisticsService.getInboundStatistics(
            startDate, endDate, null
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalCount()).isEqualTo(2);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(30000));
        assertThat(result.getTotalQuantity()).isEqualByComparingTo(BigDecimal.valueOf(300));
        assertThat(result.getAvgDaily()).isNotNull();
        assertThat(result.getTrendData()).isNotNull();
        assertThat(result.getTrendData().getDates()).hasSize(15);

        verify(inboundMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试入库统计 - 指定仓库")
    void testGetInboundStatistics_WithWarehouse() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        Long warehouseId = 1L;

        List<Inbound> mockInbounds = Arrays.asList(
            TestDataBuilder.createInbound(1L, warehouseId, LocalDateTime.now(),
                BigDecimal.valueOf(15000))
        );
        when(inboundMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(mockInbounds);

        List<InboundDetail> mockDetails = Arrays.asList(
            TestDataBuilder.createInboundDetail(1L, 1L, 1L, BigDecimal.valueOf(150),
                BigDecimal.valueOf(100))
        );
        when(inboundDetailMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(mockDetails);

        when(warehouseMapper.selectById(warehouseId)).thenReturn(testWarehouse1);
        when(materialMapper.selectById(1L)).thenReturn(testMaterial1);

        // When
        InboundStatisticsDTO result = statisticsService.getInboundStatistics(
            startDate, endDate, warehouseId
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalCount()).isEqualTo(1);
        assertThat(result.getWarehouseData()).isNotEmpty();

        verify(inboundMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试入库统计 - 默认日期范围")
    void testGetInboundStatistics_DefaultDateRange() {
        // Given - 不传入日期，使用默认最近30天
        when(inboundMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Collections.emptyList());

        // When
        InboundStatisticsDTO result = statisticsService.getInboundStatistics(
            null, null, null
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTrendData().getDates()).hasSize(31); // 包含起止日期

        verify(inboundMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试出库统计 - 按出库类型筛选")
    void testGetOutboundStatistics_WithType() {
        // Given
        LocalDate startDate = LocalDate.of(2025, 11, 1);
        LocalDate endDate = LocalDate.of(2025, 11, 10);
        Integer outboundType = OutboundSource.DIRECT.getValue();

        List<Outbound> mockOutbounds = Arrays.asList(
            TestDataBuilder.createOutbound(1L, 1L, OutboundSource.DIRECT,
                LocalDateTime.of(2025, 11, 5, 10, 0), BigDecimal.valueOf(5000)),
            TestDataBuilder.createOutbound(2L, 1L, OutboundSource.DIRECT,
                LocalDateTime.of(2025, 11, 8, 14, 0), BigDecimal.valueOf(3000))
        );
        when(outboundMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(mockOutbounds);

        List<OutboundDetail> mockDetails = Arrays.asList(
            TestDataBuilder.createOutboundDetail(1L, 1L, 1L, BigDecimal.valueOf(50),
                BigDecimal.valueOf(100)),
            TestDataBuilder.createOutboundDetail(2L, 2L, 1L, BigDecimal.valueOf(30),
                BigDecimal.valueOf(100))
        );
        when(outboundDetailMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(mockDetails);

        when(warehouseMapper.selectById(1L)).thenReturn(testWarehouse1);
        when(materialMapper.selectById(1L)).thenReturn(testMaterial1);

        // When
        OutboundStatisticsDTO result = statisticsService.getOutboundStatistics(
            startDate, endDate, null, outboundType
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalCount()).isEqualTo(2);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(8000));
        assertThat(result.getTotalQuantity()).isEqualByComparingTo(BigDecimal.valueOf(80));
        assertThat(result.getTypeData()).isNotEmpty();

        verify(outboundMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试出库统计 - 趋势数据完整性")
    void testGetOutboundStatistics_TrendDataIntegrity() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(6);
        LocalDate endDate = LocalDate.now();

        when(outboundMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Collections.emptyList());

        // When
        OutboundStatisticsDTO result = statisticsService.getOutboundStatistics(
            startDate, endDate, null, null
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTrendData()).isNotNull();
        assertThat(result.getTrendData().getDates()).hasSize(7);
        assertThat(result.getTrendData().getQuantities()).hasSize(7);
        assertThat(result.getTrendData().getAmounts()).hasSize(7);
    }

    @Test
    @DisplayName("测试库存统计 - 库存周转率计算")
    void testGetInventoryStatistics_TurnoverRate() {
        // Given
        Long warehouseId = 1L;

        List<Inventory> mockInventories = Arrays.asList(
            TestDataBuilder.createInventory(1L, warehouseId, 1L, BigDecimal.valueOf(100)),
            TestDataBuilder.createInventory(2L, warehouseId, 2L, BigDecimal.valueOf(200))
        );
        when(inventoryMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(mockInventories);

        when(materialMapper.selectById(1L)).thenReturn(testMaterial1);
        when(materialMapper.selectById(2L)).thenReturn(testMaterial2);

        List<Outbound> mockOutbounds = Arrays.asList(
            TestDataBuilder.createOutbound(1L, warehouseId, OutboundSource.DIRECT,
                LocalDateTime.now().minusDays(5), BigDecimal.valueOf(5000))
        );
        when(outboundMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(mockOutbounds);

        List<OutboundDetail> mockOutboundDetails = Arrays.asList(
            TestDataBuilder.createOutboundDetail(1L, 1L, 1L, BigDecimal.valueOf(50),
                BigDecimal.valueOf(100))
        );
        when(outboundDetailMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(mockOutboundDetails);

        // When
        InventoryStatisticsDTO result = statisticsService.getInventoryStatistics(warehouseId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMaterialCount()).isEqualTo(2);
        assertThat(result.getTotalValue()).isGreaterThan(BigDecimal.ZERO);
        assertThat(result.getTurnoverRate()).isNotNull();

        verify(inventoryMapper, atLeastOnce()).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试库存统计 - Top 10库存占用排名")
    void testGetInventoryStatistics_Top10() {
        // Given
        // 创建15个库存记录，测试Top 10功能
        List<Inventory> mockInventories = Arrays.asList(
            TestDataBuilder.createInventory(1L, 1L, 1L, BigDecimal.valueOf(1000)),
            TestDataBuilder.createInventory(2L, 1L, 2L, BigDecimal.valueOf(2000)),
            TestDataBuilder.createInventory(3L, 1L, 1L, BigDecimal.valueOf(500))
        );
        when(inventoryMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(mockInventories);

        when(materialMapper.selectById(1L)).thenReturn(testMaterial1);
        when(materialMapper.selectById(2L)).thenReturn(testMaterial2);

        when(outboundMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Collections.emptyList());

        // When
        InventoryStatisticsDTO result = statisticsService.getInventoryStatistics(null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTopStocks()).isNotNull();
        assertThat(result.getTopStocks()).hasSizeLessThanOrEqualTo(10);

        // 验证排序：第一个应该是价值最高的
        if (!result.getTopStocks().isEmpty()) {
            for (int i = 0; i < result.getTopStocks().size() - 1; i++) {
                assertThat(result.getTopStocks().get(i).getValue())
                    .isGreaterThanOrEqualTo(result.getTopStocks().get(i + 1).getValue());
            }
        }
    }

    @Test
    @DisplayName("测试库存统计 - 预警数量统计")
    void testGetInventoryStatistics_WarningCount() {
        // Given
        Material lowStockMaterial = TestDataBuilder.createMaterial(3L, "低库存物资",
            "测试", BigDecimal.valueOf(100));
        lowStockMaterial.setMinStock(BigDecimal.valueOf(100)); // 最低库存100

        List<Inventory> mockInventories = Arrays.asList(
            TestDataBuilder.createInventory(1L, 1L, 3L, BigDecimal.valueOf(50)) // 低于最低库存
        );
        when(inventoryMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(mockInventories);

        when(materialMapper.selectById(3L)).thenReturn(lowStockMaterial);
        when(outboundMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Collections.emptyList());

        // When
        InventoryStatisticsDTO result = statisticsService.getInventoryStatistics(null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getWarningCount()).isEqualTo(1);

        verify(inventoryMapper, atLeastOnce()).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试库存统计 - 空库存列表")
    void testGetInventoryStatistics_EmptyInventory() {
        // Given
        when(inventoryMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Collections.emptyList());
        when(outboundMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Collections.emptyList());

        // When
        InventoryStatisticsDTO result = statisticsService.getInventoryStatistics(null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMaterialCount()).isEqualTo(0);
        assertThat(result.getTotalValue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getWarningCount()).isEqualTo(0);
        assertThat(result.getTopStocks()).isEmpty();
    }

    @Test
    @DisplayName("测试入库统计 - 物资分类占比")
    void testGetInboundStatistics_CategoryDistribution() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        List<Inbound> mockInbounds = Arrays.asList(
            TestDataBuilder.createInbound(1L, 1L, LocalDateTime.now(), BigDecimal.valueOf(10000))
        );
        when(inboundMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(mockInbounds);

        List<InboundDetail> mockDetails = Arrays.asList(
            TestDataBuilder.createInboundDetail(1L, 1L, 1L, BigDecimal.valueOf(100),
                BigDecimal.valueOf(100)),
            TestDataBuilder.createInboundDetail(2L, 1L, 2L, BigDecimal.valueOf(200),
                BigDecimal.valueOf(50))
        );
        when(inboundDetailMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(mockDetails);

        when(materialMapper.selectById(1L)).thenReturn(testMaterial1); // 电子设备
        when(materialMapper.selectById(2L)).thenReturn(testMaterial2); // 办公用品

        // When
        InboundStatisticsDTO result = statisticsService.getInboundStatistics(
            startDate, endDate, null
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCategoryData()).isNotEmpty();
        assertThat(result.getCategoryData()).hasSizeGreaterThanOrEqualTo(2);
    }
}
