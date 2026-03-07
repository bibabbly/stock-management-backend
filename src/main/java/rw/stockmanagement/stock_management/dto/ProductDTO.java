package rw.stockmanagement.stock_management.dto;

import lombok.Data;

@Data
public class ProductDTO {
    private String name;
    private String category;
    private String barcode;
    private String unit;
    private Double buyingPrice;
    private Double sellingPrice;
    private Integer quantity;
    private Integer minStock;
    private Long shopId;
}
