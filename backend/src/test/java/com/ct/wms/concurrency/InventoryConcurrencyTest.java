package com.ct.wms.concurrency;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ct.wms.common.enums.ApplyStatus;
import com.ct.wms.common.enums.InboundType;
import com.ct.wms.common.enums.OutboundStatus;
import com.ct.wms.dto.ApplyDTO;
import com.ct.wms.dto.ApprovalDTO;
import com.ct.wms.dto.InboundDTO;
import com.ct.wms.entity.Apply;
import com.ct.wms.entity.Inventory;
import com.ct.wms.entity.Outbound;
import com.ct.wms.mapper.ApplyMapper;
import com.ct.wms.mapper.OutboundMapper;
import com.ct.wms.service.ApplyService;
import com.ct.wms.service.InboundService;
import com.ct.wms.service.InventoryService;
import com.ct.wms.service.OutboundService;
import com.ct.wms.util.TestDataBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 并发抢库存测试（W3）：多个线程同时审批/锁定/入库/取货同一物资，证明不会超卖、不会丢更新。
 *
 * <p>刻意<b>不加</b> {@code @Transactional}：测试事务会让所有线程共用一个未提交的快照，测不出真实竞争。
 * 每个用例插入一个专用物资，结束后按物资 ID 清理，不污染其他测试。</p>
 *
 * <p>不超卖靠两层：{@code getInventoryWithLock} 的 {@code SELECT … FOR UPDATE} 让同一行的修改串行化；
 * 更新语句带 {@code AND version = ?}，即使行锁失效也不会覆盖别人的修改（最多失败重试）。</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("并发抢库存")
class InventoryConcurrencyTest {

    private static final long WH = 1L;          // 部门 2 的仓库
    private static final long ADMIN = 1L;
    private static final long EMPLOYEE = 4L;    // 部门 2 的员工
    private static final long KEEPER = 3L;      // 仓库 1 的仓管员（审批只允许仓管员/本部门管理员，ADMIN 也不行）
    private static final int THREADS = 20;

    @Autowired private JdbcTemplate jdbc;
    @Autowired private InventoryService inventoryService;
    @Autowired private ApplyService applyService;
    @Autowired private OutboundService outboundService;
    @Autowired private InboundService inboundService;
    @Autowired private ApplyMapper applyMapper;
    @Autowired private OutboundMapper outboundMapper;

    private long materialId;

    @BeforeEach
    void newMaterial() {
        String code = "CC" + System.nanoTime();
        jdbc.update("INSERT INTO tb_material (material_name, material_code, category, unit, status, deleted) VALUES (?, ?, '测试类别', 'pc', 0, 0)",
                "并发测试物资", code);
        materialId = jdbc.queryForObject("SELECT id FROM tb_material WHERE material_code = ?", Long.class, code);
    }

    @AfterEach
    void cleanUp() throws InterruptedException {
        SecurityContextHolder.clearContext();
        List<Long> applies = jdbc.queryForList(
                "SELECT DISTINCT apply_id FROM tb_apply_detail WHERE material_id = ?", Long.class, materialId);
        List<Long> outbounds = jdbc.queryForList(
                "SELECT DISTINCT outbound_id FROM tb_outbound_detail WHERE material_id = ?", Long.class, materialId);
        List<Long> inbounds = jdbc.queryForList(
                "SELECT DISTINCT inbound_id FROM tb_inbound_detail WHERE material_id = ?", Long.class, materialId);
        Thread.sleep(300);   // 审批通知是 @Async 写入的，等它落库再清
        for (Long id : applies) {
            jdbc.update("DELETE FROM tb_message WHERE related_id = ? AND related_type = 1", id);
            jdbc.update("DELETE FROM tb_apply_detail WHERE apply_id = ?", id);
            jdbc.update("DELETE FROM tb_apply WHERE id = ?", id);
        }
        for (Long id : outbounds) {
            jdbc.update("DELETE FROM tb_message WHERE related_id = ? AND related_type = 2", id);
            jdbc.update("DELETE FROM tb_outbound_detail WHERE outbound_id = ?", id);
            jdbc.update("DELETE FROM tb_outbound WHERE id = ?", id);
        }
        for (Long id : inbounds) {
            jdbc.update("DELETE FROM tb_inbound_detail WHERE inbound_id = ?", id);
            jdbc.update("DELETE FROM tb_inbound WHERE id = ?", id);
        }
        jdbc.update("DELETE FROM tb_inventory_log WHERE material_id = ?", materialId);
        jdbc.update("DELETE FROM tb_inventory WHERE material_id = ?", materialId);
        jdbc.update("DELETE FROM tb_material WHERE id = ?", materialId);
    }

    // ------------------------------------------------------------------ 工具

    /** 所有线程在同一时刻起跑；返回每个任务的结果（成功值或异常）。 */
    private <T> List<Object> race(int n, long userId, String username, String role, java.util.function.IntFunction<Callable<T>> task)
            throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(n);
        CountDownLatch ready = new CountDownLatch(n);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<Object>> futures = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Callable<T> c = task.apply(i);
            futures.add(pool.submit(() -> {
                TestDataBuilder.mockSecurityContext(userId, username, role);
                ready.countDown();
                go.await();
                try {
                    return (Object) c.call();
                } catch (Exception e) {
                    return e;
                } finally {
                    SecurityContextHolder.clearContext();
                }
            }));
        }
        ready.await();
        go.countDown();
        List<Object> results = new ArrayList<>();
        for (Future<Object> f : futures) {
            results.add(f.get(60, TimeUnit.SECONDS));
        }
        pool.shutdown();
        return results;
    }

    private static long count(List<Object> results, Object value) {
        return results.stream().filter(value::equals).count();
    }

    private static List<String> errors(List<Object> results) {
        return results.stream().filter(r -> r instanceof Exception)
                .map(r -> r.getClass().getSimpleName() + ": " + ((Exception) r).getMessage())
                .collect(Collectors.toList());
    }

    private void stock(int qty) {
        TestDataBuilder.mockSecurityContext(ADMIN, "admin", "ADMIN");
        inventoryService.increaseInventory(WH, materialId, new BigDecimal(qty), "INIT", null, ADMIN);
    }

    private Inventory inv() {
        return inventoryService.getInventory(WH, materialId);
    }

    /** 库存三个数必须自洽：可用 = 总量 − 锁定，且都不为负。 */
    private void assertConsistent(Inventory i) {
        assertThat(i.getQuantity()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(i.getLockedQuantity()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(i.getAvailableQuantity()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(i.getAvailableQuantity()).isEqualByComparingTo(i.getQuantity().subtract(i.getLockedQuantity()));
    }

    private Long createApply(int qty) {
        TestDataBuilder.mockSecurityContext(EMPLOYEE, "employee1", "USER");
        ApplyDTO.ApplyDetailDTO d = new ApplyDTO.ApplyDetailDTO();
        d.setMaterialId(materialId);
        d.setQuantity(new BigDecimal(qty));
        ApplyDTO dto = new ApplyDTO();
        dto.setWarehouseId(WH);
        dto.setApplyReason("并发测试");
        dto.setDetails(List.of(d));
        return applyService.createApply(dto);
    }

    private static ApprovalDTO approve(Long applyId) {
        ApprovalDTO a = new ApprovalDTO();
        a.setApplyId(applyId);
        a.setApprovalResult(1);
        return a;
    }

    private Outbound outboundOf(Long applyId) {
        return outboundMapper.selectOne(new LambdaQueryWrapper<Outbound>().eq(Outbound::getApplyId, applyId));
    }

    // ------------------------------------------------------------------ 用例

    @Test
    @DisplayName("20 个线程同时锁定 1 件，库存 10 → 恰好 10 个成功，不超卖")
    void concurrentLockNeverOversells() throws Exception {
        stock(10);
        List<Object> r = race(THREADS, ADMIN, "admin", "ADMIN",
                i -> () -> inventoryService.lockInventory(WH, materialId, BigDecimal.ONE));

        assertThat(errors(r)).isEmpty();
        assertThat(count(r, true)).isEqualTo(10);
        assertThat(count(r, false)).isEqualTo(10);
        Inventory i = inv();
        assertThat(i.getLockedQuantity()).isEqualByComparingTo("10");
        assertThat(i.getAvailableQuantity()).isEqualByComparingTo("0");
        assertConsistent(i);
        Integer lockLogs = jdbc.queryForObject(
                "SELECT COUNT(*) FROM tb_inventory_log WHERE material_id = ? AND change_type = 3", Integer.class, materialId);
        assertThat(lockLogs).isEqualTo(10);
    }

    @Test
    @DisplayName("20 张申请同时审批，库存 10 → 10 张通过，其余因库存不足整单回滚")
    void concurrentApprovalsNeverOversell() throws Exception {
        stock(10);
        List<Long> applies = new ArrayList<>();
        for (int k = 0; k < THREADS; k++) {
            applies.add(createApply(1));
        }
        List<Object> r = race(THREADS, KEEPER, "warehouse", "WAREHOUSE", i -> () -> {
            applyService.approveApply(approve(applies.get(i)));
            return "ok";
        });

        assertThat(count(r, "ok")).isEqualTo(10);
        assertThat(errors(r)).hasSize(10).allMatch(m -> m.contains("库存不足"));
        long approved = applies.stream().map(applyMapper::selectById)
                .filter(a -> a.getStatus() == ApplyStatus.APPROVED).count();
        long pending = applies.stream().map(applyMapper::selectById)
                .filter(a -> a.getStatus() == ApplyStatus.PENDING).count();
        assertThat(approved).isEqualTo(10);
        assertThat(pending).isEqualTo(10);   // 失败的审批整单回滚，申请仍待审批
        long outbounds = applies.stream().map(this::outboundOf).filter(o -> o != null).count();
        assertThat(outbounds).isEqualTo(10);  // 回滚的审批不能留下出库单
        Inventory i = inv();
        assertThat(i.getLockedQuantity()).isEqualByComparingTo("10");
        assertConsistent(i);
    }

    @Test
    @DisplayName("同一张申请被两个审批人同时点通过 → 只通过一次、只锁一次库存")
    void doubleApprovalLocksOnce() throws Exception {
        stock(10);
        Long applyId = createApply(3);
        List<Object> r = race(2, KEEPER, "warehouse", "WAREHOUSE", i -> () -> {
            applyService.approveApply(approve(applyId));
            return "ok";
        });

        assertThat(count(r, "ok")).isEqualTo(1);
        assertThat(errors(r)).hasSize(1);
        assertThat(inv().getLockedQuantity()).isEqualByComparingTo("3");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM tb_outbound WHERE apply_id = ?", Integer.class, applyId))
                .isEqualTo(1);
        assertConsistent(inv());
    }

    @Test
    @DisplayName("同一张出库单同时\"确认取货\"和\"取消\" → 只有一个生效，库存只动一次")
    void confirmAndCancelRace() throws Exception {
        for (int round = 0; round < 5; round++) {   // 多跑几轮，两种先后顺序都覆盖到
            stock(10);
            Long applyId = createApply(4);
            TestDataBuilder.mockSecurityContext(KEEPER, "warehouse", "WAREHOUSE");
            applyService.approveApply(approve(applyId));
            Long outboundId = outboundOf(applyId).getId();
            BigDecimal before = inv().getQuantity();

            List<Object> r = race(2, ADMIN, "admin", "ADMIN", i -> () -> {
                if (i == 0) {
                    outboundService.confirmOutbound(outboundId);
                    return "confirmed";
                }
                outboundService.cancelOutbound(outboundId, "并发测试");
                return "canceled";
            });

            assertThat(errors(r)).as("第 %d 轮", round).hasSize(1);
            Outbound o = outboundMapper.selectById(outboundId);
            Inventory i = inv();
            assertThat(i.getLockedQuantity()).as("锁定量要么扣减要么释放，不能残留").isEqualByComparingTo("0");
            if (r.contains("confirmed")) {
                assertThat(o.getStatus()).isEqualTo(OutboundStatus.COMPLETED);
                assertThat(i.getQuantity()).isEqualByComparingTo(before.subtract(new BigDecimal(4)));
            } else {
                assertThat(o.getStatus()).isEqualTo(OutboundStatus.CANCELED);
                assertThat(i.getQuantity()).isEqualByComparingTo(before);
            }
            assertConsistent(i);
        }
    }

    @Test
    @DisplayName("10 个线程同时入库各 1 件 → 全部成功，一件不丢")
    void concurrentInboundLosesNothing() throws Exception {
        stock(5);
        List<Object> r = race(10, ADMIN, "admin", "ADMIN", i -> () -> {
            InboundDTO dto = new InboundDTO();
            dto.setWarehouseId(WH);
            dto.setInboundType(InboundType.PURCHASE);
            dto.setInboundTime(LocalDateTime.now());
            InboundDTO.InboundDetailDTO d = new InboundDTO.InboundDetailDTO();
            d.setMaterialId(materialId);
            d.setQuantity(BigDecimal.ONE);
            dto.setDetails(List.of(d));
            return inboundService.createInbound(dto);
        });

        assertThat(errors(r)).isEmpty();
        assertThat(inv().getQuantity()).isEqualByComparingTo("15");
        assertConsistent(inv());
    }

    @Test
    @DisplayName("入库与审批锁定交错进行 → 两边都成功，账目自洽")
    void inboundInterleavedWithLocks() throws Exception {
        stock(10);
        List<Object> r = race(THREADS, ADMIN, "admin", "ADMIN", i -> () -> {
            if (i % 2 == 0) {
                inventoryService.increaseInventory(WH, materialId, BigDecimal.ONE, "IN", null, ADMIN);
                return "in";
            }
            return inventoryService.lockInventory(WH, materialId, BigDecimal.ONE);
        });

        assertThat(errors(r)).isEmpty();
        assertThat(count(r, "in")).isEqualTo(10);
        assertThat(count(r, true)).isEqualTo(10);   // 库存始终 ≥ 10，每次锁 1 件都应成功
        Inventory i = inv();
        assertThat(i.getQuantity()).isEqualByComparingTo("20");
        assertThat(i.getLockedQuantity()).isEqualByComparingTo("10");
        assertConsistent(i);
    }

    @Test
    @DisplayName("decreaseInventory 不能扣走已被审批锁定的库存")
    void decreaseRespectsLockedStock() {
        stock(10);
        TestDataBuilder.mockSecurityContext(ADMIN, "admin", "ADMIN");
        inventoryService.lockInventory(WH, materialId, new BigDecimal(8));   // 8 件已承诺给待取货的申请
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                inventoryService.decreaseInventory(WH, materialId, new BigDecimal(5), "D", null, ADMIN))
                .hasMessageContaining("库存不足");
        assertThat(inv().getQuantity()).isEqualByComparingTo("10");
        assertConsistent(inv());
    }
}
