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

    List<Sale> findByShopId(Long shopId);

    // Optimized paginated query with JOIN FETCH to avoid N+1
    @Query(value = "SELECT DISTINCT s FROM Sale s LEFT JOIN FETCH s.items i LEFT JOIN FETCH i.product LEFT JOIN FETCH s.user LEFT JOIN FETCH s.supplier WHERE s.shop.id = :shopId ORDER BY s.date DESC",
            countQuery = "SELECT COUNT(s) FROM Sale s WHERE s.shop.id = :shopId")
    Page<Sale> findByShopIdOrderByDateDesc(@Param("shopId") Long shopId, Pageable pageable);

    // Optimized unpaged query for reports
    @Query("SELECT DISTINCT s FROM Sale s LEFT JOIN FETCH s.items i LEFT JOIN FETCH i.product LEFT JOIN FETCH s.user LEFT JOIN FETCH s.supplier WHERE s.shop.id = :shopId ORDER BY s.date DESC")
    List<Sale> findByShopId(@Param("shopId") Long shopId, Pageable pageable);

    List<Sale> findByShopIdAndDateBetween(Long shopId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT DISTINCT s FROM Sale s LEFT JOIN FETCH s.items i LEFT JOIN FETCH i.product WHERE s.shop.id = :shopId AND s.date BETWEEN :start AND :end")
    List<Sale> findByShopIdAndDateBetweenWithItems(@Param("shopId") Long shopId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.shop.id = :shopId AND s.date BETWEEN :start AND :end")
    double sumTotalAmountByShopIdAndDateBetween(@Param("shopId") Long shopId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.shop.id = :shopId AND s.date BETWEEN :start AND :end")
    long countByShopIdAndDateBetween(@Param("shopId") Long shopId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(si.quantity * si.product.buyingPrice), 0) FROM SaleItem si WHERE si.sale.shop.id = :shopId AND si.sale.date BETWEEN :start AND :end")
    double sumPurchaseCostByShopIdAndDateBetween(@Param("shopId") Long shopId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}