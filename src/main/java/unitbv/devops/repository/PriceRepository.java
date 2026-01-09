package unitbv.devops.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unitbv.devops.entity.Price;
import unitbv.devops.entity.Symbol;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {
    List<Price> findBySymbolOrderByTimestampDesc(Symbol symbol);
    List<Price> findBySymbolAndTimestampBetween(Symbol symbol, LocalDateTime start, LocalDateTime end);
    Price findFirstBySymbolOrderByTimestampDesc(Symbol symbol);
}

