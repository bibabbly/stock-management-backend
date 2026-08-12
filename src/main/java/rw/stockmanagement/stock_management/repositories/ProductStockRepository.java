package rw.stockmanagement.stock_management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.stockmanagement.stock_management.models.ProductStock;
import java.util.List;
import java.util.Optional;

public interface ProductStockRepository extends JpaRepository<ProductStock, Long> {

    Optional<ProductStock> findByProductIdAndLocationId(Long productId, Long locationId);

    List<ProductStock> findByLocationId(Long locationId);

    List<ProductStock> findByProductId(Long productId);

    // Get total quantity across all locations for a product
    @Query("SELECT COALESCE(SUM(ps.quantity), 0) FROM ProductStock ps WHERE ps.product.id = :productId")
    Integer getTotalQuantityByProductId(@Param("productId") Long productId);

    // Get total stock value at cost for a shop
    @Query("SELECT COALESCE(SUM(ps.quantity * p.buyingPrice), 0) FROM ProductStock ps " +
            "JOIN ps.product p JOIN ps.location l WHERE l.shop.id = :shopId")
    Double getTotalStockValueAtCostByShopId(@Param("shopId") Long shopId);

    // Get total stock value at selling price for a shop
    @Query("SELECT COALESCE(SUM(ps.quantity * p.sellingPrice), 0) FROM ProductStock ps " +
            "JOIN ps.product p JOIN ps.location l WHERE l.shop.id = :shopId")
    Double getTotalStockValueAtSaleByShopId(@Param("shopId") Long shopId);

    // Get all product stocks for a shop across all locations
    @Query("SELECT ps FROM ProductStock ps JOIN ps.location l WHERE l.shop.id = :shopId")
    List<ProductStock> findByShopId(@Param("shopId") Long shopId);

    // Low stock — total quantity across locations <= minStock
    @Query("SELECT p FROM rw.stockmanagement.stock_management.models.Product p " +
            "WHERE p.shop.id = :shopId " +
            "AND (SELECT COALESCE(SUM(ps.quantity), 0) FROM ProductStock ps WHERE ps.product.id = p.id) <= p.minStock")
    List<rw.stockmanagement.stock_management.models.Product> findLowStockProducts(@Param("shopId") Long shopId);
}