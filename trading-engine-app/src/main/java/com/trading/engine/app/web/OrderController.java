package com.trading.engine.app.web;

import com.trading.engine.core.TradingEngine;
import com.trading.engine.core.model.Order;
import com.trading.engine.core.model.OrderSide;
import com.trading.engine.core.model.OrderType;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final TradingEngine engine;

    @PostMapping
    public ResponseEntity<String> submitOrder(@RequestBody OrderRequest request) {
        Order order = Order.builder()
                .orderId(System.nanoTime()) // Generate unique ID or take from request
                .symbol(request.getSymbol())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .side(request.getSide())
                .type(request.getType())
                .userId(request.getUserId())
                .remainingQuantity(request.getQuantity())
                .build();

        engine.submitOrder(order);
        return ResponseEntity.ok("Order submitted successfully with ID: " + order.orderId());
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> cancelOrder(@PathVariable("orderId") long orderId, @RequestParam("symbol") String symbol) {
        engine.cancelOrder(orderId, symbol);
        return ResponseEntity.ok("Cancel request submitted for Order ID: " + orderId);
    }

    @Data
    public static class OrderRequest {
        private String symbol;
        private long price;
        private long quantity;
        private OrderSide side;
        private OrderType type;
        private long userId;
    }
}
