package rw.stockmanagement.stock_management.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "stock_locations")
public class StockLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @Column(nullable = false)
    private String name;

    @Column(name = "is_main")
    private Boolean isMain = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isMain == null) isMain = false;
    }
}