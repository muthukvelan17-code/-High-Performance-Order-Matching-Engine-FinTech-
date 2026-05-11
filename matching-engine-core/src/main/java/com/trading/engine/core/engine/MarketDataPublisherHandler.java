package com.trading.engine.core.engine;

import com.lmax.disruptor.EventHandler;
import com.trading.engine.core.event.OrderEvent;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MarketDataPublisherHandler implements EventHandler<OrderEvent> {
    private final MarketDataListener listener;

    @Override
    public void onEvent(OrderEvent event, long sequence, boolean endOfBatch) {
        if (!event.getTrades().isEmpty()) {
            listener.onTrade(event.getTrades());
        }
        
        // Notify order book update for every event that modifies the book
        String symbol = event.getOrder() != null ? event.getOrder().symbol() : null;
        if (symbol != null) {
            listener.onOrderBookUpdate(symbol);
        }
    }
}
