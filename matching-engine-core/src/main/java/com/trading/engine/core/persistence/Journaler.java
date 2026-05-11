package com.trading.engine.core.persistence;

import com.trading.engine.core.model.Order;
import com.trading.engine.core.model.Trade;

public interface Journaler {
    void journalOrder(Order order);
    void journalTrade(Trade trade);
    void journalCancel(long orderId);
}
