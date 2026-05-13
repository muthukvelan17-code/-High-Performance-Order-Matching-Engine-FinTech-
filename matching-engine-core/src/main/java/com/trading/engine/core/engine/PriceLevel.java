package com.trading.engine.core.engine;

import com.trading.engine.core.model.Order;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(fluent = true)
public class PriceLevel {
    private final long price;
    private Order head;
    private Order tail;
    private long totalQuantity;

    public PriceLevel(long price) {
        this.price = price;
        this.totalQuantity = 0;
    }

    public void addOrder(Order order) {
        if (tail == null) {
            head = order;
            tail = order;
            order.prev(null);
            order.next(null);
        } else {
            tail.next(order);
            order.prev(tail);
            order.next(null);
            tail = order;
        }
        totalQuantity += order.remainingQuantity();
    }

    public void removeOrder(Order order) {
        if (order.prev() != null) {
            order.prev().next(order.next());
        } else {
            head = order.next();
        }

        if (order.next() != null) {
            order.next().prev(order.prev());
        } else {
            tail = order.prev();
        }

        order.next(null);
        order.prev(null);
        totalQuantity -= order.remainingQuantity();
    }

    public boolean isEmpty() {
        return head == null;
    }
}
