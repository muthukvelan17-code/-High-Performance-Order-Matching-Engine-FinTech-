package com.trading.engine.app.web;

import com.trading.engine.app.db.OrderHistoryEntity;
import com.trading.engine.app.db.OrderHistoryRepository;
import com.trading.engine.app.db.TradeHistoryEntity;
import com.trading.engine.app.db.TradeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final TradeHistoryRepository tradeHistoryRepository;
    private final OrderHistoryRepository orderHistoryRepository;

    @GetMapping("/trades")
    public List<TradeHistoryEntity> getTradeHistory(@RequestParam(name = "symbol", required = false) String symbol) {
        if (symbol != null && !symbol.isEmpty()) {
            return tradeHistoryRepository.findBySymbol(symbol.toUpperCase());
        }
        return tradeHistoryRepository.findAll();
    }

    @GetMapping("/orders")
    public List<OrderHistoryEntity> getOrderHistory(
            @RequestParam(name = "symbol", required = false) String symbol,
            @RequestParam(name = "side", required = false) String side) {
        
        boolean hasSymbol = symbol != null && !symbol.isEmpty();
        boolean hasSide = side != null && !side.isEmpty();

        if (hasSymbol && hasSide) {
            return orderHistoryRepository.findBySymbolAndSide(symbol.toUpperCase(), side.toUpperCase());
        } else if (hasSymbol) {
            return orderHistoryRepository.findBySymbol(symbol.toUpperCase());
        } else if (hasSide) {
            return orderHistoryRepository.findBySide(side.toUpperCase());
        }
        return orderHistoryRepository.findAll();
    }
}
