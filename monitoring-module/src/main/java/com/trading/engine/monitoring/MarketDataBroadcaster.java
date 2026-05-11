package com.trading.engine.monitoring;

import com.trading.engine.core.model.Trade;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarketDataBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public MarketDataBroadcaster(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastTrades(List<Trade> trades) {
        for (Trade trade : trades) {
            messagingTemplate.convertAndSend("/topic/trades", trade);
        }
    }
    
    public void broadcastOrderBook(String symbol, Object orderBookSnapshot) {
        messagingTemplate.convertAndSend("/topic/orderbook/" + symbol, orderBookSnapshot);
    }
}
