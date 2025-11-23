package com.splitBill.splitBill.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateRestoRequest {
    @NotBlank(message = "Nama resto wajib diisi")
    private String name;
}