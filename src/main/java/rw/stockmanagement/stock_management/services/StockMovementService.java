package rw.stockmanagement.stock_management.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final UserRepository userRepository;
    private final ProductStockRepository productStockRepository;
    private final StockLocationRepository stockLocationRepository;

    public List<StockMovement> getAllMovements(Long shopId) {
        return stockMovementRepository.findByShopId(shopId);
    }

    public Page<StockMovement> getAllMovementsPaged(Long shopId, int page, int size,
                                                    String type, String search) {
        Pageable pageable = PageRequest.of(page, size);
        boolean hasSearch = search != null && !search.isEmpty();
        boolean hasType = type != null && !type.equals("ALL");

        if (hasType && hasSearch) {
            return stockMovementRepository
                    .findByShopIdAndTypeAndProductNameContainingIgnoreCaseOrderByCreatedAtDesc(
                            shopId, StockMovement.MovementType.valueOf(type), search, pageable);
        } else if (hasType) {
            return stockMovementRepository
                    .findByShopIdAndTypeOrderByCreatedAtDesc(
                            shopId, StockMovement.MovementType.valueOf(type), pageable);
        } else if (hasSearch) {
            return stockMovementRepository
                    .findByShopIdAndProductNameContainingIgnoreCaseOrderByCreatedAtDesc(
                            shopId, search, pageable);
        } else {
            return stockMovementRepository.findByShopIdOrderByCreatedAtDesc(shopId, pageable);
        }
    }

    public List<StockMovement> getProductMovements(Long productId) {
        return stockMovementRepository.findByProductId(productId);
    }

    public List<StockMovement> getMovementsByType(Long shopId, String type) {
        return stockMovementRepository.findByShopIdAndType(
                shopId, StockMovement.MovementType.valueOf(type.toUpperCase()));
    }

    @Transactional
    public StockMovement restockFromSupplier(Long shopId, Long productId, Long supplierId,
                                             Integer quantity, String note, Long userId) {
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than zero");
        }

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Get main location for this shop
        StockLocation mainLocation = stockLocationRepository
                .findByShopIdAndIsMainTrue(shopId)
                .orElse(null);

        if (mainLocation != null) {
            // Update product_stock for main location
            ProductStock productStock = productStockRepository
                    .findByProductIdAndLocationId(productId, mainLocation.getId())
                    .orElseGet(() -> {
                        ProductStock ps = new ProductStock();
                        ps.setProduct(product);
                        ps.setLocation(mainLocation);
                        ps.setQuantity(0);
                        return ps;
                    });
            productStock.setQuantity(productStock.getQuantity() + quantity);
            productStockRepository.save(productStock);

            // Sync products.quantity with total across all locations
            Integer total = productStockRepository.getTotalQuantityByProductId(productId);
            product.setQuantity(total != null ? total : 0);
        } else {
            // Fallback — no location found, update products.quantity directly
            product.setQuantity(product.getQuantity() + quantity);
        }

        productRepository.save(product);

        // Record stock movement
        StockMovement movement = new StockMovement();
        movement.setShop(shop);
        movement.setProduct(product);
        movement.setType(StockMovement.MovementType.IN);
        movement.setQuantity(quantity);
        movement.setNote(note != null && !note.isEmpty() ? note : "Direct Restock");

        if (userId != null) {
            userRepository.findById(userId).ifPresent(movement::setUser);
        }

        return stockMovementRepository.save(movement);
    }

    @Transactional
    public StockMovement manualStockOut(Long shopId, Long productId, Integer quantity,
                                        String reason, Long userId) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("Reason is mandatory for manual stock out");
        }

        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than zero");
        }

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check total stock across all locations
        Integer totalStock = productStockRepository.getTotalQuantityByProductId(productId);
        int currentTotal = totalStock != null ? totalStock : product.getQuantity();

        if (currentTotal < quantity) {
            throw new RuntimeException(
                    "Insufficient stock. Available: " + currentTotal);
        }

        // Deduct from main location first
        StockLocation mainLocation = stockLocationRepository
                .findByShopIdAndIsMainTrue(shopId)
                .orElse(null);

        if (mainLocation != null) {
            ProductStock productStock = productStockRepository
                    .findByProductIdAndLocationId(productId, mainLocation.getId())
                    .orElse(null);

            if (productStock != null) {
                int newQty = productStock.getQuantity() - quantity;
                if (newQty < 0) newQty = 0;
                productStock.setQuantity(newQty);
                productStockRepository.save(productStock);
            }

            // Sync products.quantity
            Integer total = productStockRepository.getTotalQuantityByProductId(productId);
            product.setQuantity(total != null ? total : 0);
        } else {
            // Fallback
            product.setQuantity(Math.max(0, product.getQuantity() - quantity));
        }

        // Auto deactivate if total stock reaches 0
        if (product.getQuantity() == 0) {
            product.setActive(false);
        }

        productRepository.save(product);

        // Record stock movement
        StockMovement movement = new StockMovement();
        movement.setShop(shop);
        movement.setProduct(product);
        movement.setType(StockMovement.MovementType.OUT);
        movement.setQuantity(quantity);
        movement.setNote(reason);

        if (userId != null) {
            userRepository.findById(userId).ifPresent(movement::setUser);
        }

        return stockMovementRepository.save(movement);
    }
}