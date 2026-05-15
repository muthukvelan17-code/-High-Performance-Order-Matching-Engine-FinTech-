package com.trading.engine.app.web;

import com.trading.engine.core.TradingEngine;
import com.trading.engine.core.model.OrderBookSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/market-data")
@RequiredArgsConstructor
public class MarketDataController {

    private final TradingEngine engine;

    @GetMapping("/{symbol}/book")
    public ResponseEntity<OrderBookSnapshot> getOrderBook(@PathVariable("symbol") String symbol) {
        OrderBookSnapshot snapshot = engine.getOrderBookSnapshot(symbol);
        if (snapshot == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(snapshot);
    }
}
