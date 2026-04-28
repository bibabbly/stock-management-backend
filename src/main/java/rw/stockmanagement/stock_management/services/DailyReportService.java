package rw.stockmanagement.stock_management.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import rw.stockmanagement.stock_management.models.Product;
import rw.stockmanagement.stock_management.models.Sale;
import rw.stockmanagement.stock_management.repositories.*;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DailyReportService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final JavaMailSender mailSender;

    @Value("${app.report.recipient}")
    private String recipient;

    @Value("${app.report.shop-id}")
    private Long shopId;

    public void sendDailyReport() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime start = yesterday.atStartOfDay();
        LocalDateTime end = yesterday.atTime(23, 59, 59);
        String dateStr = yesterday.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"));

        // Yesterday's sales
        List<Sale> sales = saleRepository.findByShopIdAndDateBetween(shopId, start, end);
        double revenue = sales.stream().mapToDouble(Sale::getTotalAmount).sum();
        double cost = sales.stream()
                .flatMap(s -> s.getItems().stream())
                .mapToDouble(i -> i.getQuantity() * (i.getProduct().getBuyingPrice() != null ? i.getProduct().getBuyingPrice() : 0))
                .sum();
        double profit = revenue - cost;

        // Top 5 selling products
        Map<String, Integer> productSales = new LinkedHashMap<>();
        sales.stream()
                .flatMap(s -> s.getItems().stream())
                .forEach(item -> productSales.merge(
                        item.getProduct().getName(),
                        item.getQuantity(),
                        Integer::sum
                ));
        List<Map.Entry<String, Integer>> topProducts = productSales.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());

        // Low stock products
        List<Product> lowStock = productRepository.findByShopIdAndQuantityLessThanEqualMinStock(shopId);

        // Stock movements yesterday
        long stockIn = stockMovementRepository.findByShopId(shopId).stream()
                .filter(m -> !m.getCreatedAt().isBefore(start) && !m.getCreatedAt().isAfter(end))
                .filter(m -> m.getType().name().equals("IN"))
                .mapToLong(m -> m.getQuantity())
                .sum();

        long stockOut = stockMovementRepository.findByShopId(shopId).stream()
                .filter(m -> !m.getCreatedAt().isBefore(start) && !m.getCreatedAt().isAfter(end))
                .filter(m -> m.getType().name().equals("OUT"))
                .mapToLong(m -> m.getQuantity())
                .sum();

        // Build email HTML
        String html = buildHtml(dateStr, sales.size(), revenue, profit, topProducts, lowStock, stockIn, stockOut);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(recipient);
            helper.setSubject("📊 BizTrack Daily Report — " + dateStr);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send daily report: " + e.getMessage());
        }
    }

    private String buildHtml(String date, int salesCount, double revenue, double profit,
                             List<Map.Entry<String, Integer>> topProducts,
                             List<Product> lowStock, long stockIn, long stockOut) {

        StringBuilder topProductsHtml = new StringBuilder();
        if (topProducts.isEmpty()) {
            topProductsHtml.append("<p style='color:#94a3b8;font-size:13px;'>No sales yesterday.</p>");
        } else {
            for (int i = 0; i < topProducts.size(); i++) {
                Map.Entry<String, Integer> entry = topProducts.get(i);
                topProductsHtml.append(String.format(
                        "<div style='display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px solid #f1f5f9;'>" +
                                "<span style='color:#0f172a;font-size:13px;'>%d. %s</span>" +
                                "<span style='color:#3b82f6;font-weight:600;font-size:13px;'>%d units</span>" +
                                "</div>", i + 1, entry.getKey(), entry.getValue()
                ));
            }
        }

        StringBuilder lowStockHtml = new StringBuilder();
        if (lowStock.isEmpty()) {
            lowStockHtml.append("<p style='color:#16a34a;font-size:13px;'>✅ All products are well stocked.</p>");
        } else {
            for (Product p : lowStock) {
                lowStockHtml.append(String.format(
                        "<div style='display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px solid #fef2f2;'>" +
                                "<span style='color:#0f172a;font-size:13px;'>%s</span>" +
                                "<span style='color:#ef4444;font-weight:600;font-size:13px;'>%d left (min: %d)</span>" +
                                "</div>", p.getName(), p.getQuantity(), p.getMinStock()
                ));
            }
        }

        return "<!DOCTYPE html><html><body style='margin:0;padding:0;background:#f8fafc;font-family:Arial,sans-serif;'>" +
                "<div style='max-width:600px;margin:20px auto;background:white;border-radius:16px;overflow:hidden;border:1px solid #e2e8f0;'>" +

                // Header
                "<div style='background:linear-gradient(135deg,#3b82f6,#06b6d4);padding:30px;text-align:center;'>" +
                "<h1 style='color:white;margin:0;font-size:22px;'>📊 BizTrack Daily Report</h1>" +
                "<p style='color:rgba(255,255,255,0.85);margin:6px 0 0;font-size:14px;'>" + date + "</p>" +
                "</div>" +

                // Summary cards
                "<div style='padding:24px;'>" +
                "<div style='display:grid;grid-template-columns:1fr 1fr 1fr;gap:12px;margin-bottom:24px;'>" +

                "<div style='background:#f0fdf4;border-radius:12px;padding:16px;text-align:center;'>" +
                "<p style='color:#94a3b8;font-size:11px;margin:0 0 4px;text-transform:uppercase;'>Sales</p>" +
                "<p style='color:#16a34a;font-size:24px;font-weight:700;margin:0;'>" + salesCount + "</p>" +
                "</div>" +

                "<div style='background:#eff6ff;border-radius:12px;padding:16px;text-align:center;'>" +
                "<p style='color:#94a3b8;font-size:11px;margin:0 0 4px;text-transform:uppercase;'>Revenue</p>" +
                "<p style='color:#3b82f6;font-size:18px;font-weight:700;margin:0;'>RWF " + String.format("%,.0f", revenue) + "</p>" +
                "</div>" +

                "<div style='background:" + (profit >= 0 ? "#f0fdf4" : "#fef2f2") + ";border-radius:12px;padding:16px;text-align:center;'>" +
                "<p style='color:#94a3b8;font-size:11px;margin:0 0 4px;text-transform:uppercase;'>Profit</p>" +
                "<p style='color:" + (profit >= 0 ? "#16a34a" : "#ef4444") + ";font-size:18px;font-weight:700;margin:0;'>RWF " + String.format("%,.0f", profit) + "</p>" +
                "</div>" +

                "</div>" +

                // Stock movements
                "<div style='background:#f8fafc;border-radius:12px;padding:16px;margin-bottom:24px;display:flex;justify-content:space-around;'>" +
                "<div style='text-align:center;'>" +
                "<p style='color:#94a3b8;font-size:11px;margin:0 0 4px;text-transform:uppercase;'>Stock IN</p>" +
                "<p style='color:#16a34a;font-size:20px;font-weight:700;margin:0;'>+" + stockIn + "</p>" +
                "</div>" +
                "<div style='text-align:center;'>" +
                "<p style='color:#94a3b8;font-size:11px;margin:0 0 4px;text-transform:uppercase;'>Stock OUT</p>" +
                "<p style='color:#ef4444;font-size:20px;font-weight:700;margin:0;'>-" + stockOut + "</p>" +
                "</div>" +
                "</div>" +

                // Top products
                "<div style='margin-bottom:24px;'>" +
                "<h3 style='color:#0f172a;font-size:15px;margin:0 0 12px;'>🏆 Top Selling Products</h3>" +
                topProductsHtml +
                "</div>" +

                // Low stock
                "<div style='margin-bottom:24px;'>" +
                "<h3 style='color:#0f172a;font-size:15px;margin:0 0 12px;'>⚠️ Low Stock Alerts</h3>" +
                lowStockHtml +
                "</div>" +

                "</div>" +

                // Footer
                "<div style='background:#f8fafc;padding:16px;text-align:center;border-top:1px solid #e2e8f0;'>" +
                "<p style='color:#94a3b8;font-size:12px;margin:0;'>BizTrack by INNOTEWO INC LTD · Kigali, Rwanda</p>" +
                "</div>" +

                "</div></body></html>";
    }
}