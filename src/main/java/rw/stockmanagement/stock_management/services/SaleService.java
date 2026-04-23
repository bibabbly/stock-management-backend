package rw.stockmanagement.stock_management.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.stockmanagement.stock_management.dto.SaleDTO;
import rw.stockmanagement.stock_management.models.*;
import rw.stockmanagement.stock_management.repositories.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final StockMovementRepository stockMovementRepository;
    private final SupplierRepository supplierRepository;

    public List<Sale> getAllSales(Long shopId) {
        return saleRepository.findByShopId(shopId);
    }

    public Page<Sale> getAllSalesPaged(Long shopId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return saleRepository.findByShopIdOrderByDateDesc(shopId, pageable);
    }

    public Sale getSale(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found"));
    }

    @Transactional
    public Sale createSale(SaleDTO dto) {
        Shop shop = shopRepository.findById(dto.getShopId())
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Sale sale = new Sale();
        sale.setShop(shop);
        sale.setUser(user);
        sale.setPaymentMethod(dto.getPaymentMethod());

        if (dto.getSupplierId() != null) {
            supplierRepository.findById(dto.getSupplierId())
                    .ifPresent(sale::setSupplier);
        }

        List<SaleItem> saleItems = new ArrayList<>();
        List<Product> updatedProducts = new ArrayList<>();
        List<StockMovement> movements = new ArrayList<>();
        double totalAmount = 0.0;

        for (SaleDTO.SaleItemDTO itemDTO : dto.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (product.getQuantity() < itemDTO.getQuantity()) {
                throw new RuntimeException(
                        "Insufficient stock for: " + product.getName() +
                                ". Available: " + product.getQuantity()
                );
            }

            double subtotal = product.getSellingPrice() * itemDTO.getQuantity();
            totalAmount += subtotal;

            SaleItem saleItem = new SaleItem();
            saleItem.setSale(sale);
            saleItem.setProduct(product);
            saleItem.setQuantity(itemDTO.getQuantity());
            saleItem.setUnitPrice(product.getSellingPrice());
            saleItem.setSubtotal(subtotal);
            saleItems.add(saleItem);

            product.setQuantity(product.getQuantity() - itemDTO.getQuantity());
            updatedProducts.add(product);

            StockMovement movement = new StockMovement();
            movement.setShop(shop);
            movement.setProduct(product);
            movement.setType(StockMovement.MovementType.OUT);
            movement.setQuantity(itemDTO.getQuantity());
            movement.setNote("Sale transaction");
            movements.add(movement);
        }

        productRepository.saveAll(updatedProducts);
        stockMovementRepository.saveAll(movements);

        sale.setTotalAmount(totalAmount);
        sale.setItems(saleItems);

        return saleRepository.save(sale);
    }

    public Double getTodayTotal(Long shopId) {
        java.time.LocalDateTime startOfDay = java.time.LocalDate.now().atStartOfDay();
        java.time.LocalDateTime endOfDay = java.time.LocalDate.now().atTime(23, 59, 59);
        return saleRepository.sumTotalAmountByShopIdAndDateBetween(shopId, startOfDay, endOfDay);
    }
}