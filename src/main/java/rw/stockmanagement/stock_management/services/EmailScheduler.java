package rw.stockmanagement.stock_management.services;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailScheduler {

    private final DailyReportService dailyReportService;

    // Every day at 7:00 AM Rwanda time (UTC+2 = 05:00 UTC)
    @Scheduled(cron = "0 0 5 * * *", zone = "Africa/Kigali")
    public void sendDailyReport() {
        System.out.println("Sending daily report...");
        dailyReportService.sendDailyReport();
    }
}