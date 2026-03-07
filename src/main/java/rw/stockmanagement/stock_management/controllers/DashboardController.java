package rw.stockmanagement.stock_management.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.stockmanagement.stock_management.services.DashboardService;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<Map<String, Object>> getDashboard(@PathVariable Long shopId) {
        return ResponseEntity.ok(dashboardService.getDashboard(shopId));
    }
}