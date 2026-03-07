package rw.stockmanagement.stock_management.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @Column(nullable = false)
    private String name;

    private String category;
    private String barcode;
    private String unit;

    @Column(name = "buying_price")
    private Double buyingPrice;

    @Column(name = "selling_price")
    private Double sellingPrice;

    private Integer quantity;

    @Column(name = "min_stock")
    private Integer minStock;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
