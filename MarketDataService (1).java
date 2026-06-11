package com.trading.engine.marketdata;

import com.trading.engine.core.engine.MarketDataListener;
import com.trading.engine.core.model.Trade;
import com.trading.engine.grpc.MarketDataUpdate;
import com.trading.engine.grpc.TradeEvent;
import com.trading.engine.grpc.PriceLevel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MarketDataService implements MarketDataListener {
    private final Map<String, Sinks.Many<MarketDataUpdate>> sinks = new ConcurrentHashMap<>();
    private final Map<String, List<Trade>> recentTradesMap = new ConcurrentHashMap<>();

    public Flux<MarketDataUpdate> streamMarketData(String symbol) {
        return sinks.computeIfAbsent(symbol, s -> Sinks.many().multicast().directBestEffort())
                .asFlux();
    }

    public List<Trade> getRecentTrades(String symbol) {
        return recentTradesMap.getOrDefault(symbol, Collections.emptyList());
    }

    @Override
    public void onTrade(List<Trade> trades) {
        if (trades.isEmpty()) return;
        String symbol = trades.get(0).symbol();
        
        // Save to recent trades list (keep last 50)
        List<Trade> recentTrades = recentTradesMap.computeIfAbsent(symbol, s -> new CopyOnWriteArrayList<>());
        recentTrades.addAll(0, trades); // Prepend new trades
        while (recentTrades.size() > 50) {
            recentTrades.remove(recentTrades.size() - 1);
        }

        MarketDataUpdate update = MarketDataUpdate.newBuilder()
                .setSymbol(symbol)
                .addAllTrades(trades.stream().map(t -> TradeEvent.newBuilder()
                        .setTradeId(t.tradeId())
                        .setPrice(t.price())
                        .setQuantity(t.quantity())
                        .setTimestamp(t.timestamp())
                        .build()).toList())
                .build();

        broadcast(symbol, update);
    }

    @Override
    public void onOrderBookUpdate(com.trading.engine.core.model.OrderBookSnapshot snapshot) {
        String symbol = snapshot.getSymbol();
        MarketDataUpdate update = MarketDataUpdate.newBuilder()
                .setSymbol(symbol)
                .addAllBids(snapshot.getBids().stream().map(b -> PriceLevel.newBuilder()
                        .setPrice(b.getPrice())
                        .setQuantity(b.getQuantity())
                        .build()).toList())
                .addAllAsks(snapshot.getAsks().stream().map(a -> PriceLevel.newBuilder()
                        .setPrice(a.getPrice())
                        .setQuantity(a.getQuantity())
                        .build()).toList())
                .build();
        broadcast(symbol, update);
    }

    private void broadcast(String symbol, MarketDataUpdate update) {
        Sinks.Many<MarketDataUpdate> sink = sinks.get(symbol);
        if (sink != null) {
            sink.tryEmitNext(update);
        }
    }
}
