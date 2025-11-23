package com.splitBill.splitBill.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class AssignItemRequest {
    @Min(value = 1, message = "Jumlah porsi minimal 1")
    private int quantityTaken = 1;
}