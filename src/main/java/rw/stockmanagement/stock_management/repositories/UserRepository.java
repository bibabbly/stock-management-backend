package rw.stockmanagement.stock_management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.stockmanagement.stock_management.models.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByShopId(Long shopId);
}