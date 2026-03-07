package rw.stockmanagement.stock_management.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.stockmanagement.stock_management.models.Shop;
import rw.stockmanagement.stock_management.repositories.ShopRepository;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ShopController {

    private final ShopRepository shopRepository;

    // Get shop details
    @GetMapping("/{id}")
    public ResponseEntity<Shop> getShop(@PathVariable Long id) {
        return shopRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update shop details
    @PutMapping("/{id}")
    public ResponseEntity<Shop> updateShop(@PathVariable Long id, @RequestBody Shop updated) {
        return shopRepository.findById(id).map(shop -> {
            shop.setName(updated.getName());
            shop.setAddress(updated.getAddress());
            shop.setPhone(updated.getPhone());
            shop.setEmail(updated.getEmail());
            shop.setTinNumber(updated.getTinNumber());
            shop.setWebsite(updated.getWebsite());
            shop.setReceiptFooter(updated.getReceiptFooter());
            shop.setLogo(updated.getLogo());
            return ResponseEntity.ok(shopRepository.save(shop));
        }).orElse(ResponseEntity.notFound().build());
    }
}