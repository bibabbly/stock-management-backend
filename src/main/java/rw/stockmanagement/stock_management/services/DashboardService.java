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

        // Today's range
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

        // This month's range
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        // Today's sales
        double todayRevenue = saleRepository
                .findByShopIdAndDateBetween(shopId, startOfDay, endOfDay)
                .stream().mapToDouble(s -> s.getTotalAmount()).sum();

        // This month's revenue
        double monthRevenue = saleRepository
                .findByShopIdAndDateBetween(shopId, startOfMonth, endOfDay)
                .stream().mapToDouble(s -> s.getTotalAmount()).sum();

        // Total sales count today
        long todaySalesCount = saleRepository
                .findByShopIdAndDateBetween(shopId, startOfDay, endOfDay).size();

        // Low stock products
        List<Product> lowStockProducts = productRepository.findByShopIdAndQuantityLessThanEqualMinStock(shopId);

        // Total products
        long totalProducts = productRepository.findByShopId(shopId).size();

        // Total suppliers
        long totalSuppliers = supplierRepository.findByShopId(shopId).size();

        // This month's purchase cost (based on items actually sold)
        double monthPurchaseCost = saleRepository
                .findByShopIdAndDateBetween(shopId, startOfMonth, endOfDay)
                .stream()
                .flatMap(sale -> sale.getItems().stream())
                .mapToDouble(item -> item.getQuantity() * (item.getProduct().getBuyingPrice() != null ? item.getProduct().getBuyingPrice() : 0))
                .sum();

        // This month's profit
        double monthProfit = monthRevenue - monthPurchaseCost;

        // Today's purchase cost (based on items actually sold today)
        double todayPurchaseCost = saleRepository
                .findByShopIdAndDateBetween(shopId, startOfDay, endOfDay)
                .stream()
                .flatMap(sale -> sale.getItems().stream())
                .mapToDouble(item -> item.getQuantity() * (item.getProduct().getBuyingPrice() != null ? item.getProduct().getBuyingPrice() : 0))
                .sum();

        // Today's profit
        double todayProfit = todayRevenue - todayPurchaseCost;

        dashboard.put("todayRevenue", todayRevenue);
        dashboard.put("monthRevenue", monthRevenue);
        dashboard.put("todaySalesCount", todaySalesCount);
        dashboard.put("lowStockProducts", lowStockProducts);
        dashboard.put("lowStockCount", lowStockProducts.size());
        dashboard.put("totalProducts", totalProducts);
        dashboard.put("totalSuppliers", totalSuppliers);
        dashboard.put("monthPurchaseCost", monthPurchaseCost);
        dashboard.put("monthProfit", monthProfit);
        dashboard.put("todayProfit", todayProfit);

        return dashboard;
    }
}