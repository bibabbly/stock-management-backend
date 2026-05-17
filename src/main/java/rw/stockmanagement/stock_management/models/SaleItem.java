package rw.stockmanagement.stock_management.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "sale_items")
public class SaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sale_id")
    @JsonBackReference
    private Sale sale;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantity;

    @Column(name = "unit_price")
    private Double unitPrice;

    private Double subtotal; // original = unitPrice * quantity

    @Column(name = "discount_type")
    private String discountType; // "PERCENTAGE" or "FIXED"

    @Column(name = "discount_value")
    private Double discountValue;

    @Column(name = "discount_amount")
    private Double discountAmount;

    @Column(name = "final_subtotal")
    private Double finalSubtotal; // subtotal - discountAmount
}