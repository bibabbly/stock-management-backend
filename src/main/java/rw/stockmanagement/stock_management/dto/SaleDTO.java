package rw.stockmanagement.stock_management.dto;

import lombok.Data;
import java.util.List;

@Data
public class SaleDTO {
    private Long shopId;
    private Long userId;
    private String paymentMethod;
    private Long supplierId;
    private List<SaleItemDTO> items;

    @Data
    public static class SaleItemDTO {
        private Long productId;
        private Integer quantity;
        private String discountType;  // "PERCENTAGE", "FIXED", or null
        private Double discountValue;
    }
}