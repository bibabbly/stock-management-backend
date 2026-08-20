package rw.stockmanagement.stock_management.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.stockmanagement.stock_management.models.StockTransfer;
import java.time.LocalDateTime;
import java.util.List;

public interface StockTransferRepository extends JpaRepository<StockTransfer, Long> {
    Page<StockTransfer> findByShopIdOrderByCreatedAtDesc(Long shopId, Pageable pageable);
    List<StockTransfer> findByShopId(Long shopId);

    @Query("SELECT t FROM StockTransfer t WHERE t.shop.id = :shopId " +
            "AND (:search IS NULL OR :search = '' OR LOWER(t.product.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:startDate IS NULL OR t.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR t.createdAt <= :endDate) " +
            "ORDER BY t.createdAt DESC")
    Page<StockTransfer> search(@Param("shopId") Long shopId,
                               @Param("search") String search,
                               @Param("startDate") LocalDateTime startDate,
                               @Param("endDate") LocalDateTime endDate,
                               Pageable pageable);
}