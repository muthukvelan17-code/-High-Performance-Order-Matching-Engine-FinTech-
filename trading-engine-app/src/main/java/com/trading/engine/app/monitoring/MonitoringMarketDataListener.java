package com.trading.engine.app.monitoring;

import com.trading.engine.core.engine.MarketDataListener;
import com.trading.engine.core.model.Trade;
import com.trading.engine.monitoring.MarketDataBroadcaster;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Primary
@RequiredArgsConstructor
public class MonitoringMarketDataListener implements MarketDataListener {

    private final MarketDataBroadcaster broadcaster;

    @Override
    public void onTrade(List<Trade> trades) {
        broadcaster.broadcastTrades(trades);
    }

    @Override
    public void onOrderBookUpdate(String symbol) {
        // In a real app, we'd fetch the snapshot from the order book
        // For the dashboard demo, we'll focus on trades first
        broadcaster.broadcastOrderBook(symbol, "OrderBook Updated");
    }
}
