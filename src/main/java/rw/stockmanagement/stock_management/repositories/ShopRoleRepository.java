package rw.stockmanagement.stock_management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.stockmanagement.stock_management.models.ShopRole;
import java.util.List;

public interface ShopRoleRepository extends JpaRepository<ShopRole, Long> {
    List<ShopRole> findByShopId(Long shopId);
}