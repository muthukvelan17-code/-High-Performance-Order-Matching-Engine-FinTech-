package com.trading.engine.core.engine;

import com.trading.engine.core.model.Order;
import com.trading.engine.core.model.OrderSide;
import com.trading.engine.core.model.Trade;
import org.agrona.collections.Long2ObjectHashMap;
import java.util.*;

public class OrderBook {
    private static final int MAX_PRICE_LEVELS = 10000;
    
    private final String symbol;
    private final Long2ObjectHashMap<PriceLevel> bidLevels; 
    private final Long2ObjectHashMap<PriceLevel> askLevels;
    
    private final long[] bidPrices;
    private int numBids;
    
    private final long[] askPrices;
    private int numAsks;
    
    private final Long2ObjectHashMap<Order> orderMap;

    public OrderBook(String symbol) {
        this.symbol = symbol;
        this.bidLevels = new Long2ObjectHashMap<>();
        this.askLevels = new Long2ObjectHashMap<>();
        this.bidPrices = new long[MAX_PRICE_LEVELS];
        this.askPrices = new long[MAX_PRICE_LEVELS];
        this.numBids = 0;
        this.numAsks = 0;
        this.orderMap = new Long2ObjectHashMap<>();
    }

    public List<Trade> processOrder(Order takerOrder) {
        List<Trade> trades = new ArrayList<>(); 
        
        if (takerOrder.side() == OrderSide.BUY) {
            matchOrder(takerOrder, askLevels, askPrices, numAsks, true, trades);
        } else {
            matchOrder(takerOrder, bidLevels, bidPrices, numBids, false, trades);
        }

        if (takerOrder.remainingQuantity() > 0) {
            addLimitOrder(takerOrder);
        }

        return trades;
    }

    private void matchOrder(Order takerOrder, Long2ObjectHashMap<PriceLevel> oppositeLevels, long[] oppositePrices, int numOppositePrices, boolean isOppositeAsk, List<Trade> trades) {
        long remainingQty = takerOrder.remainingQuantity();
        
        int i = 0;
        while (i < numOppositePrices && remainingQty > 0) {
            long price = oppositePrices[i];
            
            if (takerOrder.side() == OrderSide.BUY && price > takerOrder.price()) break;
            if (takerOrder.side() == OrderSide.SELL && price < takerOrder.price()) break;

            PriceLevel level = oppositeLevels.get(price);
            Order makerOrder = level.head();
            
            while (makerOrder != null && remainingQty > 0) {
                Order nextOrder = makerOrder.next();
                long matchQty = Math.min(remainingQty, makerOrder.remainingQuantity());
                
                Trade trade = Trade.builder()
                        .tradeId(System.nanoTime()) 
                        .makerOrderId(makerOrder.orderId())
                        .takerOrderId(takerOrder.orderId())
                        .symbol(symbol)
                        .price(price)
                        .quantity(matchQty)
                        .timestamp(System.currentTimeMillis())
                        .build();
                
                trades.add(trade);
                remainingQty -= matchQty;
                
                makerOrder.remainingQuantity(makerOrder.remainingQuantity() - matchQty);
                level.totalQuantity(level.totalQuantity() - matchQty);
                
                if (makerOrder.remainingQuantity() == 0) {
                    level.removeOrder(makerOrder);
                    orderMap.remove(makerOrder.orderId());
                }
                
                makerOrder = nextOrder;
            }
            
            if (level.isEmpty()) {
                removePrice(oppositePrices, i, numOppositePrices);
                oppositeLevels.remove(price);
                if (isOppositeAsk) numAsks--; else numBids--;
                numOppositePrices--;
            } else {
                i++;
            }
        }
        takerOrder.remainingQuantity(remainingQty);
    }

    private void addLimitOrder(Order order) {
        if (order.type() == com.trading.engine.core.model.OrderType.LIMIT) {
            boolean isBuy = order.side() == OrderSide.BUY;
            Long2ObjectHashMap<PriceLevel> levels = isBuy ? bidLevels : askLevels;
            PriceLevel level = levels.get(order.price());
            
            if (level == null) {
                level = new PriceLevel(order.price());
                levels.put(order.price(), level);
                if (isBuy) {
                    insertPrice(bidPrices, numBids, order.price(), true);
                    numBids++;
                } else {
                    insertPrice(askPrices, numAsks, order.price(), false);
                    numAsks++;
                }
            }
            level.addOrder(order);
            orderMap.put(order.orderId(), order);
        }
    }

    public void cancelOrder(long orderId) {
        Order order = orderMap.remove(orderId);
        if (order != null) {
            boolean isBuy = order.side() == OrderSide.BUY;
            Long2ObjectHashMap<PriceLevel> levels = isBuy ? bidLevels : askLevels;
            PriceLevel level = levels.get(order.price());
            
            if (level != null) {
                level.removeOrder(order);
                if (level.isEmpty()) {
                    levels.remove(order.price());
                    if (isBuy) {
                        removePrice(bidPrices, indexOf(bidPrices, numBids, order.price()), numBids);
                        numBids--;
                    } else {
                        removePrice(askPrices, indexOf(askPrices, numAsks, order.price()), numAsks);
                        numAsks--;
                    }
                }
            }
        }
    }

    private void insertPrice(long[] prices, int numPrices, long price, boolean descending) {
        int i = 0;
        while (i < numPrices) {
            if (descending) {
                if (price > prices[i]) break;
            } else {
                if (price < prices[i]) break;
            }
            i++;
        }
        if (i < numPrices) {
            System.arraycopy(prices, i, prices, i + 1, numPrices - i);
        }
        prices[i] = price;
    }

    private void removePrice(long[] prices, int index, int numPrices) {
        if (index >= 0 && index < numPrices) {
            System.arraycopy(prices, index + 1, prices, index, numPrices - index - 1);
        }
    }

    private int indexOf(long[] prices, int numPrices, long price) {
        for (int i = 0; i < numPrices; i++) {
            if (prices[i] == price) return i;
        }
        return -1;
    }

    public com.trading.engine.core.model.OrderBookSnapshot getSnapshot(int depth) {
        return com.trading.engine.core.model.OrderBookSnapshot.builder()
                .symbol(symbol)
                .bids(getTopLevels(bidLevels, bidPrices, numBids, depth))
                .asks(getTopLevels(askLevels, askPrices, numAsks, depth))
                .timestamp(System.currentTimeMillis())
                .build();
    }

    private List<com.trading.engine.core.model.OrderBookSnapshot.PriceLevelData> getTopLevels(Long2ObjectHashMap<PriceLevel> levels, long[] prices, int numPrices, int depth) {
        List<com.trading.engine.core.model.OrderBookSnapshot.PriceLevelData> snapshotLevels = new ArrayList<>();
        int count = Math.min(depth, numPrices);
        for (int i = 0; i < count; i++) {
            long price = prices[i];
            snapshotLevels.add(com.trading.engine.core.model.OrderBookSnapshot.PriceLevelData.builder()
                    .price(price)
                    .quantity(levels.get(price).totalQuantity())
                    .build());
        }
        return snapshotLevels;
    }
}
