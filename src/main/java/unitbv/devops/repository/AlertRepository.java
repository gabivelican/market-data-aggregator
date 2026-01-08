package unitbv.devops.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unitbv.devops.entity.Alert;
import unitbv.devops.entity.Symbol;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findBySymbolOrderByTriggeredAtDesc(Symbol symbol);
    List<Alert> findBySymbolAndTriggeredAtBetween(Symbol symbol, LocalDateTime start, LocalDateTime end);
}
