package com.ct.wms.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 枚举类测试
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
public class EnumsTest {

    @Test
    public void testApplyStatus() {
        assertEquals(0, com.ct.wms.common.enums.ApplyStatus.PENDING.getValue());
        assertEquals(1, com.ct.wms.common.enums.ApplyStatus.APPROVED.getValue());
        assertEquals(2, com.ct.wms.common.enums.ApplyStatus.REJECTED.getValue());
        assertEquals(3, com.ct.wms.common.enums.ApplyStatus.COMPLETED.getValue());
        assertEquals(4, com.ct.wms.common.enums.ApplyStatus.CANCELED.getValue());

        assertEquals("待审批", com.ct.wms.common.enums.ApplyStatus.PENDING.getDesc());
        assertEquals("已通过", com.ct.wms.common.enums.ApplyStatus.APPROVED.getDesc());
    }

    @Test
    public void testOutboundStatus() {
        assertEquals(0, com.ct.wms.common.enums.OutboundStatus.PENDING_PICKUP.getValue());
        assertEquals(1, com.ct.wms.common.enums.OutboundStatus.COMPLETED.getValue());
        assertEquals(2, com.ct.wms.common.enums.OutboundStatus.CANCELED.getValue());

        assertEquals("待取货", com.ct.wms.common.enums.OutboundStatus.PENDING_PICKUP.getDesc());
        assertEquals("已完成", com.ct.wms.common.enums.OutboundStatus.COMPLETED.getDesc());
    }

    @Test
    public void testOutboundType() {
        assertEquals(1, com.ct.wms.common.enums.OutboundType.RECEIVE.getCode());
        assertEquals(2, com.ct.wms.common.enums.OutboundType.SCRAP.getCode());
        assertEquals(3, com.ct.wms.common.enums.OutboundType.TRANSFER.getCode());
        assertEquals(4, com.ct.wms.common.enums.OutboundType.OTHER.getCode());

        assertEquals("领用出库", com.ct.wms.common.enums.OutboundType.RECEIVE.getDesc());
        assertEquals("报废出库", com.ct.wms.common.enums.OutboundType.SCRAP.getDesc());
    }

    @Test
    public void testInboundType() {
        assertEquals(1, com.ct.wms.common.enums.InboundType.PURCHASE.getCode());
        assertEquals(2, com.ct.wms.common.enums.InboundType.RETURN.getCode());
        assertEquals(3, com.ct.wms.common.enums.InboundType.TRANSFER.getCode());
    }

    @Test
    public void testOutboundSource() {
        assertEquals(1, com.ct.wms.common.enums.OutboundSource.DIRECT.getCode());
        assertEquals(2, com.ct.wms.common.enums.OutboundSource.FROM_APPLY.getCode());

        assertEquals("直接出库", com.ct.wms.common.enums.OutboundSource.DIRECT.getDesc());
        assertEquals("申请出库", com.ct.wms.common.enums.OutboundSource.FROM_APPLY.getDesc());
    }

    @Test
    public void testMessageType() {
        assertNotNull(com.ct.wms.common.enums.MessageType.values());
        assertTrue(com.ct.wms.common.enums.MessageType.values().length > 0);
    }
}
