package com.trading.engine.core.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class OrderBookSnapshot {
    private String symbol;
    private List<PriceLevelData> bids;
    private List<PriceLevelData> asks;
    private long timestamp;

    @Data
    @Builder
    public static class PriceLevelData {
        private long price;
        private long quantity;
    }
}
