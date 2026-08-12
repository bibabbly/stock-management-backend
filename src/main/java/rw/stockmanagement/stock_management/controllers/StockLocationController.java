package rw.stockmanagement.stock_management.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.stockmanagement.stock_management.models.*;
import rw.stockmanagement.stock_management.services.StockLocationService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock-locations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StockLocationController {

    private final StockLocationService stockLocationService;

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<StockLocation>> getLocations(@PathVariable Long shopId) {
        return ResponseEntity.ok(stockLocationService.getLocationsByShop(shopId));
    }

    @PostMapping("/shop/{shopId}")
    public ResponseEntity<?> createLocation(
            @PathVariable Long shopId,
            @RequestBody Map<String, Object> body) {
        try {
            String name = body.get("name").toString();
            Boolean isMain = body.get("isMain") != null
                    ? Boolean.valueOf(body.get("isMain").toString()) : false;
            return ResponseEntity.ok(stockLocationService.createLocation(shopId, name, isMain));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{locationId}")
    public ResponseEntity<?> updateLocation(
            @PathVariable Long locationId,
            @RequestBody Map<String, Object> body) {
        try {
            String name = body.get("name").toString();
            Boolean isMain = body.get("isMain") != null
                    ? Boolean.valueOf(body.get("isMain").toString()) : null;
            return ResponseEntity.ok(stockLocationService.updateLocation(locationId, name, isMain));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{locationId}")
    public ResponseEntity<?> deleteLocation(@PathVariable Long locationId) {
        try {
            stockLocationService.deleteLocation(locationId);
            return ResponseEntity.ok(Map.of("message", "Location deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{locationId}/stock")
    public ResponseEntity<List<ProductStock>> getStockByLocation(@PathVariable Long locationId) {
        return ResponseEntity.ok(stockLocationService.getStockByLocation(locationId));
    }

    @GetMapping("/shop/{shopId}/stock")
    public ResponseEntity<List<ProductStock>> getStockByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(stockLocationService.getStockByShop(shopId));
    }
}