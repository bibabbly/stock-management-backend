package rw.stockmanagement.stock_management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String email;
    private String role;
    private String shopName;
    private Long userId;
    private Long shopId;
    private List<String> permissions;
    private String name;
}