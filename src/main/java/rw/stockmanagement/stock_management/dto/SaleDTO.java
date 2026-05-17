package rw.stockmanagement.stock_management.dto;

import lombok.Data;
import java.util.List;

@Data
public class SaleDTO {
    private Long shopId;
    private Long userId;
    private String paymentMethod;
    private Long supplierId;
    private String discountType;  // "PERCENTAGE" or "FIXED"
    private Double discountValue; // e.g. 10 or 5000
    private List<SaleItemDTO> items;

    @Data
    public static class SaleItemDTO {
        private Long productId;
        private Integer quantity;
    }
}