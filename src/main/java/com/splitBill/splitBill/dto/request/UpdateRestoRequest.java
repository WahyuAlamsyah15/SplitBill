package com.splitBill.splitBill.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateRestoRequest {
    @NotBlank(message = "Nama resto tidak boleh kosong")
    private String name;
}
