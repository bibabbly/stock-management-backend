package rw.stockmanagement.stock_management.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.stockmanagement.stock_management.models.StockMovement;
import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByShopId(Long shopId);
    Page<StockMovement> findByShopIdOrderByCreatedAtDesc(Long shopId, Pageable pageable);
    Page<StockMovement> findByShopIdAndTypeOrderByCreatedAtDesc(Long shopId, StockMovement.MovementType type, Pageable pageable);
    Page<StockMovement> findByShopIdAndProductNameContainingIgnoreCaseOrderByCreatedAtDesc(Long shopId, String name, Pageable pageable);
    Page<StockMovement> findByShopIdAndTypeAndProductNameContainingIgnoreCaseOrderByCreatedAtDesc(Long shopId, StockMovement.MovementType type, String name, Pageable pageable);
    List<StockMovement> findByProductId(Long productId);
    List<StockMovement> findByShopIdAndType(Long shopId, StockMovement.MovementType type);
}