package rw.stockmanagement.stock_management.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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

    // Get all products by shop — paginated + search
    public Page<Product> getAllProducts(Long shopId, int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        if (search != null && !search.isEmpty()) {
            return productRepository
                    .findByShopIdAndNameContainingIgnoreCaseOrShopIdAndCategoryContainingIgnoreCase(
                            shopId, search, shopId, search, pageable);
        }
        return productRepository.findByShopId(shopId, pageable);
    }

    // Get single product
    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    // Create product
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

        return productRepository.save(product);
    }

    // Update product
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

    // Delete product
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    // Get low stock products
    public List<Product> getLowStockProducts(Long shopId) {
        return productRepository.findByShopIdAndQuantityLessThan(shopId, 10);
    }
}