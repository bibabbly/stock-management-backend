package rw.stockmanagement.stock_management.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.stockmanagement.stock_management.models.Debt;
import java.time.LocalDate;
import java.util.List;

public interface DebtRepository extends JpaRepository<Debt, Long> {

    Page<Debt> findByShopIdOrderByDueDateAsc(Long shopId, Pageable pageable);

    Page<Debt> findByShopIdAndDebtTypeOrderByDueDateAsc(Long shopId, Debt.DebtType debtType, Pageable pageable);

    Page<Debt> findByShopIdAndStatusOrderByDueDateAsc(Long shopId, Debt.DebtStatus status, Pageable pageable);

    Page<Debt> findByShopIdAndDebtTypeAndStatusOrderByDueDateAsc(
            Long shopId, Debt.DebtType debtType, Debt.DebtStatus status, Pageable pageable);

    // For scheduler — find debts due tomorrow
    @Query("SELECT d FROM Debt d WHERE d.dueDate = :tomorrow AND d.status != 'PAID'")
    List<Debt> findDebtsDueTomorrow(@Param("tomorrow") LocalDate tomorrow);

    // Summary queries
    @Query("SELECT COALESCE(SUM(d.totalAmount - d.paidAmount), 0) FROM Debt d WHERE d.shop.id = :shopId AND d.status != 'PAID' AND d.debtType = 'CUSTOMER'")
    Double getTotalCustomerDebt(@Param("shopId") Long shopId);

    @Query("SELECT COALESCE(SUM(d.totalAmount - d.paidAmount), 0) FROM Debt d WHERE d.shop.id = :shopId AND d.status != 'PAID' AND d.debtType = 'SUPPLIER'")
    Double getTotalSupplierDebt(@Param("shopId") Long shopId);
}