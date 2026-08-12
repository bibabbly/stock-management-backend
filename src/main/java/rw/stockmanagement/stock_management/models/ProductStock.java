package rw.stockmanagement.stock_management.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "product_stock")
public class ProductStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private StockLocation location;

    private Integer quantity = 0;
}