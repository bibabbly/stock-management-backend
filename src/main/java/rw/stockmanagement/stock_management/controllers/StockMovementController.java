package rw.stockmanagement.stock_management.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.stockmanagement.stock_management.models.StockMovement;
import rw.stockmanagement.stock_management.services.StockMovementService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock-movements")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<?> getAllMovements(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "ALL") String type,
            @RequestParam(defaultValue = "") String search) {
        return ResponseEntity.ok(stockMovementService.getAllMovementsPaged(
                shopId, page, size, type, search));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<StockMovement>> getProductMovements(
            @PathVariable Long productId) {
        return ResponseEntity.ok(stockMovementService.getProductMovements(productId));
    }

    @GetMapping("/shop/{shopId}/type/{type}")
    public ResponseEntity<List<StockMovement>> getByType(
            @PathVariable Long shopId, @PathVariable String type) {
        return ResponseEntity.ok(stockMovementService.getMovementsByType(shopId, type));
    }

    @PostMapping("/restock")
    public ResponseEntity<?> restock(@RequestBody Map<String, Object> body) {
        try {
            Long shopId = Long.valueOf(body.get("shopId").toString());
            Long productId = Long.valueOf(body.get("productId").toString());
            Long supplierId = body.get("supplierId") != null
                    ? Long.valueOf(body.get("supplierId").toString()) : null;
            Integer quantity = Integer.valueOf(body.get("quantity").toString());
            String note = body.get("note") != null ? body.get("note").toString() : "";
            Long userId = body.get("userId") != null
                    ? Long.valueOf(body.get("userId").toString()) : null;
            Long locationId = body.get("locationId") != null
                    ? Long.valueOf(body.get("locationId").toString()) : null;

            StockMovement movement = stockMovementService.restockFromSupplier(
                    shopId, productId, supplierId, quantity, note, userId, locationId);
            return ResponseEntity.ok(movement);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/manual-out")
    public ResponseEntity<?> manualStockOut(@RequestBody Map<String, Object> body) {
        try {
            Long shopId = Long.valueOf(body.get("shopId").toString());
            Long productId = Long.valueOf(body.get("productId").toString());
            Integer quantity = Integer.valueOf(body.get("quantity").toString());
            String reason = body.get("reason").toString();
            Long userId = body.get("userId") != null
                    ? Long.valueOf(body.get("userId").toString()) : null;
            Long locationId = body.get("locationId") != null
                    ? Long.valueOf(body.get("locationId").toString()) : null;

            StockMovement movement = stockMovementService.manualStockOut(
                    shopId, productId, quantity, reason, userId, locationId);
            return ResponseEntity.ok(movement);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}