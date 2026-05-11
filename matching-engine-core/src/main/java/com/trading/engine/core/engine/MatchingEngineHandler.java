package com.trading.engine.core.engine;

import com.lmax.disruptor.EventHandler;
import com.trading.engine.core.event.OrderEvent;
import com.trading.engine.core.model.Trade;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchingEngineHandler implements EventHandler<OrderEvent> {
    private final Map<String, OrderBook> orderBooks = new HashMap<>();

    @Override
    public void onEvent(OrderEvent event, long sequence, boolean endOfBatch) {
        String symbol = event.getOrder() != null ? event.getOrder().symbol() : null;
        if (symbol == null) return;

        OrderBook book = orderBooks.computeIfAbsent(symbol, OrderBook::new);

        switch (event.getEventType()) {
            case NEW_ORDER -> {
                System.out.println("[MatchingEngine] New Order: " + event.getOrder().side() + " " + event.getOrder().symbol() + " @ " + event.getOrder().price() + " qty: " + event.getOrder().quantity());
                List<Trade> trades = book.processOrder(event.getOrder());
                for (Trade trade : trades) {
                    System.out.println("[MATCHED] Trade: " + trade.quantity() + " " + trade.symbol() + " at " + trade.price() + " (Maker: " + trade.makerOrderId() + ", Taker: " + trade.takerOrderId() + ")");
                }
                event.getTrades().addAll(trades);
            }
            case CANCEL_ORDER -> {
                System.out.println("[MatchingEngine] Cancel Order: " + event.getCancelOrderId());
                book.cancelOrder(event.getCancelOrderId());
            }
            case MODIFY_ORDER -> {
                System.out.println("[MatchingEngine] Modify Order: " + event.getOrder().orderId());
                book.cancelOrder(event.getOrder().orderId());
                List<Trade> trades = book.processOrder(event.getOrder());
                event.getTrades().addAll(trades);
            }
        }
    }
}
