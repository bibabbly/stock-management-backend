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

    public List<StockMovement> getAllMovements(Long shopId) {
        return stockMovementRepository.findByShopId(shopId);
    }

    public Page<StockMovement> getAllMovementsPaged(Long shopId, int page, int size, String type, String search) {
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
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        supplierRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        product.setQuantity(product.getQuantity() + quantity);
        productRepository.save(product);

        StockMovement movement = new StockMovement();
        movement.setShop(shop);
        movement.setProduct(product);
        movement.setType(StockMovement.MovementType.IN);
        movement.setQuantity(quantity);
        movement.setNote(note != null ? note : "Restock from supplier");

        if (userId != null) {
            userRepository.findById(userId).ifPresent(movement::setUser);
        }

        return stockMovementRepository.save(movement);
    }
}