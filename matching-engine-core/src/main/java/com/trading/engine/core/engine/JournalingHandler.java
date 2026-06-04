package com.trading.engine.core.engine;

import com.lmax.disruptor.EventHandler;
import com.trading.engine.core.event.OrderEvent;
import com.trading.engine.core.persistence.Journaler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JournalingHandler implements EventHandler<OrderEvent> {
    private final Journaler journaler;

    @Override
    public void onEvent(OrderEvent event, long sequence, boolean endOfBatch) {
        if (event.getEventType() == OrderEvent.EventType.NEW_ORDER) {
            com.trading.engine.core.model.Order order = event.getOrder();
            if (order != null) {
                journaler.journalOrder(order);
            }
        } else if (event.getEventType() == OrderEvent.EventType.CANCEL_ORDER) {
            journaler.journalCancel(event.getCancelOrderId());
        }

        // Journal trades
        event.getTrades().forEach(journaler::journalTrade);
    }
}
