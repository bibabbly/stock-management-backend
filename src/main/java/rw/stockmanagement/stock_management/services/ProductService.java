package rw.stockmanagement.stock_management.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.stockmanagement.stock_management.dto.ProductDTO;
import rw.stockmanagement.stock_management.models.Product;
import rw.stockmanagement.stock_management.models.Shop;
import rw.stockmanagement.stock_management.repositories.ProductRepository;
import rw.stockmanagement.stock_management.repositories.ShopRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;

    public Page<Product> getAllProducts(Long shopId, int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        if (search != null && !search.isEmpty()) {
            return productRepository
                    .findByShopIdAndNameContainingIgnoreCaseOrShopIdAndCategoryContainingIgnoreCase(
                            shopId, search, shopId, search, pageable);
        }
        return productRepository.findByShopId(shopId, pageable);
    }

    // Get only ACTIVE products — for sale modal and restock modal
    public List<Product> getActiveProducts(Long shopId) {
        return productRepository.findByShopIdAndActiveTrue(shopId);
    }

    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public Product createProduct(ProductDTO dto) {
        Shop shop = shopRepository.findById(dto.getShopId())
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        Product product = new Product();
        product.setShop(shop);
        product.setName(dto.getName());
        product.setCategory(dto.getCategory());
        product.setBarcode(dto.getBarcode());
        product.setUnit(dto.getUnit());
        product.setBuyingPrice(dto.getBuyingPrice());
        product.setSellingPrice(dto.getSellingPrice());
        product.setQuantity(dto.getQuantity());
        product.setMinStock(dto.getMinStock());
        product.setActive(true);

        return productRepository.save(product);
    }

    public Product updateProduct(Long id, ProductDTO dto) {
        Product product = getProduct(id);
        product.setName(dto.getName());
        product.setCategory(dto.getCategory());
        product.setBarcode(dto.getBarcode());
        product.setUnit(dto.getUnit());
        product.setBuyingPrice(dto.getBuyingPrice());
        product.setSellingPrice(dto.getSellingPrice());
        product.setQuantity(dto.getQuantity());
        product.setMinStock(dto.getMinStock());

        return productRepository.save(product);
    }

    // Deactivate — only if quantity = 0
    @Transactional
    public Product deactivateProduct(Long id) {
        Product product = getProduct(id);
        if (product.getQuantity() != null && product.getQuantity() > 0) {
            throw new RuntimeException(
                    "Cannot deactivate product with stock remaining. Current stock: " + product.getQuantity() +
                            ". Please do a manual stock out first.");
        }
        product.setActive(false);
        return productRepository.save(product);
    }

    // Reactivate anytime
    @Transactional
    public Product reactivateProduct(Long id) {
        Product product = getProduct(id);
        product.setActive(true);
        return productRepository.save(product);
    }

    // Delete — blocked, use deactivate instead
    public void deleteProduct(Long id) {
        Product product = getProduct(id);
        if (product.getQuantity() != null && product.getQuantity() > 0) {
            throw new RuntimeException("Cannot delete product with stock remaining.");
        }
        // Instead of deleting, deactivate
        product.setActive(false);
        productRepository.save(product);
    }

    public List<Product> getLowStockProducts(Long shopId) {
        return productRepository.findByShopIdAndQuantityLessThan(shopId, 10);
    }
}