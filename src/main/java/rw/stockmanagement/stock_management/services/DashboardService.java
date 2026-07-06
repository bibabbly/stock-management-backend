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

        // Today metrics
        double todayRevenue = saleRepository.sumTotalAmountByShopIdAndDateBetween(shopId, startOfDay, endOfDay);
        double todayPurchaseCost = saleRepository.sumPurchaseCostByShopIdAndDateBetween(shopId, startOfDay, endOfDay);
        long todaySalesCount = saleRepository.countByShopIdAndDateBetween(shopId, startOfDay, endOfDay);

        // Month metrics
        double monthRevenue = saleRepository.sumTotalAmountByShopIdAndDateBetween(shopId, startOfMonth, endOfDay);
        double monthPurchaseCost = saleRepository.sumPurchaseCostByShopIdAndDateBetween(shopId, startOfMonth, endOfDay);

        // Products — single query, reused for everything
        List<Product> allProducts = productRepository.findByShopId(shopId);
        List<Product> lowStockProducts = productRepository.findByShopIdAndQuantityLessThanEqualMinStock(shopId);

        // Stock values calculated in memory — no extra DB query needed
        double stockValueAtCost = allProducts.stream()
                .mapToDouble(p -> (p.getBuyingPrice() != null ? p.getBuyingPrice() : 0.0) * p.getQuantity())
                .sum();

        double stockValueAtSale = allProducts.stream()
                .mapToDouble(p -> (p.getSellingPrice() != null ? p.getSellingPrice() : 0.0) * p.getQuantity())
                .sum();

        long totalProducts = allProducts.size();
        long totalSuppliers = supplierRepository.countByShopId(shopId);

        // Build response
        dashboard.put("todayRevenue", todayRevenue);
        dashboard.put("todayProfit", todayRevenue - todayPurchaseCost);
        dashboard.put("todaySalesCount", todaySalesCount);
        dashboard.put("monthRevenue", monthRevenue);
        dashboard.put("monthPurchaseCost", monthPurchaseCost);
        dashboard.put("monthProfit", monthRevenue - monthPurchaseCost);
        dashboard.put("totalProducts", totalProducts);
        dashboard.put("totalSuppliers", totalSuppliers);
        dashboard.put("stockValueAtCost", stockValueAtCost);
        dashboard.put("stockValueAtSale", stockValueAtSale);
        dashboard.put("lowStockProducts", lowStockProducts);
        dashboard.put("lowStockCount", lowStockProducts.size());

        return dashboard;
    }
}