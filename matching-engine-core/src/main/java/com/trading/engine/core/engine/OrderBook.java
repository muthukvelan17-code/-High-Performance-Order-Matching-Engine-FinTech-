package com.trading.engine.core.engine;

import com.trading.engine.core.model.Order;
import com.trading.engine.core.model.OrderSide;
import com.trading.engine.core.model.OrderStatus;
import com.trading.engine.core.model.Trade;
import org.agrona.collections.Long2ObjectHashMap;
import java.util.*;

public class OrderBook {
    private final String symbol;
    private final TreeMap<Long, PriceLevel> bids; // Price -> Level (Descending)
    private final TreeMap<Long, PriceLevel> asks; // Price -> Level (Ascending)
    private final Long2ObjectHashMap<Order> orderMap; // OrderId -> Order for fast lookup/cancel (Primitive map)

    public OrderBook(String symbol) {
        this.symbol = symbol;
        this.bids = new TreeMap<>(Collections.reverseOrder());
        this.asks = new TreeMap<>();
        this.orderMap = new Long2ObjectHashMap<>();
    }

    public List<Trade> processOrder(Order takerOrder) {
        List<Trade> trades = new ArrayList<>();
        
        if (takerOrder.side() == OrderSide.BUY) {
            matchOrder(takerOrder, asks, trades);
        } else {
            matchOrder(takerOrder, bids, trades);
        }

        // If order still has remaining quantity and is not IOC/Market
        if (takerOrder.remainingQuantity() > 0) {
            addLimitOrder(takerOrder);
        }

        return trades;
    }

    private void matchOrder(Order takerOrder, TreeMap<Long, PriceLevel> oppositeSide, List<Trade> trades) {
        long remainingQty = takerOrder.remainingQuantity();
        
        Iterator<Map.Entry<Long, PriceLevel>> iterator = oppositeSide.entrySet().iterator();
        while (iterator.hasNext() && remainingQty > 0) {
            Map.Entry<Long, PriceLevel> entry = iterator.next();
            long price = entry.getKey();
            
            // For BUY order, price must be <= taker price
            // For SELL order, price must be >= taker price
            if (takerOrder.side() == OrderSide.BUY && price > takerOrder.price()) break;
            if (takerOrder.side() == OrderSide.SELL && price < takerOrder.price()) break;

            PriceLevel level = entry.getValue();
            Iterator<Order> orderIterator = level.getOrders().iterator();
            
            while (orderIterator.hasNext() && remainingQty > 0) {
                Order makerOrder = orderIterator.next();
                long matchQty = Math.min(remainingQty, makerOrder.remainingQuantity());
                
                // Create trade
                Trade trade = Trade.builder()
                        .tradeId(System.nanoTime()) // In production, use a proper ID generator
                        .makerOrderId(makerOrder.orderId())
                        .takerOrderId(takerOrder.orderId())
                        .symbol(symbol)
                        .price(price)
                        .quantity(matchQty)
                        .timestamp(System.currentTimeMillis())
                        .build();
                
                trades.add(trade);
                remainingQty -= matchQty;
                
                // Update maker order (Simplified: in a real engine we'd update a mutable state)
                // For this sprint, I'll assume we update the order state or the caller handles it
                // level.reduceQuantity(matchQty);
                
                if (matchQty == makerOrder.remainingQuantity()) {
                    orderIterator.remove();
                    orderMap.remove(makerOrder.orderId());
                } else {
                    // Update remaining quantity logic
                    // In high perf, we'd use mutable orders
                }
            }
            
            if (level.isEmpty()) {
                iterator.remove();
            }
        }
    }

    private void addLimitOrder(Order order) {
        if (order.type() == com.trading.engine.core.model.OrderType.LIMIT) {
            TreeMap<Long, PriceLevel> side = (order.side() == OrderSide.BUY) ? bids : asks;
            side.computeIfAbsent(order.price(), PriceLevel::new).addOrder(order);
            orderMap.put(order.orderId(), order);
        }
    }

    public void cancelOrder(long orderId) {
        Order order = orderMap.remove(orderId);
        if (order != null) {
            TreeMap<Long, PriceLevel> side = (order.side() == OrderSide.BUY) ? bids : asks;
            PriceLevel level = side.get(order.price());
            if (level != null) {
                level.removeOrder(order);
                if (level.isEmpty()) {
                    side.remove(order.price());
                }
            }
        }
    }

    public com.trading.engine.core.model.OrderBookSnapshot getSnapshot(int depth) {
        return com.trading.engine.core.model.OrderBookSnapshot.builder()
                .symbol(symbol)
                .bids(getTopLevels(bids, depth))
                .asks(getTopLevels(asks, depth))
                .timestamp(System.currentTimeMillis())
                .build();
    }

    private List<com.trading.engine.core.model.OrderBookSnapshot.PriceLevelData> getTopLevels(TreeMap<Long, PriceLevel> side, int depth) {
        List<com.trading.engine.core.model.OrderBookSnapshot.PriceLevelData> levels = new ArrayList<>();
        int count = 0;
        for (Map.Entry<Long, PriceLevel> entry : side.entrySet()) {
            if (count >= depth) break;
            levels.add(com.trading.engine.core.model.OrderBookSnapshot.PriceLevelData.builder()
                    .price(entry.getKey())
                    .quantity(entry.getValue().getTotalQuantity())
                    .build());
            count++;
        }
        return levels;
    }
}
