package rw.stockmanagement.stock_management.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.stockmanagement.stock_management.dto.DebtDTO;
import rw.stockmanagement.stock_management.models.Debt;
import rw.stockmanagement.stock_management.services.DebtService;

import java.util.Map;

@RestController
@RequestMapping("/api/debts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DebtController {

    private final DebtService debtService;

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<Page<Debt>> getDebts(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "ALL") String type,
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(debtService.getDebts(shopId, type, status, page, size));
    }

    @GetMapping("/shop/{shopId}/summary")
    public ResponseEntity<Map<String, Double>> getSummary(@PathVariable Long shopId) {
        return ResponseEntity.ok(debtService.getSummary(shopId));
    }

    @PostMapping
    public ResponseEntity<Debt> createDebt(@RequestBody DebtDTO dto) {
        return ResponseEntity.ok(debtService.createDebt(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Debt> updateDebt(@PathVariable Long id, @RequestBody DebtDTO dto) {
        return ResponseEntity.ok(debtService.updateDebt(id, dto));
    }

    @PostMapping("/{id}/payment")
    public ResponseEntity<Debt> recordPayment(
            @PathVariable Long id,
            @RequestBody Map<String, Double> body) {
        return ResponseEntity.ok(debtService.recordPayment(id, body.get("amount")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDebt(@PathVariable Long id) {
        debtService.deleteDebt(id);
        return ResponseEntity.ok().build();
    }
}