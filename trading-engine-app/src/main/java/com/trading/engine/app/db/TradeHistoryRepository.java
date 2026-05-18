package com.trading.engine.app.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TradeHistoryRepository extends JpaRepository<TradeHistoryEntity, Long> {
    List<TradeHistoryEntity> findBySymbol(String symbol);
}
