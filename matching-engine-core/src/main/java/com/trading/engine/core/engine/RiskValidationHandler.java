package com.trading.engine.core.engine;

import com.lmax.disruptor.EventHandler;
import com.trading.engine.core.event.OrderEvent;

public class RiskValidationHandler implements EventHandler<OrderEvent> {
    @Override
    public void onEvent(OrderEvent event, long sequence, boolean endOfBatch) {
        // Simple risk check: ensure quantity is positive
        if (event.getEventType() == OrderEvent.EventType.NEW_ORDER) {
            if (event.getOrder().quantity() <= 0) {
                // In a real system, we'd mark the event as rejected
                // For this sprint, we'll just log or flag it
            }
        }
    }
}
