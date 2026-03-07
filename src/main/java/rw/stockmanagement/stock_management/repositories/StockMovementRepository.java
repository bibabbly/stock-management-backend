package rw.stockmanagement.stock_management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.stockmanagement.stock_management.models.StockMovement;

import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByShopId(Long shopId);
    List<StockMovement> findByProductId(Long productId);
    List<StockMovement> findByShopIdAndType(Long shopId, StockMovement.MovementType type);
}
