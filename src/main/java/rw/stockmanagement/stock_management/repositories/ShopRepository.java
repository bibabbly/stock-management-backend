package rw.stockmanagement.stock_management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.stockmanagement.stock_management.models.Shop;


public interface ShopRepository extends JpaRepository<Shop, Long> {
}
