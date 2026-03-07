package rw.stockmanagement.stock_management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.stockmanagement.stock_management.models.Product;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByShopId(Long shopId);
    List<Product> findByShopIdAndQuantityLessThan(Long shopId, Integer quantity);
    @Query("SELECT p FROM Product p WHERE p.shop.id = :shopId AND p.quantity <= p.minStock")
    List<Product> findByShopIdAndQuantityLessThanEqualMinStock(@Param("shopId") Long shopId);
}
