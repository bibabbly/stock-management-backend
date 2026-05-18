package rw.stockmanagement.stock_management.services;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rw.stockmanagement.stock_management.models.Product;
import rw.stockmanagement.stock_management.models.Sale;
import rw.stockmanagement.stock_management.models.Shop;
import rw.stockmanagement.stock_management.repositories.*;
import java.io.IOException;
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
    private final ShopRepository shopRepository;

    public void sendDailyReport() {
        String sendGridApiKey = System.getenv("SPRING_SENDGRID_KEY");
        String fromEmail = System.getenv("SPRING_SENDGRID_FROM") != null ? System.getenv("SPRING_SENDGRID_FROM") : "noreply@innotewo.com";
        String ccEmail = System.getenv("SPRING_REPORT_TO") != null ? System.getenv("SPRING_REPORT_TO") : "bizimungu2004@gmail.com";

        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime start = yesterday.atStartOfDay();
        LocalDateTime end = yesterday.atTime(23, 59, 59);
        String dateStr = yesterday.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"));

        List<Shop> activeShops = shopRepository.findAll().stream()
                .filter(Shop::isActive)
                .collect(Collectors.toList());

        for (Shop shop : activeShops) {
            sendShopReport(shop, sendGridApiKey, fromEmail, ccEmail, start, end, dateStr);
        }
    }

    public void sendShopReport(Shop shop, String sendGridApiKey, String fromEmail, String ccEmail,
                               LocalDateTime start, LocalDateTime end, String dateStr) {
        Long shopId = shop.getId();

        List<Sale> sales = saleRepository.findByShopIdAndDateBetweenWithItems(shopId, start, end);
        double revenue = sales.stream().mapToDouble(Sale::getTotalAmount).sum();
        double cost = sales.stream()
                .flatMap(s -> s.getItems().stream())
                .mapToDouble(i -> i.getQuantity() * (i.getProduct().getBuyingPrice() != null ? i.getProduct().getBuyingPrice() : 0))
                .sum();
        double profit = revenue - cost;

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

        List<Product> lowStock = productRepository.findByShopIdAndQuantityLessThanEqualMinStock(shopId);

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

        String html = buildHtml(shop.getName(), dateStr, sales.size(), revenue, profit, topProducts, lowStock, stockIn, stockOut);

        String recipientEmail = shop.getOwnerEmail();
        if (recipientEmail == null || recipientEmail.isEmpty()) {
            System.out.println("Skipping shop " + shop.getName() + " — no owner email set");
            return;
        }

        try {
            Email from = new Email(fromEmail, "BizTrack");
            Email to = new Email(recipientEmail);
            Content content = new Content("text/html", html);
            Mail mail = new Mail(from, "📊 BizTrack Daily Report — " + shop.getName() + " — " + dateStr, to, content);

            Personalization personalization = mail.getPersonalization().get(0);
            personalization.addCc(new Email(ccEmail));

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println("SendGrid status: " + response.getStatusCode());
            System.out.println("SendGrid body: " + response.getBody());
            System.out.println("SendGrid headers: " + response.getHeaders());

            System.out.println("Report sent to " + shop.getName() + " (" + recipientEmail + ") — Status: " + response.getStatusCode());
        } catch (IOException e) {
            System.err.println("Failed to send report for " + shop.getName() + ": " + e.getMessage());
        }
    }

    public void sendDailyReportForShop(Long shopId) {
        String sendGridApiKey = System.getenv("SPRING_SENDGRID_KEY");
        String fromEmail = System.getenv("SPRING_SENDGRID_FROM") != null ? System.getenv("SPRING_SENDGRID_FROM") : "noreply@innotewo.com";
        String ccEmail = System.getenv("SPRING_REPORT_TO") != null ? System.getenv("SPRING_REPORT_TO") : "bizimungu2004@gmail.com";

        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime start = yesterday.atStartOfDay();
        LocalDateTime end = yesterday.atTime(23, 59, 59);
        String dateStr = yesterday.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"));

        shopRepository.findById(shopId).ifPresent(shop ->
                sendShopReport(shop, sendGridApiKey, fromEmail, ccEmail, start, end, dateStr));
    }

    private String buildHtml(String shopName, String date, int salesCount, double revenue, double profit,
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
                "<div style='background:linear-gradient(135deg,#3b82f6,#06b6d4);padding:30px;text-align:center;'>" +
                "<h1 style='color:white;margin:0;font-size:22px;'>📊 BizTrack Daily Report</h1>" +
                "<p style='color:rgba(255,255,255,0.85);margin:4px 0 0;font-size:15px;font-weight:600;'>" + shopName + "</p>" +
                "<p style='color:rgba(255,255,255,0.7);margin:4px 0 0;font-size:13px;'>" + date + "</p>" +
                "</div>" +
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
                "<div style='margin-bottom:24px;'>" +
                "<h3 style='color:#0f172a;font-size:15px;margin:0 0 12px;'>🏆 Top Selling Products</h3>" +
                topProductsHtml +
                "</div>" +
                "<div style='margin-bottom:24px;'>" +
                "<h3 style='color:#0f172a;font-size:15px;margin:0 0 12px;'>⚠️ Low Stock Alerts</h3>" +
                lowStockHtml +
                "</div>" +
                "</div>" +
                "<div style='background:#f8fafc;padding:16px;text-align:center;border-top:1px solid #e2e8f0;'>" +
                "<p style='color:#94a3b8;font-size:12px;margin:0;'>BizTrack by INNOTEWO INC LTD · Kigali, Rwanda</p>" +
                "</div>" +
                "</div></body></html>";
    }
}