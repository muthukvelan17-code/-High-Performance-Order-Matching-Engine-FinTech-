package com.trading.engine.benchmark;

import com.trading.engine.core.engine.OrderBook;
import com.trading.engine.core.model.Order;
import com.trading.engine.core.model.OrderSide;
import com.trading.engine.core.model.OrderType;
import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;
import java.util.Random;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(1)
public class MatchingEngineBenchmark {
    private OrderBook orderBook;
    private Random random;
    private long orderId = 0;

    @Setup
    public void setup() {
        orderBook = new OrderBook("BTCUSD");
        random = new Random();
        
        // Pre-fill the book
        for (int i = 0; i < 1000; i++) {
            orderBook.processOrder(createOrder(OrderSide.BUY, 10000 + random.nextInt(100)));
            orderBook.processOrder(createOrder(OrderSide.SELL, 10100 + random.nextInt(100)));
        }
    }

    @Benchmark
    public void testOrderMatching() {
        OrderSide side = random.nextBoolean() ? OrderSide.BUY : OrderSide.SELL;
        long price = side == OrderSide.BUY ? 10100 : 10000;
        orderBook.processOrder(createOrder(side, price));
    }

    private Order createOrder(OrderSide side, long price) {
        return Order.builder()
                .orderId(++orderId)
                .symbol("BTCUSD")
                .price(price)
                .quantity(100)
                .remainingQuantity(100)
                .side(side)
                .type(OrderType.LIMIT)
                .build();
    }
}
