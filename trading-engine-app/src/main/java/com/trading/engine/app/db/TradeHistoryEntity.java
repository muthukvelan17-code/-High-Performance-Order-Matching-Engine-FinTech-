package com.trading.engine.app.db;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trade_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeHistoryEntity {
    @Id
    private Long tradeId;
    private Long makerOrderId;
    private Long takerOrderId;
    private String symbol;
    private Long price;
    private Long quantity;
    private Long timestamp;
}
