package rw.stockmanagement.stock_management.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.stockmanagement.stock_management.models.StockTransfer;
import rw.stockmanagement.stock_management.services.StockTransferService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@RestController
@RequestMapping("/api/stock-transfers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StockTransferController {

    private final StockTransferService stockTransferService;

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<Page<StockTransfer>> getTransfers(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDateTime start = (startDate != null && !startDate.isBlank())
                ? LocalDate.parse(startDate).atStartOfDay() : null;
        LocalDateTime end = (endDate != null && !endDate.isBlank())
                ? LocalDate.parse(endDate).atTime(LocalTime.MAX) : null;

        return ResponseEntity.ok(
                stockTransferService.getTransfers(shopId, page, size, search, start, end));
    }

    @PostMapping
    public ResponseEntity<?> transfer(@RequestBody Map<String, Object> body) {
        try {
            Long shopId = Long.valueOf(body.get("shopId").toString());
            Long productId = Long.valueOf(body.get("productId").toString());
            Long fromLocationId = Long.valueOf(body.get("fromLocationId").toString());
            Long toLocationId = Long.valueOf(body.get("toLocationId").toString());
            Integer quantity = Integer.valueOf(body.get("quantity").toString());
            String note = body.get("note") != null ? body.get("note").toString() : "";

            if (body.get("userId") == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "userId is required"));
            }
            Long userId = Long.valueOf(body.get("userId").toString());

            StockTransfer transfer = stockTransferService.transfer(
                    shopId, productId, fromLocationId, toLocationId, quantity, note, userId);
            return ResponseEntity.ok(transfer);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}