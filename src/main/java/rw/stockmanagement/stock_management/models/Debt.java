package rw.stockmanagement.stock_management.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "debts")
public class Debt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;

    // CUSTOMER = customer owes shop, SUPPLIER = shop owes supplier
    @Enumerated(EnumType.STRING)
    @Column(name = "debt_type")
    private DebtType debtType;

    // Name of customer or supplier
    private String name;

    private String phone;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "paid_amount")
    private Double paidAmount = 0.0;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private DebtStatus status = DebtStatus.PENDING;

    private String note;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (paidAmount == null) paidAmount = 0.0;
        updateStatus();
    }

    public Double getRemainingAmount() {
        return totalAmount - paidAmount;
    }

    public void updateStatus() {
        if (paidAmount >= totalAmount) {
            status = DebtStatus.PAID;
        } else if (paidAmount > 0) {
            status = DebtStatus.PARTIAL;
        } else {
            status = DebtStatus.PENDING;
        }
    }

    public enum DebtType {
        CUSTOMER, SUPPLIER
    }

    public enum DebtStatus {
        PENDING, PARTIAL, PAID
    }
}