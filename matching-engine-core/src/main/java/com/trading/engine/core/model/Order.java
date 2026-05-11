package com.trading.engine.core.model;

import lombok.Builder;
import java.io.Serializable;

@Builder
public record Order(
    long orderId,
    String symbol,
    long price,
    long quantity,
    long remainingQuantity,
    OrderSide side,
    OrderType type,
    OrderStatus status,
    long timestamp,
    long userId
) implements Serializable {}
