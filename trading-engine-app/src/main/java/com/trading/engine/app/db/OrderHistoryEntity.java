package com.trading.engine.app.db;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistoryEntity {
    @Id
    private Long orderId;
    private String symbol;
    private Long price;
    private Long quantity;
    private Long remainingQuantity;
    private String side;
    private String type;
    private String status;
    private Long timestamp;
    private Long userId;
}
