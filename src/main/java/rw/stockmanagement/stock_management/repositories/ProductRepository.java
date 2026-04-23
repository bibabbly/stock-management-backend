package rw.stockmanagement.stock_management.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.stockmanagement.stock_management.models.Product;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByShopId(Long shopId);
    Page<Product> findByShopId(Long shopId, Pageable pageable);
    Page<Product> findByShopIdAndNameContainingIgnoreCaseOrShopIdAndCategoryContainingIgnoreCase(Long shopId1, String name, Long shopId2, String category, Pageable pageable);
    List<Product> findByShopIdAndQuantityLessThan(Long shopId, Integer quantity);
    @Query("SELECT p FROM Product p WHERE p.shop.id = :shopId AND p.quantity <= p.minStock")
    List<Product> findByShopIdAndQuantityLessThanEqualMinStock(@Param("shopId") Long shopId);
    long countByShopId(Long shopId);
}