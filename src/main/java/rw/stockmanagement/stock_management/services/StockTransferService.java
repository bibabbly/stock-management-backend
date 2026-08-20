package rw.stockmanagement.stock_management.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.stockmanagement.stock_management.models.*;
import rw.stockmanagement.stock_management.repositories.*;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockTransferService {

    private final StockTransferRepository stockTransferRepository;
    private final ProductStockRepository productStockRepository;
    private final StockLocationRepository stockLocationRepository;
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;

    @Transactional
    public StockTransfer transfer(Long shopId, Long productId, Long fromLocationId,
                                  Long toLocationId, Integer quantity, String note, Long userId) {

        if (fromLocationId.equals(toLocationId)) {
            throw new RuntimeException("Source and destination locations cannot be the same");
        }

        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than zero");
        }

        if (userId == null) {
            throw new RuntimeException("User is required to perform a stock transfer");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        StockLocation fromLocationEntity = stockLocationRepository.findById(fromLocationId)
                .orElseThrow(() -> new RuntimeException("Source location not found"));
        StockLocation toLocationEntity = stockLocationRepository.findById(toLocationId)
                .orElseThrow(() -> new RuntimeException("Destination location not found"));

        // Direction rule:
        // ADMIN can transfer between any locations (full access).
        // Non-admin (STOCK_TRANSFER permission) can only move Warehouse -> Main.
        boolean isAdmin = user.getRole() == User.Role.ADMIN;

        if (!isAdmin) {
            if (Boolean.TRUE.equals(fromLocationEntity.getIsMain()) || !Boolean.TRUE.equals(toLocationEntity.getIsMain())) {
                throw new RuntimeException("You can only transfer stock from Warehouse to Main");
            }
        }

        // Get source stock — check availability
        ProductStock fromStock = productStockRepository
                .findByProductIdAndLocationId(productId, fromLocationId)
                .orElseThrow(() -> new RuntimeException("Product not found in source location"));

        if (fromStock.getQuantity() < quantity) {
            throw new RuntimeException(
                    "Insufficient stock in source location. Available: " + fromStock.getQuantity());
        }

        // Get or create destination stock
        ProductStock toStock = productStockRepository
                .findByProductIdAndLocationId(productId, toLocationId)
                .orElseGet(() -> {
                    ProductStock ps = new ProductStock();
                    ps.setProduct(productRepository.findById(productId)
                            .orElseThrow(() -> new RuntimeException("Product not found")));
                    ps.setLocation(toLocationEntity);
                    ps.setQuantity(0);
                    return ps;
                });

        // Deduct from source, add to destination
        fromStock.setQuantity(fromStock.getQuantity() - quantity);
        toStock.setQuantity(toStock.getQuantity() + quantity);

        productStockRepository.save(fromStock);
        productStockRepository.save(toStock);

        // Update products.quantity to total across all locations
        updateProductTotalQuantity(productId);

        // Record transfer
        StockTransfer transfer = new StockTransfer();
        transfer.setShop(shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found")));
        transfer.setProduct(fromStock.getProduct());
        transfer.setFromLocation(fromStock.getLocation());
        transfer.setToLocation(toStock.getLocation());
        transfer.setQuantity(quantity);
        transfer.setNote(note);
        transfer.setUser(user);

        return stockTransferRepository.save(transfer);
    }

    // Keep products.quantity in sync with product_stock total
    private void updateProductTotalQuantity(Long productId) {
        Integer total = productStockRepository.getTotalQuantityByProductId(productId);
        productRepository.findById(productId).ifPresent(p -> {
            p.setQuantity(total != null ? total : 0);
            productRepository.save(p);
        });
    }

    public Page<StockTransfer> getTransfers(Long shopId, int page, int size,
                                            String search, LocalDateTime startDate, LocalDateTime endDate) {
        return stockTransferRepository.search(
                shopId, search, startDate, endDate, PageRequest.of(page, size));
    }
}