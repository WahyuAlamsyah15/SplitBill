package com.splitBill.splitBill.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ItemResponse {
    private UUID id;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
}