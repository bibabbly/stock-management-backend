package rw.stockmanagement.stock_management.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.stockmanagement.stock_management.models.Sale;
import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    // Simple paginated query — only COMPLETED
    Page<Sale> findByShopIdAndStatusOrderByDateDesc(Long shopId, Sale.SaleStatus status, Pageable pageable);

    // Fetch full details for specific IDs only
    @Query("SELECT DISTINCT s FROM Sale s LEFT JOIN FETCH s.items i LEFT JOIN FETCH i.product LEFT JOIN FETCH s.user LEFT JOIN FETCH s.supplier WHERE s.id IN :ids ORDER BY s.date DESC")
    List<Sale> findByIdsWithDetails(@Param("ids") List<Long> ids);

    // Unpaged for reports — only COMPLETED
    @Query("SELECT DISTINCT s FROM Sale s LEFT JOIN FETCH s.items i LEFT JOIN FETCH i.product LEFT JOIN FETCH s.user LEFT JOIN FETCH s.supplier WHERE s.shop.id = :shopId AND s.status = 'COMPLETED' ORDER BY s.date DESC")
    List<Sale> findByShopId(@Param("shopId") Long shopId, Pageable pageable);

    List<Sale> findByShopId(Long shopId);

    List<Sale> findByShopIdAndDateBetween(Long shopId, LocalDateTime start, LocalDateTime end);

    // Date range — only COMPLETED
    @Query("SELECT DISTINCT s FROM Sale s LEFT JOIN FETCH s.items i LEFT JOIN FETCH i.product LEFT JOIN FETCH s.user LEFT JOIN FETCH s.supplier WHERE s.shop.id = :shopId AND s.date BETWEEN :start AND :end AND s.status = 'COMPLETED' ORDER BY s.date DESC")
    List<Sale> findByShopIdAndDateBetweenOptimized(@Param("shopId") Long shopId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // For daily email report — only COMPLETED
    @Query("SELECT DISTINCT s FROM Sale s LEFT JOIN FETCH s.items i LEFT JOIN FETCH i.product WHERE s.shop.id = :shopId AND s.date BETWEEN :start AND :end AND s.status = 'COMPLETED'")
    List<Sale> findByShopIdAndDateBetweenWithItems(@Param("shopId") Long shopId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Dashboard — revenue only from COMPLETED sales
    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.shop.id = :shopId AND s.date BETWEEN :start AND :end AND s.status = 'COMPLETED'")
    double sumTotalAmountByShopIdAndDateBetween(@Param("shopId") Long shopId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Dashboard — count only COMPLETED sales
    @Query("SELECT COUNT(s) FROM Sale s WHERE s.shop.id = :shopId AND s.date BETWEEN :start AND :end AND s.status = 'COMPLETED'")
    long countByShopIdAndDateBetween(@Param("shopId") Long shopId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Dashboard — purchase cost only from COMPLETED sales
    @Query("SELECT COALESCE(SUM(si.quantity * si.product.buyingPrice), 0) FROM SaleItem si WHERE si.sale.shop.id = :shopId AND si.sale.date BETWEEN :start AND :end AND si.sale.status = 'COMPLETED'")
    double sumPurchaseCostByShopIdAndDateBetween(@Param("shopId") Long shopId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // All completed sales unpaged
    @Query("SELECT DISTINCT s FROM Sale s LEFT JOIN FETCH s.items i LEFT JOIN FETCH i.product LEFT JOIN FETCH s.user LEFT JOIN FETCH s.supplier WHERE s.shop.id = :shopId AND s.status = 'COMPLETED' ORDER BY s.date DESC")
    List<Sale> findCompletedByShopId(@Param("shopId") Long shopId);
}