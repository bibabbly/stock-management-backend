package rw.stockmanagement.stock_management.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.stockmanagement.stock_management.dto.SaleDTO;
import rw.stockmanagement.stock_management.models.Sale;
import rw.stockmanagement.stock_management.services.SaleService;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SaleController {

    private final SaleService saleService;

    // Get all sales for a shop
    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<Sale>> getAllSales(@PathVariable Long shopId) {
        return ResponseEntity.ok(saleService.getAllSales(shopId));
    }

    // Get single sale
    @GetMapping("/{id}")
    public ResponseEntity<Sale> getSale(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.getSale(id));
    }

    // Create a sale
    @PostMapping
    public ResponseEntity<Sale> createSale(@RequestBody SaleDTO dto) {
        return ResponseEntity.ok(saleService.createSale(dto));
    }

    // Get today's total sales amount
    @GetMapping("/today/{shopId}")
    public ResponseEntity<Double> getTodayTotal(@PathVariable Long shopId) {
        return ResponseEntity.ok(saleService.getTodayTotal(shopId));
    }
}
