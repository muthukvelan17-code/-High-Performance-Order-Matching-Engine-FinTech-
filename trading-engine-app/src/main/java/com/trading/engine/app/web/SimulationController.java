package com.trading.engine.app.web;

import com.trading.engine.core.TradingEngine;
import com.trading.engine.core.model.Order;
import com.trading.engine.core.model.OrderSide;
import com.trading.engine.core.model.OrderType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SimulationController {

    private final TradingEngine engine;

    @GetMapping("/api/test/simulate")
    public String simulate() {
        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    engine.submitOrder(Order.builder()
                            .orderId(System.currentTimeMillis() + i)
                            .symbol("BTCUSD")
                            .price(50000 + i)
                            .quantity(100)
                            .side(OrderSide.BUY)
                            .type(OrderType.LIMIT)
                            .userId(101)
                            .remainingQuantity(100)
                            .build());

                    engine.submitOrder(Order.builder()
                            .orderId(System.currentTimeMillis() + i + 100)
                            .symbol("BTCUSD")
                            .price(50000 + i)
                            .quantity(100)
                            .side(OrderSide.SELL)
                            .type(OrderType.LIMIT)
                            .userId(102)
                            .remainingQuantity(100)
                            .build());
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return "Simulation Started";
    }
}
