package rw.stockmanagement.stock_management.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DebtDTO {
    private Long shopId;
    private String debtType; // "CUSTOMER" or "SUPPLIER"
    private String name;
    private String phone;
    private Double totalAmount;
    private Double paidAmount;
    private LocalDate dueDate;
    private String note;
}