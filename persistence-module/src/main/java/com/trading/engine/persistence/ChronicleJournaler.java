package com.trading.engine.persistence;

import com.trading.engine.core.model.Order;
import com.trading.engine.core.model.Trade;
import com.trading.engine.core.persistence.Journaler;
import net.openhft.chronicle.map.ChronicleMap;
import java.io.File;
import java.io.IOException;

public class ChronicleJournaler implements Journaler {
    private final ChronicleMap<Long, Order> orderMap;
    private final ChronicleMap<Long, Trade> tradeMap;

    public ChronicleJournaler() throws IOException {
        String dataDir = System.getProperty("trading.engine.data-dir", System.getenv().getOrDefault("DATA_DIR", "."));
        File dataDirFile = new File(dataDir);
        if (!dataDirFile.exists()) {
            dataDirFile.mkdirs();
        }
        
        File ordersFile = new File(dataDirFile, "orders.dat");
        File tradesFile = new File(dataDirFile, "trades.dat");

        this.orderMap = ChronicleMap
                .of(Long.class, Order.class)
                .name("order-map")
                .entries(1_000_000)
                .averageValue(Order.builder().symbol("BTCUSD").build()) // Template for size estimation
                .createPersistedTo(ordersFile);

        this.tradeMap = ChronicleMap
                .of(Long.class, Trade.class)
                .name("trade-map")
                .entries(1_000_000)
                .averageValue(Trade.builder().symbol("BTCUSD").build())
                .createPersistedTo(tradesFile);
    }

    @Override
    public void journalOrder(Order order) {
        orderMap.put(order.orderId(), order);
    }

    @Override
    public void journalTrade(Trade trade) {
        tradeMap.put(trade.tradeId(), trade);
    }

    @Override
    public void journalCancel(long orderId) {
        orderMap.remove(orderId);
    }
}
