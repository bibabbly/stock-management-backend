package rw.stockmanagement.stock_management.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rw.stockmanagement.stock_management.models.Product;
import rw.stockmanagement.stock_management.repositories.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final SupplierRepository supplierRepository;

    public Map<String, Object> getDashboard(Long shopId) {
        Map<String, Object> dashboard = new HashMap<>();

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        double todayRevenue = saleRepository.sumTotalAmountByShopIdAndDateBetween(shopId, startOfDay, endOfDay);
        double monthRevenue = saleRepository.sumTotalAmountByShopIdAndDateBetween(shopId, startOfMonth, endOfDay);
        long todaySalesCount = saleRepository.countByShopIdAndDateBetween(shopId, startOfDay, endOfDay);
        double todayPurchaseCost = saleRepository.sumPurchaseCostByShopIdAndDateBetween(shopId, startOfDay, endOfDay);
        double monthPurchaseCost = saleRepository.sumPurchaseCostByShopIdAndDateBetween(shopId, startOfMonth, endOfDay);

        List<Product> lowStockProducts = productRepository.findByShopIdAndQuantityLessThanEqualMinStock(shopId);
        long totalProducts = productRepository.countByShopId(shopId);
        long totalSuppliers = supplierRepository.countByShopId(shopId);

        dashboard.put("todayRevenue", todayRevenue);
        dashboard.put("monthRevenue", monthRevenue);
        dashboard.put("todaySalesCount", todaySalesCount);
        dashboard.put("lowStockProducts", lowStockProducts);
        dashboard.put("lowStockCount", lowStockProducts.size());
        dashboard.put("totalProducts", totalProducts);
        dashboard.put("totalSuppliers", totalSuppliers);
        dashboard.put("monthPurchaseCost", monthPurchaseCost);
        dashboard.put("monthProfit", monthRevenue - monthPurchaseCost);
        dashboard.put("todayProfit", todayRevenue - todayPurchaseCost);

        return dashboard;
    }
}