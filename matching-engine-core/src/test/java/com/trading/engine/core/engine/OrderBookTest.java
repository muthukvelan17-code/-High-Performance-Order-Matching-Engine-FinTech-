package com.trading.engine.core.engine;

import com.trading.engine.core.model.Order;
import com.trading.engine.core.model.OrderSide;
import com.trading.engine.core.model.OrderType;
import com.trading.engine.core.model.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OrderBookTest {
    private OrderBook orderBook;

    @BeforeEach
    void setUp() {
        orderBook = new OrderBook("BTCUSD");
    }

    @Test
    void testLimitOrderMatching() {
        // Add a SELL order
        Order sellOrder = Order.builder()
                .orderId(1)
                .symbol("BTCUSD")
                .price(100)
                .quantity(10)
                .remainingQuantity(10)
                .side(OrderSide.SELL)
                .type(OrderType.LIMIT)
                .build();
        
        orderBook.processOrder(sellOrder);

        // Add a BUY order at the same price
        Order buyOrder = Order.builder()
                .orderId(2)
                .symbol("BTCUSD")
                .price(100)
                .quantity(5)
                .remainingQuantity(5)
                .side(OrderSide.BUY)
                .type(OrderType.LIMIT)
                .build();

        List<Trade> trades = orderBook.processOrder(buyOrder);

        assertEquals(1, trades.size());
        assertEquals(5, trades.get(0).quantity());
        assertEquals(100, trades.get(0).price());
    }

    @Test
    void testOrderCancellation() {
        Order buyOrder = Order.builder()
                .orderId(1)
                .symbol("BTCUSD")
                .price(100)
                .quantity(10)
                .remainingQuantity(10)
                .side(OrderSide.BUY)
                .type(OrderType.LIMIT)
                .build();

        orderBook.processOrder(buyOrder);
        orderBook.cancelOrder(1);

        // Try to match against cancelled order
        Order sellOrder = Order.builder()
                .orderId(2)
                .symbol("BTCUSD")
                .price(100)
                .quantity(10)
                .remainingQuantity(10)
                .side(OrderSide.SELL)
                .type(OrderType.LIMIT)
                .build();

        List<Trade> trades = orderBook.processOrder(sellOrder);
        assertTrue(trades.isEmpty());
    }
}
