package rw.stockmanagement.stock_management.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.stockmanagement.stock_management.models.*;
import rw.stockmanagement.stock_management.repositories.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository; // ← ADD THIS

    // Get all movements for a shop
    public List<StockMovement> getAllMovements(Long shopId) {
        return stockMovementRepository.findByShopId(shopId);
    }

    // Get movements for a specific product
    public List<StockMovement> getProductMovements(Long productId) {
        return stockMovementRepository.findByProductId(productId);
    }

    // Get movements by type (IN or OUT)
    public List<StockMovement> getMovementsByType(Long shopId, String type) {
        return stockMovementRepository.findByShopIdAndType(
                shopId, StockMovement.MovementType.valueOf(type.toUpperCase()));
    }

    // Manually add stock IN (restock from supplier)
    @Transactional
    public StockMovement restockFromSupplier(Long shopId, Long productId, Long supplierId,
                                             Integer quantity, String note, Long userId) { // ← ADD userId
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        supplierRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        // Increase stock
        product.setQuantity(product.getQuantity() + quantity);
        productRepository.save(product);

        // Record movement
        StockMovement movement = new StockMovement();
        movement.setShop(shop);
        movement.setProduct(product);
        movement.setType(StockMovement.MovementType.IN);
        movement.setQuantity(quantity);
        movement.setNote(note != null ? note : "Restock from supplier");

        // ← ADD THIS: save the user who recorded the restock
        if (userId != null) {
            userRepository.findById(userId).ifPresent(movement::setUser);
        }

        return stockMovementRepository.save(movement);
    }
}