package com.trading.engine.app.db;

import com.trading.engine.core.model.Order;
import com.trading.engine.core.model.Trade;
import com.trading.engine.core.persistence.Journaler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseJournaler implements Journaler {
    private final OrderHistoryRepository orderRepository;
    private final TradeHistoryRepository tradeRepository;

    @Override
    public void journalOrder(Order order) {
        try {
            OrderHistoryEntity entity = OrderHistoryEntity.builder()
                    .orderId(order.orderId())
                    .symbol(order.symbol())
                    .price(order.price())
                    .quantity(order.quantity())
                    .remainingQuantity(order.remainingQuantity())
                    .side(order.side() != null ? order.side().name() : null)
                    .type(order.type() != null ? order.type().name() : null)
                    .status(order.status() != null ? order.status().name() : null)
                    .timestamp(order.timestamp())
                    .userId(order.userId())
                    .build();
            orderRepository.save(entity);
        } catch (Exception e) {
            log.error("Failed to journal order to database", e);
        }
    }

    @Override
    public void journalTrade(Trade trade) {
        try {
            TradeHistoryEntity entity = TradeHistoryEntity.builder()
                    .tradeId(trade.tradeId())
                    .makerOrderId(trade.makerOrderId())
                    .takerOrderId(trade.takerOrderId())
                    .symbol(trade.symbol())
                    .price(trade.price())
                    .quantity(trade.quantity())
                    .timestamp(trade.timestamp())
                    .build();
            tradeRepository.save(entity);
        } catch (Exception e) {
            log.error("Failed to journal trade to database", e);
        }
    }

    @Override
    public void journalCancel(long orderId) {
        try {
            Optional<OrderHistoryEntity> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isPresent()) {
                OrderHistoryEntity order = orderOpt.get();
                order.setStatus("CANCELLED");
                orderRepository.save(order);
            }
        } catch (Exception e) {
            log.error("Failed to journal cancel to database", e);
        }
    }
}
