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
    private Order order;
    private long cancelOrderId;
    private EventType eventType;
    private final List<Trade> trades = new ArrayList<>();

    public enum EventType {
        NEW_ORDER, CANCEL_ORDER, MODIFY_ORDER
    }

    public void clear() {
        order = null;
        cancelOrderId = 0;
        trades.clear();
    }
}
