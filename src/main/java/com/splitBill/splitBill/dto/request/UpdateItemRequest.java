package com.splitBill.splitBill.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class UpdateItemRequest {
    private UUID id;
    private String name;
    private BigDecimal price;
    private Integer quantity;
}
