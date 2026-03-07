package rw.stockmanagement.stock_management.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.stockmanagement.stock_management.dto.ProductDTO;
import rw.stockmanagement.stock_management.models.Product;
import rw.stockmanagement.stock_management.services.ProductService;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    // Get all products for a shop
    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<Product>> getAllProducts(@PathVariable Long shopId) {
        return ResponseEntity.ok(productService.getAllProducts(shopId));
    }

    // Get single product
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    // Create product
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody ProductDTO dto) {
        return ResponseEntity.ok(productService.createProduct(dto));
    }

    // Update product
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id,
                                                 @RequestBody ProductDTO dto) {
        return ResponseEntity.ok(productService.updateProduct(id, dto));
    }

    // Delete product
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("Product deleted successfully");
    }

    // Get low stock products
    @GetMapping("/low-stock/{shopId}")
    public ResponseEntity<List<Product>> getLowStock(@PathVariable Long shopId) {
        return ResponseEntity.ok(productService.getLowStockProducts(shopId));
    }
}