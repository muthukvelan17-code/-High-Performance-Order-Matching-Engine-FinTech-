package com.trading.engine.core.engine;

import com.trading.engine.core.model.Trade;
import java.util.List;

public interface MarketDataListener {
    void onTrade(List<Trade> trades);
    void onOrderBookUpdate(com.trading.engine.core.model.OrderBookSnapshot snapshot);
}
