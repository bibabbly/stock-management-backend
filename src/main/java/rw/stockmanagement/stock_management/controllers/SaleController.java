package rw.stockmanagement.stock_management.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.stockmanagement.stock_management.dto.SaleDTO;
import rw.stockmanagement.stock_management.models.Sale;
import rw.stockmanagement.stock_management.services.SaleService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SaleController {

    private final SaleService saleService;

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<?> getAllSales(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(saleService.getAllSalesPaged(shopId, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sale> getSale(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.getSale(id));
    }

    @PostMapping
    public ResponseEntity<Sale> createSale(@RequestBody SaleDTO dto) {
        return ResponseEntity.ok(saleService.createSale(dto));
    }

    @GetMapping("/today/{shopId}")
    public ResponseEntity<Double> getTodayTotal(@PathVariable Long shopId) {
        return ResponseEntity.ok(saleService.getTodayTotal(shopId));
    }
    @GetMapping("/shop/{shopId}/all")
    public ResponseEntity<List<Sale>> getAllSalesUnpaged(@PathVariable Long shopId) {
        return ResponseEntity.ok(saleService.getAllSales(shopId));
    }

    @GetMapping("/shop/{shopId}/report")
    public ResponseEntity<List<Sale>> getSalesReport(
            @PathVariable Long shopId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
        return ResponseEntity.ok(saleService.getSalesByDateRange(shopId, start, end));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelSale(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            Long cancelledByUserId = Long.valueOf(body.get("userId").toString());
            String reason = body.getOrDefault("reason", "No reason provided").toString();
            Sale cancelled = saleService.cancelSale(id, cancelledByUserId, reason);
            return ResponseEntity.ok(cancelled);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/shop/{shopId}/cancelled")
    public ResponseEntity<?> getCancelledSales(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(saleService.getCancelledSalesPaged(shopId, page, size));
    }
}