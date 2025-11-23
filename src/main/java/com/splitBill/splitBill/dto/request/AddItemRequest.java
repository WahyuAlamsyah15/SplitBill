package com.splitBill.splitBill.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AddItemRequest {
    // @NotBlank(message = "Nama item wajib diisi")
    private String name;

    // @NotNull(message = "Harga wajib diisi")
    // @DecimalMin(value = "0.0", inclusive = false, message = "Harga harus lebih dari 0")
    private BigDecimal price;

    // @Min(value = 1, message = "Jumlah minimal 1")
    private Integer quantity = 1;
}