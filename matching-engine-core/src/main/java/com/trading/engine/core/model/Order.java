package com.trading.engine.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true)
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    private long orderId;
    private String symbol;
    private long price;
    private long quantity;
    private long remainingQuantity;
    private OrderSide side;
    private OrderType type;
    private OrderStatus status;
    private long timestamp;
    private long userId;
    
    // Intrusive linked list pointers for zero-allocation PriceLevel queues
    private Order next;
    private Order prev;
}
