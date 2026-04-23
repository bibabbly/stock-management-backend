package rw.stockmanagement.stock_management.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<?> getAllMovements(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "ALL") String type,
            @RequestParam(defaultValue = "") String search) {
        return ResponseEntity.ok(stockMovementService.getAllMovementsPaged(shopId, page, size, type, search));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<StockMovement>> getProductMovements(@PathVariable Long productId) {
        return ResponseEntity.ok(stockMovementService.getProductMovements(productId));
    }

    @GetMapping("/shop/{shopId}/type/{type}")
    public ResponseEntity<List<StockMovement>> getByType(@PathVariable Long shopId, @PathVariable String type) {
        return ResponseEntity.ok(stockMovementService.getMovementsByType(shopId, type));
    }

    @PostMapping("/restock")
    public ResponseEntity<StockMovement> restock(@RequestBody StockMovementDTO dto) {
        return ResponseEntity.ok(stockMovementService.restockFromSupplier(
                dto.getShopId(), dto.getProductId(), dto.getSupplierId(),
                dto.getQuantity(), dto.getNote(), dto.getUserId()));
    }
}