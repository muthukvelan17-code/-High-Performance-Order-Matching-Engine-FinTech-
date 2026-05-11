package com.trading.engine.core.model;

import lombok.Builder;
import java.io.Serializable;

@Builder
public record Trade(
    long tradeId,
    long makerOrderId,
    long takerOrderId,
    String symbol,
    long price,
    long quantity,
    long timestamp
) implements Serializable {}
