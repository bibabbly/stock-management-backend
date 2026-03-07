package rw.stockmanagement.stock_management.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.stockmanagement.stock_management.dto.StockMovementDTO;
import rw.stockmanagement.stock_management.models.StockMovement;
import rw.stockmanagement.stock_management.services.StockMovementService;
import java.util.List;

@RestController
@RequestMapping("/api/stock-movements")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    // Get all movements for a shop
    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<StockMovement>> getAllMovements(@PathVariable Long shopId) {
        return ResponseEntity.ok(stockMovementService.getAllMovements(shopId));
    }

    // Get movements for a specific product
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<StockMovement>> getProductMovements(@PathVariable Long productId) {
        return ResponseEntity.ok(stockMovementService.getProductMovements(productId));
    }

    // Get movements by type IN or OUT
    @GetMapping("/shop/{shopId}/type/{type}")
    public ResponseEntity<List<StockMovement>> getByType(@PathVariable Long shopId, @PathVariable String type) {
        return ResponseEntity.ok(stockMovementService.getMovementsByType(shopId, type));
    }

    // Restock from supplier
    @PostMapping("/restock")
    public ResponseEntity<StockMovement> restock(@RequestBody StockMovementDTO dto) {
        return ResponseEntity.ok(stockMovementService.restockFromSupplier(
                dto.getShopId(), dto.getProductId(), dto.getSupplierId(),
                dto.getQuantity(), dto.getNote()));
    }
}