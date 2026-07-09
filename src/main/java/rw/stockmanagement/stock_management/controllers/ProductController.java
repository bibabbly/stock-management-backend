package rw.stockmanagement.stock_management.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.stockmanagement.stock_management.dto.ProductDTO;
import rw.stockmanagement.stock_management.models.Product;
import rw.stockmanagement.stock_management.services.ProductService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<?> getAllProducts(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String search) {
        return ResponseEntity.ok(productService.getAllProducts(shopId, page, size, search));
    }

    // Only active products — for sale modal and restock modal
    @GetMapping("/shop/{shopId}/active")
    public ResponseEntity<List<Product>> getActiveProducts(@PathVariable Long shopId) {
        return ResponseEntity.ok(productService.getActiveProducts(shopId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody ProductDTO dto) {
        return ResponseEntity.ok(productService.createProduct(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id,
                                                 @RequestBody ProductDTO dto) {
        return ResponseEntity.ok(productService.updateProduct(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(Map.of("message", "Product deactivated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Deactivate product
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateProduct(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productService.deactivateProduct(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Reactivate product
    @PutMapping("/{id}/reactivate")
    public ResponseEntity<?> reactivateProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.reactivateProduct(id));
    }

    @GetMapping("/low-stock/{shopId}")
    public ResponseEntity<List<Product>> getLowStock(@PathVariable Long shopId) {
        return ResponseEntity.ok(productService.getLowStockProducts(shopId));
    }
}