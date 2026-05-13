package com.trading.engine.core;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.trading.engine.core.engine.*;
import com.trading.engine.core.event.OrderEvent;
import com.trading.engine.core.event.OrderEventFactory;
import com.trading.engine.core.persistence.Journaler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.openhft.affinity.AffinityThreadFactory;
import net.openhft.affinity.AffinityStrategies;
import java.util.concurrent.Executors;

@Slf4j
public class TradingEngine {
    @Getter
    private final Disruptor<OrderEvent> disruptor;
    private final MatchingEngineHandler matchingHandler;
    private final RiskValidationHandler riskHandler;
    private final JournalingHandler journalingHandler;
    private final MarketDataPublisherHandler marketDataHandler;

    public TradingEngine(Journaler journaler, MarketDataListener marketDataListener) {
        int bufferSize = 1024 * 64; // Must be power of 2
        this.disruptor = new Disruptor<>(
                new OrderEventFactory(),
                65536,
                new AffinityThreadFactory("TradingEngine", AffinityStrategies.SAME_SOCKET, true),
                ProducerType.SINGLE,
                new BusySpinWaitStrategy()
        );

        this.matchingHandler = new MatchingEngineHandler();
        this.riskHandler = new RiskValidationHandler();
        this.journalingHandler = new JournalingHandler(journaler);
        this.marketDataHandler = new MarketDataPublisherHandler(marketDataListener);

        // Define pipeline: Risk -> Matching -> [Journaling, MarketData]
        disruptor.handleEventsWith(riskHandler)
                .then(matchingHandler)
                .then(journalingHandler, marketDataHandler);

        disruptor.start();
    }

    public void submitOrder(com.trading.engine.core.model.Order order) {
        log.info("Submitting order: {} {} @ {}", order.side(), order.symbol(), order.price());
        disruptor.getRingBuffer().publishEvent((event, sequence) -> {
            event.clear();
            event.setOrder(order);
            event.setEventType(OrderEvent.EventType.NEW_ORDER);
        });
    }

    public void cancelOrder(long orderId, String symbol) {
        disruptor.getRingBuffer().publishEvent((event, sequence) -> {
            event.clear();
            event.setCancelOrderId(orderId);
            event.setEventType(OrderEvent.EventType.CANCEL_ORDER);
        });
    }

    public com.trading.engine.core.model.OrderBookSnapshot getOrderBookSnapshot(String symbol) {
        return matchingHandler.getSnapshot(symbol);
    }
}
