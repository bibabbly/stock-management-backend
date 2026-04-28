package rw.stockmanagement.stock_management.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.stockmanagement.stock_management.services.DailyReportService;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DailyReportController {

    private final DailyReportService dailyReportService;

    @PostMapping("/send-daily")
    public ResponseEntity<String> sendNow() {
        dailyReportService.sendDailyReport();
        return ResponseEntity.ok("Daily report sent!");
    }
}