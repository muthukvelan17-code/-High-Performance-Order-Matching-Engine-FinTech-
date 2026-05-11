package com.trading.engine.core.engine;

import com.trading.engine.core.model.Order;
import java.util.LinkedList;
import java.util.Queue;
import lombok.Getter;

@Getter
public class PriceLevel {
    private final long price;
    private final Queue<Order> orders;
    private long totalQuantity;

    public PriceLevel(long price) {
        this.price = price;
        this.orders = new LinkedList<>();
        this.totalQuantity = 0;
    }

    public void addOrder(Order order) {
        orders.add(order);
        totalQuantity += order.remainingQuantity();
    }

    public void removeOrder(Order order) {
        if (orders.remove(order)) {
            totalQuantity -= order.remainingQuantity();
        }
    }

    public boolean isEmpty() {
        return orders.isEmpty();
    }
}
