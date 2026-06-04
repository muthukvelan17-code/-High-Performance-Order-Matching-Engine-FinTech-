package com.trading.engine.core.engine;

import com.lmax.disruptor.EventHandler;
import com.trading.engine.core.event.OrderEvent;
import com.trading.engine.core.model.Trade;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MatchingEngineHandler implements EventHandler<OrderEvent> {
    private final Map<String, OrderBook> orderBooks = new HashMap<>();

    @Override
    public void onEvent(OrderEvent event, long sequence, boolean endOfBatch) {
        com.trading.engine.core.model.Order order = event.getOrder();
        if (order == null) return;
        String symbol = order.symbol();
        if (symbol == null) return;

        OrderBook book = orderBooks.computeIfAbsent(symbol, OrderBook::new);

        switch (event.getEventType()) {
            case NEW_ORDER -> {
                log.info("[MatchingEngine] New Order: {} {} @ {} qty: {}", order.side(), order.symbol(), order.price(), order.quantity());
                List<Trade> trades = book.processOrder(order);
                for (Trade trade : trades) {
                    log.info("[MATCHED] Trade: {} {} at {} (Maker: {}, Taker: {})", trade.quantity(), trade.symbol(), trade.price(), trade.makerOrderId(), trade.takerOrderId());
                }
                event.getTrades().addAll(trades);
            }
            case CANCEL_ORDER -> {
                book.cancelOrder(event.getCancelOrderId());
            }
            case MODIFY_ORDER -> {
                book.cancelOrder(order.orderId());
                List<Trade> trades = book.processOrder(order);
                event.getTrades().addAll(trades);
            }
        }
        
        event.setSnapshot(book.getSnapshot(10));
    }

    public com.trading.engine.core.model.OrderBookSnapshot getSnapshot(String symbol) {
        OrderBook book = orderBooks.get(symbol);
        return book != null ? book.getSnapshot(10) : null;
    }
}
