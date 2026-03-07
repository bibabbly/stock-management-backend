package rw.stockmanagement.stock_management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.stockmanagement.stock_management.models.Sale;
import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByShopId(Long shopId);
    List<Sale> findByShopIdAndDateBetween(Long shopId, LocalDateTime start, LocalDateTime end);

}
