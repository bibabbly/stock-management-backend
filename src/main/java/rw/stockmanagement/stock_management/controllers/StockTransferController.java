package rw.stockmanagement.stock_management.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.stockmanagement.stock_management.models.StockTransfer;
import rw.stockmanagement.stock_management.services.StockTransferService;
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
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(stockTransferService.getTransfers(shopId, page, size));
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
            Long userId = body.get("userId") != null
                    ? Long.valueOf(body.get("userId").toString()) : null;

            StockTransfer transfer = stockTransferService.transfer(
                    shopId, productId, fromLocationId, toLocationId, quantity, note, userId);
            return ResponseEntity.ok(transfer);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}