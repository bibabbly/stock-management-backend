package rw.stockmanagement.stock_management.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.stockmanagement.stock_management.models.*;
import rw.stockmanagement.stock_management.repositories.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockLocationService {

    private final StockLocationRepository stockLocationRepository;
    private final ProductStockRepository productStockRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;

    public List<StockLocation> getLocationsByShop(Long shopId) {
        return stockLocationRepository.findByShopId(shopId);
    }

    @Transactional
    public StockLocation createLocation(Long shopId, String name, Boolean isMain) {
        // Max 3 locations per shop
        long count = stockLocationRepository.countByShopId(shopId);
        if (count >= 3) {
            throw new RuntimeException("Maximum of 3 stock locations allowed per shop");
        }

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        StockLocation location = new StockLocation();
        location.setShop(shop);
        location.setName(name);
        location.setIsMain(isMain != null ? isMain : false);
        location = stockLocationRepository.save(location);

        // Create product_stock entry for all existing products in this shop
        List<Product> products = productRepository.findByShopId(shopId);
        for (Product product : products) {
            ProductStock ps = new ProductStock();
            ps.setProduct(product);
            ps.setLocation(location);
            ps.setQuantity(0);
            productStockRepository.save(ps);
        }

        return location;
    }

    @Transactional
    public StockLocation updateLocation(Long locationId, String name, Boolean isMain) {
        StockLocation location = stockLocationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        location.setName(name);
        if (isMain != null) location.setIsMain(isMain);
        return stockLocationRepository.save(location);
    }

    @Transactional
    public void deleteLocation(Long locationId) {
        // Check if location has stock
        List<ProductStock> stocks = productStockRepository.findByLocationId(locationId);
        boolean hasStock = stocks.stream().anyMatch(ps -> ps.getQuantity() > 0);
        if (hasStock) {
            throw new RuntimeException("Cannot delete location with stock remaining. Transfer stock first.");
        }
        productStockRepository.deleteAll(stocks);
        stockLocationRepository.deleteById(locationId);
    }

    public List<ProductStock> getStockByLocation(Long locationId) {
        return productStockRepository.findByLocationId(locationId);
    }

    public List<ProductStock> getStockByShop(Long shopId) {
        return productStockRepository.findByShopId(shopId);
    }
}