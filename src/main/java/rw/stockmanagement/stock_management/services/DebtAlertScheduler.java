package rw.stockmanagement.stock_management.services;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import rw.stockmanagement.stock_management.models.Debt;
import rw.stockmanagement.stock_management.repositories.DebtRepository;
import rw.stockmanagement.stock_management.repositories.ShopRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DebtAlertScheduler {

    private final DebtRepository debtRepository;
    private final ShopRepository shopRepository;

    // Runs at 8AM Rwanda time (6AM UTC) every day
    @Scheduled(cron = "0 0 6 * * *", zone = "UTC")
    public void sendDebtAlerts() {
        String sendGridApiKey = System.getenv("SPRING_SENDGRID_KEY");
        String fromEmail = System.getenv("SPRING_SENDGRID_FROM") != null
                ? System.getenv("SPRING_SENDGRID_FROM") : "bizimungu2004@gmail.com";

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Debt> debtsDueTomorrow = debtRepository.findDebtsDueTomorrow(tomorrow);

        if (debtsDueTomorrow.isEmpty()) return;

        // Group debts by shop
        Map<Long, List<Debt>> debtsByShop = debtsDueTomorrow.stream()
                .collect(Collectors.groupingBy(d -> d.getShop().getId()));

        for (Map.Entry<Long, List<Debt>> entry : debtsByShop.entrySet()) {
            Long shopId = entry.getKey();
            List<Debt> shopDebts = entry.getValue();

            shopRepository.findById(shopId).ifPresent(shop -> {
                String ownerEmail = shop.getOwnerEmail();
                if (ownerEmail == null || ownerEmail.isEmpty()) return;

                String html = buildAlertHtml(shop.getName(), shopDebts, tomorrow.toString());

                try {
                    Email from = new Email(fromEmail, "BizTrack");
                    Email to = new Email(ownerEmail);
                    Content content = new Content("text/html", html);
                    Mail mail = new Mail(from,
                            "⚠️ BizTrack Debt Alert — " + shopDebts.size() + " debt(s) due tomorrow",
                            to, content);

                    SendGrid sg = new SendGrid(sendGridApiKey);
                    Request request = new Request();
                    request.setMethod(Method.POST);
                    request.setEndpoint("mail/send");
                    request.setBody(mail.build());
                    sg.api(request);

                    System.out.println("Debt alert sent to " + shop.getName() + " (" + ownerEmail + ")");
                } catch (IOException e) {
                    System.err.println("Failed to send debt alert: " + e.getMessage());
                }
            });
        }
    }

    private String buildAlertHtml(String shopName, List<Debt> debts, String dueDate) {
        StringBuilder rows = new StringBuilder();
        for (Debt d : debts) {
            String typeLabel = d.getDebtType() == Debt.DebtType.CUSTOMER ? "Customer owes you" : "You owe supplier";
            String typeColor = d.getDebtType() == Debt.DebtType.CUSTOMER ? "#16a34a" : "#ef4444";
            rows.append(String.format(
                    "<div style='padding:12px;border-bottom:1px solid #f1f5f9;'>" +
                            "<div style='display:flex;justify-content:space-between;margin-bottom:4px;'>" +
                            "<span style='font-weight:600;color:#0f172a;font-size:14px;'>%s</span>" +
                            "<span style='color:%s;font-size:12px;font-weight:600;'>%s</span>" +
                            "</div>" +
                            "<div style='display:flex;justify-content:space-between;'>" +
                            "<span style='color:#64748b;font-size:13px;'>%s</span>" +
                            "<span style='color:#ef4444;font-weight:700;font-size:14px;'>RWF %,.0f remaining</span>" +
                            "</div>" +
                            "</div>",
                    d.getName(), typeColor, typeLabel,
                    d.getNote() != null ? d.getNote() : "—",
                    d.getRemainingAmount()
            ));
        }

        return "<!DOCTYPE html><html><body style='margin:0;padding:0;background:#f8fafc;font-family:Arial,sans-serif;'>" +
                "<div style='max-width:600px;margin:20px auto;background:white;border-radius:16px;overflow:hidden;border:1px solid #e2e8f0;'>" +
                "<div style='background:linear-gradient(135deg,#ef4444,#f97316);padding:30px;text-align:center;'>" +
                "<h1 style='color:white;margin:0;font-size:22px;'>⚠️ Debt Due Tomorrow</h1>" +
                "<p style='color:rgba(255,255,255,0.85);margin:4px 0 0;font-size:15px;font-weight:600;'>" + shopName + "</p>" +
                "<p style='color:rgba(255,255,255,0.7);margin:4px 0 0;font-size:13px;'>Due Date: " + dueDate + "</p>" +
                "</div>" +
                "<div style='padding:24px;'>" +
                "<p style='color:#64748b;font-size:14px;margin:0 0 16px;'>The following debts are due tomorrow. Please take action:</p>" +
                rows +
                "</div>" +
                "<div style='background:#f8fafc;padding:16px;text-align:center;border-top:1px solid #e2e8f0;'>" +
                "<p style='color:#94a3b8;font-size:12px;margin:0;'>BizTrack by INNOTEWO INC LTD · Kigali, Rwanda</p>" +
                "</div>" +
                "</div></body></html>";
    }
}