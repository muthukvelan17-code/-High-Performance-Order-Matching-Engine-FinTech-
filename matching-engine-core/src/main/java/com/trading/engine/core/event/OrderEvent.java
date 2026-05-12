package com.trading.engine.core.event;

import com.trading.engine.core.model.Order;
import com.trading.engine.core.model.Trade;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderEvent {
    // Padding to prevent false sharing (64 bytes is typical cache line size)
    protected long p1, p2, p3, p4, p5, p6, p7;
    private Order order;
    private long cancelOrderId;
    private EventType eventType;
    private final List<Trade> trades = new ArrayList<>();
    private com.trading.engine.core.model.OrderBookSnapshot snapshot;
    protected long p8, p9, p10, p11, p12, p13, p14;

    public enum EventType {
        NEW_ORDER, CANCEL_ORDER, MODIFY_ORDER
    }

    public void clear() {
        order = null;
        cancelOrderId = 0;
        trades.clear();
        snapshot = null;
    }
}
