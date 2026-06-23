package rw.stockmanagement.stock_management.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.stockmanagement.stock_management.dto.DebtDTO;
import rw.stockmanagement.stock_management.models.Debt;
import rw.stockmanagement.stock_management.models.Shop;
import rw.stockmanagement.stock_management.repositories.DebtRepository;
import rw.stockmanagement.stock_management.repositories.ShopRepository;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DebtService {

    private final DebtRepository debtRepository;
    private final ShopRepository shopRepository;

    public Page<Debt> getDebts(Long shopId, String type, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        boolean hasType = type != null && !type.equals("ALL");
        boolean hasStatus = status != null && !status.equals("ALL");

        if (hasType && hasStatus) {
            return debtRepository.findByShopIdAndDebtTypeAndStatusOrderByDueDateAsc(
                    shopId,
                    Debt.DebtType.valueOf(type),
                    Debt.DebtStatus.valueOf(status),
                    pageable);
        } else if (hasType) {
            return debtRepository.findByShopIdAndDebtTypeOrderByDueDateAsc(
                    shopId, Debt.DebtType.valueOf(type), pageable);
        } else if (hasStatus) {
            return debtRepository.findByShopIdAndStatusOrderByDueDateAsc(
                    shopId, Debt.DebtStatus.valueOf(status), pageable);
        } else {
            return debtRepository.findByShopIdOrderByDueDateAsc(shopId, pageable);
        }
    }

    @Transactional
    public Debt createDebt(DebtDTO dto) {
        Shop shop = shopRepository.findById(dto.getShopId())
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        Debt debt = new Debt();
        debt.setShop(shop);
        debt.setDebtType(Debt.DebtType.valueOf(dto.getDebtType()));
        debt.setName(dto.getName());
        debt.setPhone(dto.getPhone());
        debt.setTotalAmount(dto.getTotalAmount());
        debt.setPaidAmount(dto.getPaidAmount() != null ? dto.getPaidAmount() : 0.0);
        debt.setDueDate(dto.getDueDate());
        debt.setNote(dto.getNote());
        debt.updateStatus();

        return debtRepository.save(debt);
    }

    @Transactional
    public Debt recordPayment(Long debtId, Double amount) {
        Debt debt = debtRepository.findById(debtId)
                .orElseThrow(() -> new RuntimeException("Debt not found"));

        double newPaid = debt.getPaidAmount() + amount;
        if (newPaid > debt.getTotalAmount()) {
            throw new RuntimeException("Payment exceeds remaining balance");
        }

        debt.setPaidAmount(newPaid);
        debt.updateStatus();

        return debtRepository.save(debt);
    }

    @Transactional
    public Debt updateDebt(Long debtId, DebtDTO dto) {
        Debt debt = debtRepository.findById(debtId)
                .orElseThrow(() -> new RuntimeException("Debt not found"));

        debt.setName(dto.getName());
        debt.setPhone(dto.getPhone());
        debt.setTotalAmount(dto.getTotalAmount());
        debt.setDueDate(dto.getDueDate());
        debt.setNote(dto.getNote());
        debt.updateStatus();

        return debtRepository.save(debt);
    }

    @Transactional
    public void deleteDebt(Long debtId) {
        debtRepository.deleteById(debtId);
    }

    public Map<String, Double> getSummary(Long shopId) {
        Map<String, Double> summary = new HashMap<>();
        summary.put("customerDebt", debtRepository.getTotalCustomerDebt(shopId));
        summary.put("supplierDebt", debtRepository.getTotalSupplierDebt(shopId));
        return summary;
    }
}