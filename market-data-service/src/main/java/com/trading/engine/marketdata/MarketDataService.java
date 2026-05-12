package com.trading.engine.marketdata;

import com.trading.engine.core.engine.MarketDataListener;
import com.trading.engine.core.model.Trade;
import com.trading.engine.grpc.MarketDataUpdate;
import com.trading.engine.grpc.TradeEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MarketDataService implements MarketDataListener {
    private final Map<String, Sinks.Many<MarketDataUpdate>> sinks = new ConcurrentHashMap<>();

    public Flux<MarketDataUpdate> streamMarketData(String symbol) {
        return sinks.computeIfAbsent(symbol, s -> Sinks.many().multicast().directBestEffort())
                .asFlux();
    }

    @Override
    public void onTrade(List<Trade> trades) {
        if (trades.isEmpty()) return;
        String symbol = trades.get(0).symbol();
        
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
                // In a real system, we'd map the snapshot to the Protobuf message
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
