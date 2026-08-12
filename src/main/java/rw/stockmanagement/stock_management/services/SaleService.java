package rw.stockmanagement.stock_management.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.stockmanagement.stock_management.dto.SaleDTO;
import rw.stockmanagement.stock_management.models.*;
import rw.stockmanagement.stock_management.repositories.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final SaleItemRepository saleItemRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductStockRepository productStockRepository;
    private final StockLocationRepository stockLocationRepository;

    public Page<Sale> getAllSalesPaged(Long shopId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Sale> salesPage = saleRepository.findByShopIdAndStatusOrderByDateDesc(
                shopId, Sale.SaleStatus.COMPLETED, pageable);

        List<Long> ids = salesPage.getContent().stream()
                .map(Sale::getId)
                .collect(Collectors.toList());

        List<Sale> salesWithDetails = ids.isEmpty()
                ? Collections.emptyList()
                : saleRepository.findByIdsWithDetails(ids);

        return new PageImpl<>(salesWithDetails, pageable, salesPage.getTotalElements());
    }

    public Sale getSale(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found"));
    }

    @Transactional
    public Sale createSale(SaleDTO dto) {
        Shop shop = shopRepository.findById(dto.getShopId())
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        User user = null;
        if (dto.getUserId() != null) {
            user = userRepository.findById(dto.getUserId()).orElse(null);
        }

        Supplier supplier = null;
        if (dto.getSupplierId() != null) {
            supplier = supplierRepository.findById(dto.getSupplierId()).orElse(null);
        }

        // Get main location for stock deduction
        StockLocation mainLocation = stockLocationRepository
                .findByShopIdAndIsMainTrue(dto.getShopId())
                .orElse(null);

        Sale sale = new Sale();
        sale.setShop(shop);
        sale.setUser(user);
        sale.setSupplier(supplier);
        sale.setPaymentMethod(dto.getPaymentMethod());
        sale.setStatus(Sale.SaleStatus.COMPLETED);

        List<SaleItem> items = new ArrayList<>();
        double originalAmount = 0;
        double discountAmount = 0;

        for (SaleDTO.SaleItemDTO itemDto : dto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException(
                            "Product not found: " + itemDto.getProductId()));

            int qty = itemDto.getQuantity();
            double unitPrice = product.getSellingPrice();
            double subtotal = unitPrice * qty;

            // Calculate discount
            double itemDiscountAmount = 0;
            String discountType = null;
            Double discountValue = null;

            if (itemDto.getDiscountType() != null && itemDto.getDiscountValue() != null
                    && itemDto.getDiscountValue() > 0) {
                discountType = itemDto.getDiscountType();
                discountValue = itemDto.getDiscountValue();

                if ("PERCENTAGE".equals(discountType)) {
                    itemDiscountAmount = subtotal * (discountValue / 100);
                } else if ("FIXED".equals(discountType)) {
                    itemDiscountAmount = Math.min(discountValue, subtotal);
                }
            }

            double finalSubtotal = subtotal - itemDiscountAmount;
            originalAmount += subtotal;
            discountAmount += itemDiscountAmount;

            // Deduct from product_stock main location if available
            if (mainLocation != null) {
                ProductStock productStock = productStockRepository
                        .findByProductIdAndLocationId(product.getId(), mainLocation.getId())
                        .orElse(null);

                if (productStock != null) {
                    int newQty = Math.max(0, productStock.getQuantity() - qty);
                    productStock.setQuantity(newQty);
                    productStockRepository.save(productStock);
                }

                // Sync products.quantity with total across all locations
                Integer total = productStockRepository
                        .getTotalQuantityByProductId(product.getId());
                product.setQuantity(total != null ? total
                        : Math.max(0, product.getQuantity() - qty));
            } else {
                // Fallback for shops without locations
                product.setQuantity(Math.max(0, product.getQuantity() - qty));
            }

            productRepository.save(product);

            // Record stock movement OUT
            StockMovement movement = new StockMovement();
            movement.setShop(shop);
            movement.setProduct(product);
            movement.setType(StockMovement.MovementType.OUT);
            movement.setQuantity(qty);
            movement.setNote("Sale transaction");
            if (user != null) movement.setUser(user);
            stockMovementRepository.save(movement);

            // Build sale item
            SaleItem saleItem = new SaleItem();
            saleItem.setSale(sale);
            saleItem.setProduct(product);
            saleItem.setQuantity(qty);
            saleItem.setUnitPrice(unitPrice);
            saleItem.setSubtotal(subtotal);
            saleItem.setDiscountType(discountType);
            saleItem.setDiscountValue(discountValue);
            saleItem.setDiscountAmount(itemDiscountAmount);
            saleItem.setFinalSubtotal(finalSubtotal);
            items.add(saleItem);
        }

        sale.setOriginalAmount(originalAmount);
        sale.setDiscountAmount(discountAmount);
        sale.setTotalAmount(originalAmount - discountAmount);
        sale.setItems(items);

        return saleRepository.save(sale);
    }

    public double getTodayTotal(Long shopId) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(23, 59, 59);
        return saleRepository.sumTotalAmountByShopIdAndDateBetween(shopId, start, end);
    }

    public List<Sale> getAllSales(Long shopId) {
        return saleRepository.findCompletedByShopId(shopId);
    }

    public List<Sale> getSalesByDateRange(Long shopId, LocalDateTime start, LocalDateTime end) {
        return saleRepository.findByShopIdAndDateBetweenOptimized(shopId, start, end);
    }

    public List<Map<String, Object>> getCashDeskReport(Long shopId,
                                                       LocalDateTime start,
                                                       LocalDateTime end) {
        List<Object[]> raw = saleRepository.getCashDeskReport(shopId, start, end);
        return raw.stream().map(row -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("userId", row[0]);
            item.put("userName", row[1]);
            item.put("salesCount", row[2]);
            item.put("totalRevenue", row[3]);
            item.put("cashAmount", row[4]);
            item.put("momoAmount", row[5]);
            item.put("bankAmount", row[6]);
            return item;
        }).collect(Collectors.toList());
    }

    @Transactional
    public Sale cancelSale(Long saleId, Long cancelledByUserId, String reason) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new RuntimeException("Sale not found"));

        if (sale.getStatus() == Sale.SaleStatus.CANCELLED) {
            throw new RuntimeException("Sale is already cancelled");
        }

        if (!sale.getDate().toLocalDate().equals(LocalDate.now())) {
            throw new RuntimeException(
                    "Sale can only be cancelled on the same day it was made");
        }

        User cancelledBy = userRepository.findById(cancelledByUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get main location for stock restoration
        StockLocation mainLocation = stockLocationRepository
                .findByShopIdAndIsMainTrue(sale.getShop().getId())
                .orElse(null);

        List<StockMovement> movements = new ArrayList<>();

        for (SaleItem item : sale.getItems()) {
            Product product = item.getProduct();

            if (mainLocation != null) {
                ProductStock productStock = productStockRepository
                        .findByProductIdAndLocationId(
                                product.getId(), mainLocation.getId())
                        .orElseGet(() -> {
                            ProductStock ps = new ProductStock();
                            ps.setProduct(product);
                            ps.setLocation(mainLocation);
                            ps.setQuantity(0);
                            return ps;
                        });
                productStock.setQuantity(
                        productStock.getQuantity() + item.getQuantity());
                productStockRepository.save(productStock);

                // Sync products.quantity
                Integer total = productStockRepository
                        .getTotalQuantityByProductId(product.getId());
                product.setQuantity(total != null ? total
                        : product.getQuantity() + item.getQuantity());
            } else {
                product.setQuantity(product.getQuantity() + item.getQuantity());
            }

            productRepository.save(product);

            // Record stock movement IN for return
            StockMovement movement = new StockMovement();
            movement.setShop(sale.getShop());
            movement.setProduct(product);
            movement.setType(StockMovement.MovementType.IN);
            movement.setQuantity(item.getQuantity());
            movement.setNote("Sale cancelled - #" + sale.getId());
            movements.add(movement);
        }

        stockMovementRepository.saveAll(movements);

        sale.setStatus(Sale.SaleStatus.CANCELLED);
        sale.setCancelledBy(cancelledBy);
        sale.setCancelledAt(LocalDateTime.now());
        sale.setCancelReason(reason);

        return saleRepository.save(sale);
    }

    public Page<Sale> getCancelledSalesPaged(Long shopId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Sale> salesPage = saleRepository.findByShopIdAndStatusOrderByDateDesc(
                shopId, Sale.SaleStatus.CANCELLED, pageable);

        List<Long> ids = salesPage.getContent().stream()
                .map(Sale::getId)
                .collect(Collectors.toList());

        List<Sale> salesWithDetails = ids.isEmpty()
                ? Collections.emptyList()
                : saleRepository.findByIdsWithDetails(ids);

        return new PageImpl<>(salesWithDetails, pageable, salesPage.getTotalElements());
    }
}