package com.trading.engine.app.db;

import com.trading.engine.core.model.Order;
import com.trading.engine.core.model.Trade;
import com.trading.engine.core.persistence.Journaler;

import java.util.Arrays;
import java.util.List;

public class CompositeJournaler implements Journaler {
    private final List<Journaler> journalers;

    public CompositeJournaler(Journaler... journalers) {
        this.journalers = Arrays.asList(journalers);
    }

    @Override
    public void journalOrder(Order order) {
        for (Journaler j : journalers) {
            j.journalOrder(order);
        }
    }

    @Override
    public void journalTrade(Trade trade) {
        for (Journaler j : journalers) {
            j.journalTrade(trade);
        }
    }

    @Override
    public void journalCancel(long orderId) {
        for (Journaler j : journalers) {
            j.journalCancel(orderId);
        }
    }
}
