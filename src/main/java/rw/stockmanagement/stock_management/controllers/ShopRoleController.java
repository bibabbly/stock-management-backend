package rw.stockmanagement.stock_management.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.stockmanagement.stock_management.models.Shop;
import rw.stockmanagement.stock_management.models.ShopRole;
import rw.stockmanagement.stock_management.repositories.ShopRoleRepository;
import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ShopRoleController {

    private final ShopRoleRepository shopRoleRepository;

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<ShopRole>> getRoles(@PathVariable Long shopId) {
        return ResponseEntity.ok(shopRoleRepository.findByShopId(shopId));
    }

    @PostMapping
    public ResponseEntity<ShopRole> createRole(@RequestBody ShopRole role) {
        return ResponseEntity.ok(shopRoleRepository.save(role));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShopRole> updateRole(@PathVariable Long id, @RequestBody ShopRole updated) {
        return shopRoleRepository.findById(id).map(role -> {
            role.setName(updated.getName());
            role.setPermissions(updated.getPermissions());
            return ResponseEntity.ok(shopRoleRepository.save(role));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        shopRoleRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}