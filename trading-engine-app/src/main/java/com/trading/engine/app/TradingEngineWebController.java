package com.trading.engine.app;

import com.trading.engine.core.TradingEngine;
import com.trading.engine.marketdata.MarketDataService;
import com.trading.engine.core.model.Order;
import com.trading.engine.core.model.OrderBookSnapshot;
import com.trading.engine.core.model.OrderSide;
import com.trading.engine.core.model.OrderType;
import com.trading.engine.core.model.Trade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TradingEngineWebController {

    private final TradingEngine tradingEngine;
    private final MarketDataService marketDataService;
    private final AtomicLong orderIdCounter = new AtomicLong(2000000);

    @Value("${trading.engine.symbol-config:BTCUSD,ETHUSD,SOLUSD}")
    private String symbolConfig;

    @Autowired
    public TradingEngineWebController(TradingEngine tradingEngine, MarketDataService marketDataService) {
        this.tradingEngine = tradingEngine;
        this.marketDataService = marketDataService;
    }

    @GetMapping("/symbols")
    public List<String> getSymbols() {
        if (symbolConfig == null || symbolConfig.trim().isEmpty()) {
            return Arrays.asList("BTCUSD", "ETHUSD", "SOLUSD");
        }
        return Arrays.stream(symbolConfig.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @GetMapping("/orderbook")
    public ResponseEntity<OrderBookSnapshot> getOrderBook(@RequestParam String symbol) {
        OrderBookSnapshot snapshot = tradingEngine.getOrderBookSnapshot(symbol);
        if (snapshot == null) {
            snapshot = OrderBookSnapshot.builder()
                    .symbol(symbol)
                    .bids(Collections.emptyList())
                    .asks(Collections.emptyList())
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
        return ResponseEntity.ok(snapshot);
    }

    @GetMapping("/trades")
    public ResponseEntity<List<Trade>> getTrades(@RequestParam String symbol) {
        List<Trade> trades = marketDataService.getRecentTrades(symbol);
        return ResponseEntity.ok(trades);
    }

    @PostMapping("/order")
    public ResponseEntity<Map<String, Object>> submitOrder(@RequestBody OrderSubmissionRequest req) {
        try {
            long orderId = orderIdCounter.incrementAndGet();
            OrderSide side = "BUY".equalsIgnoreCase(req.side()) ? OrderSide.BUY : OrderSide.SELL;
            OrderType type = translateType(req.type());

            Order order = Order.builder()
                    .orderId(orderId)
                    .symbol(req.symbol())
                    .price(req.price())
                    .quantity(req.quantity())
                    .remainingQuantity(req.quantity())
                    .side(side)
                    .type(type)
                    .userId(req.userId() <= 0 ? 888 : req.userId())
                    .timestamp(System.currentTimeMillis())
                    .build();

            tradingEngine.submitOrder(order);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "orderId", orderId,
                    "message", "Order submitted successfully to the Disruptor ring buffer."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Failed to submit order: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(@RequestBody CancelSubmissionRequest req) {
        try {
            tradingEngine.cancelOrder(req.orderId(), req.symbol());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cancel request submitted for Order ID: " + req.orderId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Failed to cancel order: " + e.getMessage()
            ));
        }
    }

    private OrderType translateType(String typeStr) {
        if (typeStr == null) return OrderType.LIMIT;
        return switch (typeStr.toUpperCase()) {
            case "MARKET" -> OrderType.MARKET;
            case "IOC" -> OrderType.IOC;
            default -> OrderType.LIMIT;
        };
    }

    public record OrderSubmissionRequest(
            String symbol,
            String side,
            long price,
            long quantity,
            String type,
            long userId
    ) {}

    public record CancelSubmissionRequest(
            long orderId,
            String symbol
    ) {}
}
