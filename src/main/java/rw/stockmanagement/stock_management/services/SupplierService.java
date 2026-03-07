package rw.stockmanagement.stock_management.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rw.stockmanagement.stock_management.models.Supplier;
import rw.stockmanagement.stock_management.models.Shop;
import rw.stockmanagement.stock_management.repositories.SupplierRepository;
import rw.stockmanagement.stock_management.repositories.ShopRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final ShopRepository shopRepository;

    public List<Supplier> getAllSuppliers(Long shopId) {
        return supplierRepository.findByShopId(shopId);
    }

    public Supplier getSupplier(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
    }

    public Supplier createSupplier(Supplier supplier) {
        Shop shop = shopRepository.findById(supplier.getShop().getId())
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        supplier.setShop(shop);
        return supplierRepository.save(supplier);
    }

    public Supplier updateSupplier(Long id, Supplier updated) {
        Supplier supplier = getSupplier(id);
        supplier.setName(updated.getName());
        supplier.setPhone(updated.getPhone());
        supplier.setEmail(updated.getEmail());
        supplier.setAddress(updated.getAddress());
        return supplierRepository.save(supplier);
    }

    public void deleteSupplier(Long id) {
        supplierRepository.deleteById(id);
    }
}