package rw.stockmanagement.stock_management.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.stockmanagement.stock_management.models.Supplier;
import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findByShopId(Long shopId);
    Page<Supplier> findByShopIdOrderByCreatedAtDesc(Long shopId, Pageable pageable);
    Page<Supplier> findByShopIdAndNameContainingIgnoreCaseOrderByCreatedAtDesc(Long shopId, String name, Pageable pageable);
    long countByShopId(Long shopId);
}