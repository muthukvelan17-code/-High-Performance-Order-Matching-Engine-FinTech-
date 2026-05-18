package com.trading.engine.app.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistoryEntity, Long> {
    List<OrderHistoryEntity> findBySymbol(String symbol);
    List<OrderHistoryEntity> findBySymbolAndSide(String symbol, String side);
    List<OrderHistoryEntity> findBySide(String side);
}
