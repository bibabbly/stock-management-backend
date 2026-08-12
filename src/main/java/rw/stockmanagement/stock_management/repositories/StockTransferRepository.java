package rw.stockmanagement.stock_management.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.stockmanagement.stock_management.models.StockTransfer;
import java.util.List;

public interface StockTransferRepository extends JpaRepository<StockTransfer, Long> {
    Page<StockTransfer> findByShopIdOrderByCreatedAtDesc(Long shopId, Pageable pageable);
    List<StockTransfer> findByShopId(Long shopId);
}