package rw.stockmanagement.stock_management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.stockmanagement.stock_management.models.StockLocation;
import java.util.List;
import java.util.Optional;

public interface StockLocationRepository extends JpaRepository<StockLocation, Long> {
    List<StockLocation> findByShopId(Long shopId);
    Optional<StockLocation> findByShopIdAndIsMainTrue(Long shopId);
    long countByShopId(Long shopId);
}