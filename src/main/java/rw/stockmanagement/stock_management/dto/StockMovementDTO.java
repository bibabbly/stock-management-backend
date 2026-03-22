package rw.stockmanagement.stock_management.dto;

import lombok.Data;

@Data
public class StockMovementDTO {
    private Long shopId;
    private Long productId;
    private Long supplierId;
    private Integer quantity;
    private String note;
    private Long userId;
}